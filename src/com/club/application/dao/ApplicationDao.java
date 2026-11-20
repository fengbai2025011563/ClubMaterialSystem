package com.club.application.dao;

import com.club.application.bean.ApplicationItem;
import com.club.application.bean.LoanApplication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 申请单 DAO。
 * 契约：所有方法第一个参数都是 Connection；DAO 内禁止 commit / close / DriverManager。
 * stu3调 findById 和 findItems，不要改名字、参数顺序、返回类型。
 */
public class ApplicationDao {

    /* 插入单头，返回自增主键；client_token 重复会抛 SQLIntegrityConstraintViolationException */
    public int insert(Connection conn, LoanApplication app) throws SQLException {
        String sql = "INSERT INTO loan_applications "
                + "(applicant_id, activity_name, purpose, expected_start, expected_return, status, client_token, apply_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, app.getApplicantId());
            ps.setString(2, app.getActivityName());
            ps.setString(3, app.getPurpose());
            ps.setString(4, app.getExpectedStart());
            ps.setString(5, app.getExpectedReturn());
            ps.setString(6, app.getStatus() == null ? LoanApplication.PENDING : app.getStatus());
            ps.setString(7, app.getClientToken());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    app.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("插入申请单失败：未取得自增主键");
    }

    /* 批量插入明细；任何一条失败由调用方回滚 */
    public void insertItems(Connection conn, int applicationId, List<ApplicationItem> items) throws SQLException {
        if (items == null || items.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO application_items "
                + "(application_id, material_id, quantity, damaged_qty, lost_qty, remark) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ApplicationItem item : items) {
                ps.setInt(1, applicationId);
                ps.setInt(2, item.getMaterialId());
                ps.setInt(3, item.getQuantity());
                ps.setInt(4, item.getDamagedQty() == null ? 0 : item.getDamagedQty());
                ps.setInt(5, item.getLostQty() == null ? 0 : item.getLostQty());
                ps.setString(6, item.getRemark());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /* 按申请人查列表，status 为 null 表示全部；按 id 倒序 */
    public List<LoanApplication> findByApplicant(Connection conn, int applicantId, String status) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT a.id, a.applicant_id, a.activity_name, a.purpose, a.expected_start, a.expected_return, "
                        + "a.status, a.client_token, a.apply_time, a.reviewer_id, a.review_time, a.review_remark, "
                        + "a.borrow_time, a.return_time, "
                        + "(SELECT COUNT(*) FROM application_items i WHERE i.application_id = a.id) AS item_types "
                        + "FROM loan_applications a WHERE a.applicant_id = ?");
        boolean byStatus = status != null && !status.trim().isEmpty();
        if (byStatus) {
            sql.append(" AND a.status = ?");
        }
        sql.append(" ORDER BY a.id DESC");

        List<LoanApplication> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, applicantId);
            if (byStatus) {
                ps.setString(2, status.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LoanApplication app = mapRow(rs);
                    app.setItemTypes(rs.getInt("item_types"));
                    list.add(app);
                }
            }
        }
        return list;
    }

    /* 查单头（不含明细），查不到返回 null */
    public LoanApplication findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT id, applicant_id, activity_name, purpose, expected_start, expected_return, "
                + "status, client_token, apply_time, reviewer_id, review_time, review_remark, borrow_time, return_time "
                + "FROM loan_applications WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /* 查明细并 join materials 取 name/unit；stu3 的审批/借出/归还也调用它（只读） */
    public List<ApplicationItem> findItems(Connection conn, int applicationId) throws SQLException {
        String sql = "SELECT i.id, i.application_id, i.material_id, i.quantity, i.damaged_qty, i.lost_qty, i.remark, "
                + "m.name AS material_name, m.unit AS unit "
                + "FROM application_items i LEFT JOIN materials m ON m.id = i.material_id "
                + "WHERE i.application_id = ? ORDER BY i.id";
        List<ApplicationItem> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ApplicationItem item = new ApplicationItem();
                    item.setId(rs.getInt("id"));
                    item.setApplicationId(rs.getInt("application_id"));
                    item.setMaterialId(rs.getInt("material_id"));
                    item.setMaterialName(rs.getString("material_name"));
                    item.setUnit(rs.getString("unit"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setDamagedQty(rs.getInt("damaged_qty"));
                    item.setLostQty(rs.getInt("lost_qty"));
                    item.setRemark(rs.getString("remark"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    /* 本人撤回：UPDATE ... SET status='CANCELLED' WHERE id=? AND applicant_id=? AND status='PENDING'
       返回 1 成功，0 表示状态已变或不是本人 */
    public int cancel(Connection conn, int id, int applicantId) throws SQLException {
        String sql = "UPDATE loan_applications SET status = ? "
                + "WHERE id = ? AND applicant_id = ? AND status = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, LoanApplication.CANCELLED);
            ps.setInt(2, id);
            ps.setInt(3, applicantId);
            ps.setString(4, LoanApplication.PENDING);
            return ps.executeUpdate();
        }
    }

    /* 结果集 -> 单头 bean */
    private LoanApplication mapRow(ResultSet rs) throws SQLException {
        LoanApplication app = new LoanApplication();
        app.setId(rs.getInt("id"));
        app.setApplicantId(rs.getInt("applicant_id"));
        app.setActivityName(rs.getString("activity_name"));
        app.setPurpose(rs.getString("purpose"));
        app.setExpectedStart(rs.getString("expected_start"));
        app.setExpectedReturn(rs.getString("expected_return"));
        app.setStatus(rs.getString("status"));
        app.setClientToken(rs.getString("client_token"));
        app.setApplyTime(rs.getTimestamp("apply_time"));
        int reviewerId = rs.getInt("reviewer_id");
        app.setReviewerId(rs.wasNull() ? null : reviewerId);
        app.setReviewTime(rs.getTimestamp("review_time"));
        app.setReviewRemark(rs.getString("review_remark"));
        app.setBorrowTime(rs.getTimestamp("borrow_time"));
        app.setReturnTime(rs.getTimestamp("return_time"));
        return app;
    }
}
