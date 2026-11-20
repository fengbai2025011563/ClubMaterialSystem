package com.club.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.club.common.DBUtil;

public class UserDao {

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