package com.club.user;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 【临时调试用，正式上线前请删除本文件】
 *
 * <p>正式登录（LoginServlet + AuthFilter）是晏思汗负责的部分。在他提交之前，
 * 物资模块没法独立运行，因为没有"当前用户"就无法判断管理员权限。
 * 所以这里做了一个不查数据库的模拟登录：直接在 session 里放一个 User 对象。</p>
 *
 * <p>用法：浏览器访问 /dev-login?as=admin 或 /dev-login?as=member，
 * 之后再去 /material/list 就能看到页面。调试登录只在 session 生命周期内有效，
 * 关闭浏览器或重启 Tomcat 就失效了。</p>
 *
 * <p>删掉这个文件后，物资模块仍然正常工作：真正登录时由 AuthFilter 往 session
 * 里放 "loginUser"，键名保持一致即可。</p>
 */
@WebServlet("/dev-login")
public class DevLoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String as = request.getParameter("as");

        User user = new User();
        if ("admin".equals(as)) {
            // 对应 data.sql 里的 1 号账号：admin / 系统管理员
            user.setId(1);
            user.setUsername("admin");
            user.setRealName("系统管理员");
            user.setRole("ADMIN");
        } else {
            // 对应 data.sql 里的 3 号账号：刘艳（普通成员），方便看"成员视角"
            user.setId(3);
            user.setUsername("2025011563");
            user.setRealName("刘艳");
            user.setRole("MEMBER");
        }

        request.getSession().setAttribute("loginUser", user);
        response.sendRedirect(request.getContextPath() + "/material/list");
    }
}
