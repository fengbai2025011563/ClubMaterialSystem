package com.club.application.service;

import com.club.application.bean.ApplicationItem;
import com.club.application.bean.LoanApplication;
import com.club.application.dao.ApplicationDao;
import com.club.application.temp.BusinessException;
import com.club.application.temp.TempDBUtil;
import com.club.application.temp.TempMaterialService;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 申请业务层。
 * 只有 Service 能 getConnection / commit / rollback；DAO 禁止事务操作。
 */
public class ApplicationService {

    private final ApplicationDao dao = new ApplicationDao();
    /* 【临时】stu2 的 MaterialService 到位后替换 */
    private final TempMaterialService materialService = new TempMaterialService();

    /* 创建申请（含单头 + 明细），事务包裹 */
    public void create(int applicantId, LoanApplication app, List<ApplicationItem> items) {
        validate(app, items);                       // 纯校验，不碰库
        app.setApplicantId(applicantId);

        try (Connection conn = TempDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                checkAvailable(conn, items);        // 提示性库存校验，仍在同一事务内
                int id = dao.insert(conn, app);     // 单头
                dao.insertItems(conn, id, items);   // 明细
                conn.commit();
            } catch (SQLIntegrityConstraintViolationException dup) {
                conn.rollback();                    // client_token 唯一键冲突
                throw new BusinessException("请勿重复提交", dup);
            } catch (BusinessException be) {
                conn.rollback();                    // 业务校验失败：原样抛出具体原因
                throw be;
            } catch (Exception e) {
                conn.rollback();                    // 单头成功、明细失败时不能留半张单
                throw new BusinessException("提交申请失败，请重试", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new BusinessException("数据库连接失败", e);
        }
    }

    /* 查我的申请列表 */
    public List<LoanApplication> listMine(int applicantId, String status) {
        try (Connection conn = TempDBUtil.getConnection()) {
            return dao.findByApplicant(conn, applicantId, status);
        } catch (SQLException e) {
            throw new BusinessException("查询申请列表失败", e);
        }
    }

    /* 查单头详情；非本人访问抛 BusinessException */
    public LoanApplication detail(int applicationId, int applicantId) {
        try (Connection conn = TempDBUtil.getConnection()) {
            LoanApplication app = dao.findById(conn, applicationId);
            if (app == null) {
                throw new BusinessException("申请不存在");
            }
            if (app.getApplicantId() == null || app.getApplicantId() != applicantId) {
                throw new BusinessException("无权查看该申请");
            }
            return app;
        } catch (SQLException e) {
            throw new BusinessException("查询申请详情失败", e);
        }
    }

    /* 查明细 */
    public List<ApplicationItem> items(int applicationId) {
        try (Connection conn = TempDBUtil.getConnection()) {
            return dao.findItems(conn, applicationId);
        } catch (SQLException e) {
            throw new BusinessException("查询物资明细失败", e);
        }
    }

    /* 本人撤回 PENDING 的申请；状态已变返回"已被处理" */
    public void cancel(int applicationId, int applicantId) {
        try (Connection conn = TempDBUtil.getConnection()) {
            int affected = dao.cancel(conn, applicationId, applicantId);
            if (affected == 0) {
                throw new BusinessException("该申请已被处理");
            }
        } catch (SQLException e) {
            throw new BusinessException("撤回申请失败", e);
        }
    }

    // ================== 校验（纯校验，不碰库） ==================

    private void validate(LoanApplication app, List<ApplicationItem> items) {
        if (app == null) {
            throw new BusinessException("申请信息不能为空");
        }

        String activityName = trim(app.getActivityName());
        if (activityName.isEmpty()) {
            throw new BusinessException("活动名称不能为空");
        }
        if (activityName.length() > 80) {
            throw new BusinessException("活动名称不能超过 80 个字");
        }

        String purpose = trim(app.getPurpose());
        if (purpose.length() < 5) {
            throw new BusinessException("用途至少填写 5 个字");
        }
        if (purpose.length() > 300) {
            throw new BusinessException("用途不能超过 300 个字");
        }

        LocalDate start = parseDate(app.getExpectedStart(), "预计开始日期");
        LocalDate ret = parseDate(app.getExpectedReturn(), "预计归还日期");
        if (start == null || ret == null) {
            throw new BusinessException("预计开始日期和预计归还日期都必须填写");
        }
        if (ret.isBefore(start)) {
            throw new BusinessException("预计归还日期不能早于预计开始日期");
        }

        String token = trim(app.getClientToken());
        if (token.isEmpty()) {
            throw new BusinessException("表单已失效，请刷新页面重新填写");
        }

        if (items == null || items.isEmpty()) {
            throw new BusinessException("至少填写一行物资");
        }

        // 同一物资不能出现两行；数量必须 1–999
        Set<Integer> seen = new HashSet<>();
        List<String> ids = new ArrayList<>();
        for (ApplicationItem item : items) {
            if (item.getMaterialId() == null) {
                throw new BusinessException("物资不能为空");
            }
            if (!seen.add(item.getMaterialId())) {
                throw new BusinessException("同一物资请合并成一行");
            }
            Integer qty = item.getQuantity();
            if (qty == null) {
                throw new BusinessException("物资数量不能为空");
            }
            if (qty < 1 || qty > 999) {
                throw new BusinessException("物资数量必须是 1–999 的整数");
            }
            ids.add(String.valueOf(item.getMaterialId()));
        }
    }

    /* 提示性库存校验：数量不得超过当前可用量 */
    private void checkAvailable(Connection conn, List<ApplicationItem> items) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        for (ApplicationItem item : items) {
            ids.add(item.getMaterialId());
        }
        Map<Integer, Integer> availableMap = materialService.loadAvailableByIds(ids);
        Map<Integer, String> nameMap = materialService.loadNameByIds(ids);
        for (ApplicationItem item : items) {
            Integer available = availableMap.get(item.getMaterialId());
            String name = nameMap.getOrDefault(item.getMaterialId(), "编号 " + item.getMaterialId());
            if (available == null) {
                throw new BusinessException("物资「" + name + "」不存在或已停用");
            }
            if (item.getQuantity() > available) {
                throw new BusinessException("物资「" + name + "」当前可用 " + available + "，不能借出 "
                        + item.getQuantity());
            }
        }
    }

    private LocalDate parseDate(String value, String label) {
        String v = trim(value);
        if (v.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException e) {
            throw new BusinessException(label + "格式不正确，应为 yyyy-MM-dd");
        }
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
