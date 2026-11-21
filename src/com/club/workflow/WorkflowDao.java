package com.club.workflow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.club.application.bean.LoanApplication;

/**
 * 审批 / 借出 / 归还用到的 SQL。
 *
 * <p>状态迁移全部写成“带旧状态条件的 UPDATE”，返回影响行数：
 * 1 = 抢到处理权，0 = 已被别人处理。</p>
 */
public class WorkflowDao {

    public List<LoanApplication> findByStatus(Connection conn, String status) throws SQLException {
        String sql = "SELECT a.id, a.applicant_id, a.activity_name, a.purpose, a.expected_start, a.expected_return, "
                + "a.status, a.client_token, a.apply_time, a.reviewer_id, a.review_time, a.review_remark, "
                + "a.borrow_time, a.return_time, u.real_name AS applicant_name, "
                + "(SELECT COUNT(*) FROM application_items i WHERE i.application_id = a.id) AS item_types "
                + "FROM loan_applications a JOIN users u ON u.id = a.applicant_id "
                + "WHERE a.status = ? ORDER BY a.apply_time DESC, a.id DESC";
        List<LoanApplication> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public LoanApplication findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT a.id, a.applicant_id, a.activity_name, a.purpose, a.expected_start, a.expected_return, "
                + "a.status, a.client_token, a.apply_time, a.reviewer_id, a.review_time, a.review_remark, "
                + "a.borrow_time, a.return_time, u.real_name AS applicant_name, "
                + "(SELECT COUNT(*) FROM application_items i WHERE i.application_id = a.id) AS item_types "
                + "FROM loan_applications a JOIN users u ON u.id = a.applicant_id "
                + "WHERE a.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public int markApproved(Connection conn, int id, int reviewerId, String remark) throws SQLException {
        String sql = "UPDATE loan_applications SET status = 'APPROVED', reviewer_id = ?, review_time = NOW(), "
                + "review_remark = ? WHERE id = ? AND status = 'PENDING'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewerId);
            ps.setString(2, remark);
            ps.setInt(3, id);
            return ps.executeUpdate();
        }
    }

    public int markRejected(Connection conn, int id, int reviewerId, String remark) throws SQLException {
        String sql = "UPDATE loan_applications SET status = 'REJECTED', reviewer_id = ?, review_time = NOW(), "
                + "review_remark = ? WHERE id = ? AND status = 'PENDING'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewerId);
            ps.setString(2, remark);
            ps.setInt(3, id);
            return ps.executeUpdate();
        }
    }

    public int markVoid(Connection conn, int id, int reviewerId, String remark) throws SQLException {
        String sql = "UPDATE loan_applications SET status = 'CANCELLED', reviewer_id = ?, review_time = NOW(), "
                + "review_remark = ? WHERE id = ? AND status = 'APPROVED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewerId);
            ps.setString(2, remark);
            ps.setInt(3, id);
            return ps.executeUpdate();
        }
    }

    public int markBorrowed(Connection conn, int id) throws SQLException {
        String sql = "UPDATE loan_applications SET status = 'BORROWED', borrow_time = NOW() "
                + "WHERE id = ? AND status = 'APPROVED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    public int markReturned(Connection conn, int id) throws SQLException {
        String sql = "UPDATE loan_applications SET status = 'RETURNED', return_time = NOW() "
                + "WHERE id = ? AND status = 'BORROWED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    public int updateItemReturn(Connection conn, int applicationId, int itemId, int damaged, int lost)
            throws SQLException {
        String sql = "UPDATE application_items SET damaged_qty = ?, lost_qty = ? "
                + "WHERE id = ? AND application_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, damaged);
            ps.setInt(2, lost);
            ps.setInt(3, itemId);
            ps.setInt(4, applicationId);
            return ps.executeUpdate();
        }
    }

    private LoanApplication map(ResultSet rs) throws SQLException {
        LoanApplication app = new LoanApplication();
        app.setId(rs.getInt("id"));
        app.setApplicantId(rs.getInt("applicant_id"));
        app.setApplicantName(rs.getString("applicant_name"));
        app.setActivityName(rs.getString("activity_name"));
        app.setPurpose(rs.getString("purpose"));
        app.setExpectedStart(rs.getString("expected_start"));
        app.setExpectedReturn(rs.getString("expected_return"));
        app.setStatus(rs.getString("status"));
        app.setClientToken(rs.getString("client_token"));
        app.setApplyTime(rs.getTimestamp("apply_time"));
        app.setReviewerId((Integer) rs.getObject("reviewer_id"));
        app.setReviewTime(rs.getTimestamp("review_time"));
        app.setReviewRemark(rs.getString("review_remark"));
        app.setBorrowTime(rs.getTimestamp("borrow_time"));
        app.setReturnTime(rs.getTimestamp("return_time"));
        app.setItemTypes(rs.getInt("item_types"));
        return app;
    }
}
