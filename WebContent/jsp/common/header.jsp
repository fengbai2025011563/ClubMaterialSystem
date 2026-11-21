<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.club.user.User" %>
<%--
  全站统一头部：蓝色品牌栏 + 白色导航
  用 <%@ include file="/jsp/common/header.jsp" %> 引入。
  导航只放真实存在的页面：物资浏览、我的申请，管理员多两项。
--%>
<%
    String navCtx = request.getContextPath();
    User navUser = (User) session.getAttribute("loginUser");
    String navPath = request.getRequestURI();
    if (navPath.startsWith(navCtx)) {
        navPath = navPath.substring(navCtx.length());
    }
    boolean navAdmin = navUser != null && navUser.isAdmin();
%>
<div class="topbar">
    <div class="topbar-inner">
        <a class="brand" href="<%=navCtx%>/material/list">
            <span class="brand-name">社团活动物资借还系统</span>
            <span class="brand-sub">北京信息科技大学 · 社团物资借用与归还</span>
        </a>
        <div class="topbar-links">
            <% if (navUser != null) { %>
            <span class="who"><%= com.club.common.HtmlUtil.escape(navUser.getRealName()) %>（<%= navUser.getRoleText() %>）</span>
            <a href="<%=navCtx%>/logout">退出</a>
            <% } %>
        </div>
    </div>
    <nav class="main-nav">
        <ul>
            <li class="<%= navPath.startsWith("/material/list") ? "active" : "" %>">
                <a href="<%=navCtx%>/material/list">物资浏览</a>
            </li>
            <% if (navUser != null && !navAdmin) { %>
            <li class="<%= navPath.startsWith("/applications") ? "active" : "" %>">
                <a href="<%=navCtx%>/applications">我的申请</a>
            </li>
            <% } %>
            <% if (navAdmin) { %>
            <li class="<%= navPath.startsWith("/admin/applications") ? "active" : "" %>">
                <a href="<%=navCtx%>/admin/applications">审批管理</a>
            </li>
            <li class="<%= navPath.startsWith("/material/admin") || navPath.startsWith("/material/form") ? "active" : "" %>">
                <a href="<%=navCtx%>/material/admin">物资管理</a>
            </li>
            <% } %>
        </ul>
    </nav>
</div>
