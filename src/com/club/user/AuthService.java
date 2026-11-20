package com.club.user;

import java.sql.Connection;
import java.sql.SQLException;

import com.club.common.BusinessException;
import com.club.common.DBUtil;
import com.club.common.PasswordUtil;

public class AuthService {

    private final UserDao userDao = new UserDao();

    public User login(String username, String rawPassword) {
        if (username == null || username.trim().isEmpty()
                || rawPassword == null || rawPassword.isEmpty()) {
            throw new BusinessException("用户名或密码错误");
        }

        try (Connection conn = DBUtil.getConnection()) {
            User user = userDao.findByUsername(conn, username.trim());

            if (user == null || !PasswordUtil.matches(rawPassword, user.getPassword())) {
                throw new BusinessException("用户名或密码错误");
            }
            if (user.getStatus() != 1) {
                throw new BusinessException("用户名或密码错误");
            }

            user.setPassword(null);
            return user;
        } catch (SQLException e) {
            throw new BusinessException("登录失败，请稍后重试", e);
        }
    }
}