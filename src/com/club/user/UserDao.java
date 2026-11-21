package com.club.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.club.common.DBUtil;

public class UserDao {

    /**
     * 新增一个成员账号。
     * 只写注册页收集的四样东西：学号、密码摘要、姓名、角色；
     * 角色固定 MEMBER，管理员账号不允许自助注册。
     */
    public int insert(Connection conn, User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, real_name, role, status) "
                   + "VALUES (?, ?, ?, ?, 1)";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRealName());
            ps.setString(4, user.getRole());
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : 0;
        } finally {
            DBUtil.close(null, ps, rs);
        }
    }

    public User findByUsername(Connection conn, String username) throws SQLException {
        String sql = "SELECT id, username, password, real_name, role, status "
                   + "FROM users WHERE username = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                return toUser(rs);
            }
            return null;
        } finally {
            DBUtil.close(null, ps, rs);
        }
    }

    private User toUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setRealName(rs.getString("real_name"));
        u.setRole(rs.getString("role"));
        u.setStatus(rs.getInt("status"));
        return u;
    }
}