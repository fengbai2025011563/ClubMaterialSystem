package com.club.user;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录过滤器：除登录页与静态资源外，所有请求都必须先登录。
 *
 * <p>角色判断不在这里做：物资模块的 MaterialServlet.requireAdmin()
 * 已经会拦非管理员，后面的审批模块也会在 Service 里复核。</p>
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI().substring(req.getContextPath().length());

        // 登录页和静态资源（样式、脚本、图片）不拦截，否则登录页的背景图会被重定向成 302
        if (path.equals("/login")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.equals("/favicon.ico")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");

        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        chain.doFilter(request, response);
    }
}
