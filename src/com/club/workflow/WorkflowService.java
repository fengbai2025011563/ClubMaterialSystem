package com.club.workflow;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.club.application.bean.ApplicationItem;
import com.club.application.bean.LoanApplication;
import com.club.application.dao.ApplicationDao;
import com.club.common.BusinessException;
import com.club.common.DBUtil;
import com.club.material.MaterialDao;
import com.club.material.StockLog;
import com.club.material.StockLogDao;

/**
 * 审批、借出、归还的业务层，是全组唯一允许开启事务的地方。
 *
 * <p>状态迁移和库存更新在同一个 Connection 里完成：任何一步失败就整体回滚，
 * 不会出现“状态改了、库存没动”的中间态。库存更新按 material_id 升序执行，
 * 固定加锁顺序，避免两个事务交叉等待。</p>
 */
public class WorkflowService {

    private final WorkflowDao workflowDao = new WorkflowDao();
    private final ApplicationDao applicationDao = new ApplicationDao();
    private final MaterialDao materialDao = new MaterialDao();
    private final StockLogDao stockLogDao = new StockLogDao();

    public List<LoanApplication> listByStatus(String status) {
        try (Connection conn = DBUtil.getConnection()) {
            return workflowDao.findByStatus(conn, status);
        } catch (SQLException e) {
            throw new BusinessException("查询申请列表失败", e);
        }
    }

    public LoanApplication detail(int applicationId) {
        try (Connection conn = DBUtil.getConnection()) {
            LoanApplication app = workflowDao.findById(conn, applicationId);
            if (app == null) {
                throw new BusinessException("申请不存在");
            }
            return app;
        } catch (SQLException e) {
            throw new BusinessException("查询申请失败", e);
        }
    }

    public List<ApplicationItem> items(int applicationId) {
        try (Connection conn = DBUtil.getConnection()) {
            return applicationDao.findItems(conn, applicationId);
        } catch (SQLException e) {
            throw new BusinessException("查询申请明细失败", e);
        }
    }

    /** 审批通过：状态 PENDING→APPROVED，逐项把可用量转成预留量。 */
    public void approve(int applicationId, int adminId, String remark) {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (workflowDao.markApproved(conn, applicationId, adminId, remark) != 1) {
                    throw new BusinessException("该申请已被处理，请刷新");
                }
                List<ApplicationItem> items = applicationDao.findItems(conn, applicationId);
                items.sort(Comparator.comparingInt(ApplicationItem::getMaterialId));
                for (ApplicationItem item : items) {
                    int qty = item.getQuantity();
                    if (materialDao.reserve(conn, item.getMaterialId(), qty) != 1) {
                        throw new BusinessException("物资【" + item.getMaterialName() + "】可用库存不足，未批准");
                    }
                    stockLogDao.insert(conn, buildLog(item.getMaterialId(), applicationId, "RESERVE",
                            -qty, qty, 0, 0, adminId, "审批通过，占用库存"));
                }
                conn.commit();
            } catch (BusinessException e) {
                conn.rollback();
                throw e;
            } catch (Exception e) {
                conn.rollback();
                throw new BusinessException("审批失败，请重试", e);
            }
        } catch (SQLException e) {
            throw new BusinessException("数据库连接失败", e);
        }
    }

    /** 驳回：PENDING→REJECTED，不动库存。 */
    public void reject(int applicationId, int adminId, String remark) {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (workflowDao.markRejected(conn, applicationId, adminId, remark) != 1) {
                    throw new BusinessException("该申请已被处理，请刷新");
                }
                conn.commit();
            } catch (BusinessException e) {
                conn.rollback();
                throw e;
            } catch (Exception e) {
                conn.rollback();
                throw new BusinessException("驳回失败，请重试", e);
            }
        } catch (SQLException e) {
            throw new BusinessException("数据库连接失败", e);
        }
    }

    /** 作废已批准：APPROVED→CANCELLED，逐项把预留量释放回可用量。 */
    public void voidApproved(int applicationId, int adminId, String remark) {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (workflowDao.markVoid(conn, applicationId, adminId, remark) != 1) {
                    throw new BusinessException("该申请已被处理，请刷新");
                }
                List<ApplicationItem> items = applicationDao.findItems(conn, applicationId);
                items.sort(Comparator.comparingInt(ApplicationItem::getMaterialId));
                for (ApplicationItem item : items) {
                    int qty = item.getQuantity();
                    if (materialDao.release(conn, item.getMaterialId(), qty) != 1) {
                        throw new BusinessException("物资【" + item.getMaterialName() + "】预留数量不足，未作废");
                    }
                    stockLogDao.insert(conn, buildLog(item.getMaterialId(), applicationId, "RELEASE",
                            qty, -qty, 0, 0, adminId, "作废申请，释放预留"));
                }
                conn.commit();
            } catch (BusinessException e) {
                conn.rollback();
                throw e;
            } catch (Exception e) {
                conn.rollback();
                throw new BusinessException("作废失败，请重试", e);
            }
        } catch (SQLException e) {
            throw new BusinessException("数据库连接失败", e);
        }
    }

    /** 登记借出：APPROVED→BORROWED，逐项把预留量转成借出量。 */
    public void borrow(int applicationId, int adminId) {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (workflowDao.markBorrowed(conn, applicationId) != 1) {
                    throw new BusinessException("该申请已被处理，请刷新");
                }
                List<ApplicationItem> items = applicationDao.findItems(conn, applicationId);
                items.sort(Comparator.comparingInt(ApplicationItem::getMaterialId));
                for (ApplicationItem item : items) {
                    int qty = item.getQuantity();
                    if (materialDao.borrowOut(conn, item.getMaterialId(), qty) != 1) {
                        throw new BusinessException("物资【" + item.getMaterialName() + "】预留数量不足，未借出");
                    }
                    stockLogDao.insert(conn, buildLog(item.getMaterialId(), applicationId, "BORROW",
                            0, -qty, qty, 0, adminId, "确认借出"));
                }
                conn.commit();
            } catch (BusinessException e) {
                conn.rollback();
                throw e;
            } catch (Exception e) {
                conn.rollback();
                throw new BusinessException("登记借出失败，请重试", e);
            }
        } catch (SQLException e) {
            throw new BusinessException("数据库连接失败", e);
        }
    }

    /**
     * 归还验收：BORROWED→RETURNED。
     * damagedLost 的 key 是明细 id，value 是 {损坏, 遗失}；没传的按 0 处理。
     */
    public void returnAll(int applicationId, int adminId, Map<Integer, int[]> damagedLost) {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (workflowDao.markReturned(conn, applicationId) != 1) {
                    throw new BusinessException("该申请已被处理，请刷新");
                }
                List<ApplicationItem> items = applicationDao.findItems(conn, applicationId);
                items.sort(Comparator.comparingInt(ApplicationItem::getMaterialId));
                for (ApplicationItem item : items) {
                    int qty = item.getQuantity();
                    int damaged = 0;
                    int lost = 0;
                    int[] pair = damagedLost == null ? null : damagedLost.get(item.getId());
                    if (pair != null) {
                        damaged = pair[0];
                        lost = pair[1];
                    }
                    if (damaged < 0 || lost < 0 || damaged + lost > qty) {
                        throw new BusinessException("物资【" + item.getMaterialName() + "】损坏+遗失超过申请数量");
                    }
                    if (materialDao.giveBack(conn, item.getMaterialId(), qty, damaged, lost) != 1) {
                        throw new BusinessException("物资【" + item.getMaterialName() + "】借出数量对不上，未归还");
                    }
                    workflowDao.updateItemReturn(conn, applicationId, item.getId(), damaged, lost);

                    int good = qty - damaged - lost;
                    stockLogDao.insert(conn, buildLog(item.getMaterialId(), applicationId, "RETURN",
                            good, 0, -qty, -(damaged + lost), adminId, "归还验收"));
                }
                conn.commit();
            } catch (BusinessException e) {
                conn.rollback();
                throw e;
            } catch (Exception e) {
                conn.rollback();
                throw new BusinessException("归还失败，请重试", e);
            }
        } catch (SQLException e) {
            throw new BusinessException("数据库连接失败", e);
        }
    }

    private StockLog buildLog(int materialId, Integer applicationId, String changeType,
                              int deltaAvailable, int deltaReserved, int deltaBorrowed, int deltaTotal,
                              int operatorId, String note) {
        StockLog log = new StockLog();
        log.setMaterialId(materialId);
        log.setApplicationId(applicationId);
        log.setChangeType(changeType);
        log.setDeltaAvailable(deltaAvailable);
        log.setDeltaReserved(deltaReserved);
        log.setDeltaBorrowed(deltaBorrowed);
        log.setDeltaTotal(deltaTotal);
        log.setOperatorId(operatorId);
        log.setNote(note);
        return log;
    }
}
