package com.club.material;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.club.common.BusinessException;
import com.club.common.DBUtil;

/**
 * 物资模块的业务层：所有"能不能、够不够、合不合法"的判断都在这里。
 *
 * <p>【它在三层里的位置】页面说"我要停用 3 号物资" → Servlet 把请求转给它 →
 * 它做规则判断，再指挥 DAO 改数据库 → 把结果或错误原因告诉 Servlet。</p>
 *
 * <p>【三条规矩】</p>
 * <p>1. 数量只走四个动作：入库、预留、释放、借出、归还。编辑表单不允许直接改数量，
 *    否则库存就和流水对不上。</p>
 * <p>2. 每个改库存的动作都要写一条流水，并且两件事在一个事务里完成。</p>
 * <p>3. 用户能看懂的问题用 BusinessException 抛出（例如"可用库存不足"），
 *    数据库故障才提示"系统繁忙，请稍后再试"。</p>
 *
 * <p>【给队友的公开接口】审批借还模块（晏思汗）会调用这里的 reserve / release /
 * borrowOut / giveBack 四个方法。签名已经和任务书约定一致，请勿改动参数顺序。</p>
 */
public class MaterialService {

    private final MaterialDao materialDao = new MaterialDao();
    private final StockLogDao stockLogDao = new StockLogDao();

    // ============================ 查询类 ============================

    /**
     * 物资列表。keyword 模糊匹配名称，category 精确匹配分类，status 精确匹配状态。
     */
    public List<Material> list(String keyword, String category, String status) {
        try {
            return materialDao.search(keyword, category, status);
        } catch (SQLException e) {
            throw new BusinessException("查询物资失败，请稍后再试");
        }
    }

    /**
     * 查一件物资，不存在返回 null。
     */
    public Material get(int id) {
        try {
            return materialDao.findById(id);
        } catch (SQLException e) {
            throw new BusinessException("查询物资失败，请稍后再试");
        }
    }

    /**
     * 所有已有分类，供页面下拉框使用。
     */
    public List<String> categories() {
        try {
            return materialDao.findAllCategories();
        } catch (SQLException e) {
            throw new BusinessException("读取分类失败，请稍后再试");
        }
    }

    /**
     * 某个物资最近的库存流水。
     */
    public List<StockLog> logs(int materialId) {
        try {
            return stockLogDao.findByMaterial(materialId);
        } catch (SQLException e) {
            throw new BusinessException("读取库存流水失败，请稍后再试");
        }
    }

    // ============================ 维护类 ============================

    /**
     * 新增物资。物资本身和"初始入库流水"必须一起成功，所以放在同一个事务里。
     *
     * @param material   页面提交上来的物资
     * @param operatorId 操作人编号
     */
    public void create(Material material, Integer operatorId) {
        validateBasic(material);
        if (material.getTotalQty() < 0) {
            throw new BusinessException("库存总量不能是负数");
        }
        Connection conn = null;
        try {
            if (materialDao.existsName(material.getName(), 0)) {
                throw new BusinessException("物资名称已存在：" + material.getName());
            }
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            int newId = materialDao.insert(conn, material);
            stockLogDao.insert(conn, buildLog(newId, null, "ADJUST",
                    material.getTotalQty(), 0, 0, material.getTotalQty(),
                    operatorId, "新建物资并初始化库存"));

            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new BusinessException("新增物资失败，请稍后再试");
        } catch (BusinessException e) {
            rollbackQuietly(conn);
            throw e;
        } finally {
            DBUtil.close(conn);
        }
    }

    /**
     * 修改基础信息。数量不在这里改，要走入库或借还流程。
     */
    public void update(Material material, Integer operatorId) {
        validateBasic(material);
        try (Connection conn = DBUtil.getConnection()) {
            if (materialDao.existsName(material.getName(), material.getId())) {
                throw new BusinessException("物资名称已存在：" + material.getName());
            }
            int rows = materialDao.updateBasic(conn, material);
            if (rows == 0) {
                throw new BusinessException("物资不存在，可能已被删除");
            }
        } catch (SQLException e) {
            throw new BusinessException("保存物资失败，请稍后再试");
        }
    }

    /**
     * 启用 / 停用。只允许这两个状态值，避免页面传进来奇怪的字符串。
     */
    public void changeStatus(int id, String status) {
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            throw new BusinessException("状态只能是 ACTIVE 或 INACTIVE");
        }
        try (Connection conn = DBUtil.getConnection()) {
            int rows = materialDao.updateStatus(conn, id, status);
            if (rows == 0) {
                throw new BusinessException("物资不存在，可能已被删除");
            }
        } catch (SQLException e) {
            throw new BusinessException("修改状态失败，请稍后再试");
        }
    }

    /**
     * 入库：数量必须为正整数，改库存的同时记一条 ADJUST 流水。
     */
    public void addStock(int id, int quantity, Integer operatorId, String note) {
        if (quantity <= 0) {
            throw new BusinessException("入库数量必须是正整数");
        }
        Connection conn = null;
        try {
            Material material = materialDao.findById(id);
            if (material == null) {
                throw new BusinessException("物资不存在，可能已被删除");
            }
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            materialDao.addStock(conn, id, quantity);
            String finalNote = (note == null || note.trim().isEmpty()) ? "入库" : note.trim();
            stockLogDao.insert(conn, buildLog(id, null, "ADJUST", quantity, 0, 0, quantity, operatorId, finalNote));

            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new BusinessException("入库失败，请稍后再试");
        } catch (BusinessException e) {
            rollbackQuietly(conn);
            throw e;
        } finally {
            DBUtil.close(conn);
        }
    }

    // ================== 借还流程调用：库存条件更新 ==================

    /**
     * 预留库存：审批通过时调用。可用 → 预留。
     *
     * @param materialId    物资编号
     * @param quantity      数量
     * @param applicationId 关联申请单编号（写进流水，方便追溯）
     * @param operatorId    操作人（审批人）
     */
    public void reserve(int materialId, int quantity, int applicationId, Integer operatorId) {
        if (quantity <= 0) {
            throw new BusinessException("预留数量必须大于 0");
        }
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            int rows = materialDao.reserve(conn, materialId, quantity);
            if (rows == 0) {
                throw new BusinessException("可用库存不足或物资已停用，无法通过审批");
            }
            stockLogDao.insert(conn, buildLog(materialId, applicationId, "RESERVE",
                    -quantity, quantity, 0, 0, operatorId, "审批通过，占用库存"));

            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new BusinessException("占用库存失败，请稍后再试");
        } catch (BusinessException e) {
            rollbackQuietly(conn);
            throw e;
        } finally {
            DBUtil.close(conn);
        }
    }

    /**
     * 释放预留：驳回或作废时调用。预留 → 可用。
     */
    public void release(int materialId, int quantity, int applicationId, Integer operatorId) {
        if (quantity <= 0) {
            throw new BusinessException("释放数量必须大于 0");
        }
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            int rows = materialDao.release(conn, materialId, quantity);
            if (rows == 0) {
                throw new BusinessException("预留数量不足，无法释放");
            }
            stockLogDao.insert(conn, buildLog(materialId, applicationId, "RELEASE",
                    quantity, -quantity, 0, 0, operatorId, "驳回或作废，释放预留"));

            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new BusinessException("释放库存失败，请稍后再试");
        } catch (BusinessException e) {
            rollbackQuietly(conn);
            throw e;
        } finally {
            DBUtil.close(conn);
        }
    }

    /**
     * 借出：管理员确认东西被领走。预留 → 借出。
     */
    public void borrowOut(int materialId, int quantity, int applicationId, Integer operatorId) {
        if (quantity <= 0) {
            throw new BusinessException("借出数量必须大于 0");
        }
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            int rows = materialDao.borrowOut(conn, materialId, quantity);
            if (rows == 0) {
                throw new BusinessException("预留数量不足，无法借出");
            }
            stockLogDao.insert(conn, buildLog(materialId, applicationId, "BORROW",
                    0, -quantity, quantity, 0, operatorId, "管理员确认借出"));

            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new BusinessException("借出失败，请稍后再试");
        } catch (BusinessException e) {
            rollbackQuietly(conn);
            throw e;
        } finally {
            DBUtil.close(conn);
        }
    }

    /**
     * 归还：借出 → 可用；损坏与遗失的部分从总量中核销。
     *
     * @param quantity      归还数量
     * @param damagedQty    其中损坏的数量
     * @param lostQty       其中遗失的数量
     */
    public void giveBack(int materialId, int quantity, int damagedQty, int lostQty,
                         int applicationId, Integer operatorId) {
        if (quantity <= 0) {
            throw new BusinessException("归还数量必须大于 0");
        }
        if (damagedQty < 0 || lostQty < 0) {
            throw new BusinessException("损坏和遗失数量不能是负数");
        }
        if (damagedQty + lostQty > quantity) {
            throw new BusinessException("损坏与遗失数量之和不能超过归还数量");
        }
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            int rows = materialDao.giveBack(conn, materialId, quantity, damagedQty, lostQty);
            if (rows == 0) {
                throw new BusinessException("借出数量不足，无法归还");
            }
            stockLogDao.insert(conn, buildLog(materialId, applicationId, "RETURN",
                    quantity - damagedQty - lostQty, 0, -quantity, -(damagedQty + lostQty),
                    operatorId, buildReturnNote(damagedQty, lostQty)));

            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new BusinessException("归还失败，请稍后再试");
        } catch (BusinessException e) {
            rollbackQuietly(conn);
            throw e;
        } finally {
            DBUtil.close(conn);
        }
    }

    // ============================ 私有工具 ============================

    /**
     * 基础字段校验。长度按数据库字段长度限制来，提前拦住，避免数据库报错看不懂。
     */
    private void validateBasic(Material material) {
        if (material.getName() == null || material.getName().trim().isEmpty()) {
            throw new BusinessException("物资名称不能为空");
        }
        material.setName(material.getName().trim());
        if (material.getName().length() > 50) {
            throw new BusinessException("物资名称不能超过 50 个字");
        }
        if (material.getCategory() == null || material.getCategory().trim().isEmpty()) {
            throw new BusinessException("物资分类不能为空");
        }
        material.setCategory(material.getCategory().trim());
        if (material.getCategory().length() > 30) {
            throw new BusinessException("物资分类不能超过 30 个字");
        }
        if (material.getUnit() == null || material.getUnit().trim().isEmpty()) {
            material.setUnit("件");
        }
        material.setUnit(material.getUnit().trim());
        if (material.getRemark() != null && material.getRemark().length() > 200) {
            throw new BusinessException("备注不能超过 200 个字");
        }
    }

    /**
     * 组装一条流水，省得每个方法都写一遍 setter。
     */
    private StockLog buildLog(int materialId, Integer applicationId, String changeType,
                              int deltaAvailable, int deltaReserved, int deltaBorrowed, int deltaTotal,
                              Integer operatorId, String note) {
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

    /**
     * 归还流水的备注：有损坏或遗失时写清楚数量，方便事后解释总量为什么少了。
     */
    private String buildReturnNote(int damagedQty, int lostQty) {
        if (damagedQty == 0 && lostQty == 0) {
            return "全部完好归还";
        }
        return "归还核销：损坏 " + damagedQty + "，遗失 " + lostQty;
    }

    /**
     * 回滚时不抛异常：原始错误更重要，别让"回滚失败"把真正的报错盖住。
     */
    private void rollbackQuietly(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // 连接可能已经断开，忽略
        }
    }
}
