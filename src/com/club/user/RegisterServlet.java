package com.club.user;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.club.common.BusinessException;

/**
 * 注册。
 *
 * <p>GET 打开注册页，POST 提交注册：
 * 注册出来的账号一律是社团成员（MEMBER），管理员账号不开放自助注册。
 * 成功之后不自动登录，跳回登录页让本人用新密码登一次，确认密码没记错。</p>
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String realName = request.getParameter("realName");
        String password = request.getParameter("password");
        String confirm = request.getParameter("confirmPassword");

        try {
            authService.register(username, realName, password, confirm);
            response.sendRedirect(request.getContextPath() + "/login?msg=register");
        } catch (BusinessException e) {
            // 注册失败时把已填的学号和姓名带回去，密码不回显
            request.setAttribute("error", e.getMessage());
            request.setAttribute("username", username);
            request.setAttribute("realName", realName);
            request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
        }
    }
}
