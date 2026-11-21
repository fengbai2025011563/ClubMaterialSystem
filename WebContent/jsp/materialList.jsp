<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.club.material.Material" %>
<%@ page import="com.club.material.MaterialServlet" %>
<%@ page import="com.club.user.User" %>
<%--
  物资浏览页：谁都能看，只显示"可借"状态的物资。
  数据由 MaterialServlet 的 showBrowseList 方法放进 request，这里只负责展示。
--%>
<%
    String ctx = request.getContextPath();
    List<Material> materials = (List<Material>) request.getAttribute("materials");
    List<String> categories = (List<String>) request.getAttribute("categories");
    String keyword = (String) request.getAttribute("keyword");
    String category = (String) request.getAttribute("category");
    User loginUser = (User) request.getAttribute("loginUser");
    String msg = request.getParameter("msg");
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>物资浏览 - 社团活动物资借还系统</title>
    <link rel="stylesheet" href="<%=ctx%>/css/material.css">
</head>
<body>

<div class="topbar">
    <div class="topbar-inner">
        <div class="brand">社团活动物资借还系统<span>物资库</span></div>
        <div>
            <% if (loginUser != null) { %>
                <span style="font-size:13px;color:#d9efe7;">
                    <%= MaterialServlet.escape(loginUser.getRealName()) %>（<%= loginUser.getRoleText() %>）
                </span>
            <% } %>
            <% if (loginUser != null && loginUser.isAdmin()) { %>
                <a href="<%=ctx%>/material/admin">物资管理</a>
                <a href="<%=ctx%>/admin/applications">申请管理</a>
            <% } else if (loginUser != null) { %>
                <a href="<%=ctx%>/applications">我的申请</a>
            <% } %>
            <a href="<%=ctx%>/material/list">物资浏览</a>
        </div>
    </div>
</div>

<div class="wrap">
    <div class="page-head">
        <div>
            <h1>物资浏览</h1>
            <div class="sub">填写借用申请前，先在这里确认有没有、够不够借</div>
        </div>
    </div>

    <% if (msg != null && !msg.isEmpty()) { %>
        <div class="msg"><%= MaterialServlet.escape(msg) %></div>
    <% } %>
    <% if (error != null && !error.isEmpty()) { %>
        <div class="msg-error"><%= MaterialServlet.escape(error) %></div>
    <% } %>

    <form class="toolbar" method="get" action="<%=ctx%>/material/list">
        <label for="keyword">名称</label>
        <input type="text" id="keyword" name="keyword" class="grow"
               placeholder="输入物资名称的一部分，例如 帐篷"
               value="<%= MaterialServlet.escape(keyword) %>">

        <label for="category">分类</label>
        <select id="category" name="category">
            <option value="">全部分类</option>
            <% if (categories != null) {
                for (String item : categories) {
                    boolean selected = item.equals(category);
            %>
                <option value="<%= MaterialServlet.escape(item) %>" <%= selected ? "selected" : "" %>>
                    <%= MaterialServlet.escape(item) %>
                </option>
            <%  }
               } %>
        </select>

        <button type="submit" class="btn-primary">查询</button>
        <a class="btn" href="<%=ctx%>/material/list">重置</a>
    </form>

    <div class="card">
        <% if (materials != null && !materials.isEmpty()) { %>
        <table class="list">
            <thead>
            <tr>
                <th>物资名称</th>
                <th>分类</th>
                <th>单位</th>
                <th class="num">可借数量</th>
                <th class="num">库存总量</th>
                <th>状态</th>
                <th>存放备注</th>
            </tr>
            </thead>
            <tbody>
            <% for (Material m : materials) { %>
            <tr>
                <td class="name-cell"><%= MaterialServlet.escape(m.getName()) %></td>
                <td><%= MaterialServlet.escape(m.getCategory()) %></td>
                <td><%= MaterialServlet.escape(m.getUnit()) %></td>
                <td class="num"><%= m.getAvailableQty() %></td>
                <td class="num muted"><%= m.getTotalQty() %></td>
                <td><span class="pill pill-active"><%= m.getStatusText() %></span></td>
                <td class="muted"><%= MaterialServlet.escape(m.getRemark()) %></td>
            </tr>
            <% } %>
            </tbody>
        </table>
        <% } else { %>
        <div class="empty">
            <strong>没有找到符合条件的物资</strong>
            换个关键字试试，或者点“重置”看看全部物资。
        </div>
        <% } %>
    </div>

    <p class="hint">
        小提示：<b>可借数量</b>才是现在真正能借走的数量；<b>库存总量</b>由“可用＋预留＋借出”三部分组成，
        已经被审批占用或者正在别人手里的部分不会出现在可借数量里。
    </p>
</div>

</body>
</html>
