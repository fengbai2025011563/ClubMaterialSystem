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

    /**
     * 注册一个社团成员账号。
     *
     * <p>注册只产生 MEMBER：管理员必须由管理员在数据库里开，
     * 免得任何人注册一下就拿到审批权限。</p>
     */
    public void register(String username, String realName, String rawPassword, String confirmPassword) {
        String account = username == null ? "" : username.trim();
        String name = realName == null ? "" : realName.trim();

        if (!account.matches("[A-Za-z0-9]{4,20}")) {
            throw new BusinessException("学号/账号需为 4-20 位字母或数字");
        }
        if (name.length() < 2 || name.length() > 20) {
            throw new BusinessException("姓名需为 2-20 个字符");
        }
        if (rawPassword == null || rawPassword.length() < 6 || rawPassword.length() > 32) {
            throw new BusinessException("密码长度需为 6-32 位");
        }
        if (!rawPassword.equals(confirmPassword)) {
            throw new BusinessException("两次输入的密码不一致");
        }

        try (Connection conn = DBUtil.getConnection()) {
            // 先查一次：重名时能给出好理解的提示，而不是让数据库报唯一键错误
            if (userDao.findByUsername(conn, account) != null) {
                throw new BusinessException("该学号已注册，请直接登录");
            }

            User user = new User();
            user.setUsername(account);
            user.setPassword(PasswordUtil.sha256(rawPassword));
            user.setRealName(name);
            user.setRole("MEMBER");
            userDao.insert(conn, user);
        } catch (SQLException e) {
            // 两个同学同时提交同一个学号时，唯一索引会兜底
            if ("23000".equals(e.getSQLState())) {
                throw new BusinessException("该学号已注册，请直接登录");
            }
            throw new BusinessException("注册失败，请稍后重试", e);
        }
    }
}