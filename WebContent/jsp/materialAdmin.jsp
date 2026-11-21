<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.club.material.Material" %>
<%@ page import="com.club.material.MaterialServlet" %>
<%@ page import="com.club.user.User" %>
<%--
  物资管理列表：只有管理员能进。
  和浏览页的区别是：能看到停用的物资，也能看到预留、借出两个"看不见的数量"，
  并且每一行都带编辑、入库、停用/启用三个操作。
--%>
<%
    String ctx = request.getContextPath();
    List<Material> materials = (List<Material>) request.getAttribute("materials");
    List<String> categories = (List<String>) request.getAttribute("categories");
    String keyword = (String) request.getAttribute("keyword");
    String category = (String) request.getAttribute("category");
    String status = (String) request.getAttribute("status");
    User loginUser = (User) request.getAttribute("loginUser");
    String msg = request.getParameter("msg");
    String error = (String) request.getAttribute("error");
    if (status == null) {
        status = "";
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>物资管理 - 社团活动物资借还系统</title>
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
            <a href="<%=ctx%>/material/admin">物资管理</a>
            <a href="<%=ctx%>/admin/applications">申请管理</a>
            <a href="<%=ctx%>/material/list">物资浏览</a>
        </div>
    </div>
</div>

<div class="wrap">
    <div class="page-head">
        <div>
            <h1>物资管理</h1>
            <div class="sub">新增物资、补充库存、停用不再外借的物资，都在这一页完成</div>
        </div>
        <a class="btn-primary" href="<%=ctx%>/material/form">+ 新增物资</a>
    </div>

    <% if (msg != null && !msg.isEmpty()) { %>
        <div class="msg"><%= MaterialServlet.escape(msg) %></div>
    <% } %>
    <% if (error != null && !error.isEmpty()) { %>
        <div class="msg-error"><%= MaterialServlet.escape(error) %></div>
    <% } %>

    <form class="toolbar" method="get" action="<%=ctx%>/material/admin">
        <label for="keyword">名称</label>
        <input type="text" id="keyword" name="keyword" class="grow"
               placeholder="输入物资名称的一部分"
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

        <label for="status">状态</label>
        <select id="status" name="status">
            <option value="" <%= "".equals(status) ? "selected" : "" %>>全部状态</option>
            <option value="ACTIVE" <%= "ACTIVE".equals(status) ? "selected" : "" %>>可借</option>
            <option value="INACTIVE" <%= "INACTIVE".equals(status) ? "selected" : "" %>>停用</option>
        </select>

        <button type="submit" class="btn-primary">查询</button>
        <a class="btn" href="<%=ctx%>/material/admin">重置</a>
    </form>

    <div class="card">
        <% if (materials != null && !materials.isEmpty()) { %>
        <table class="list">
            <thead>
            <tr>
                <th class="num">编号</th>
                <th>物资名称</th>
                <th>分类</th>
                <th>单位</th>
                <th class="num">总量</th>
                <th class="num">可用</th>
                <th class="num">预留</th>
                <th class="num">借出</th>
                <th>状态</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <% for (Material m : materials) { %>
            <tr>
                <td class="num muted"><%= m.getId() %></td>
                <td class="name-cell"><%= MaterialServlet.escape(m.getName()) %></td>
                <td><%= MaterialServlet.escape(m.getCategory()) %></td>
                <td><%= MaterialServlet.escape(m.getUnit()) %></td>
                <td class="num"><%= m.getTotalQty() %></td>
                <td class="num"><%= m.getAvailableQty() %></td>
                <td class="num muted"><%= m.getReservedQty() %></td>
                <td class="num muted"><%= m.getBorrowedQty() %></td>
                <td>
                    <span class="pill <%= m.isActive() ? "pill-active" : "pill-inactive" %>">
                        <%= m.getStatusText() %>
                    </span>
                </td>
                <td>
                    <div class="actions">
                        <a class="link-btn" href="<%=ctx%>/material/form?id=<%=m.getId()%>">编辑</a>

                        <form class="inline-form" method="post" action="<%=ctx%>/material/stock">
                            <input type="hidden" name="id" value="<%=m.getId()%>">
                            <input type="number" class="qty-input" name="quantity"
                                   value="1" min="1" step="1" title="入库数量">
                            <button type="submit" class="link-btn">入库</button>
                        </form>

                        <form class="inline-form" method="post" action="<%=ctx%>/material/status">
                            <input type="hidden" name="id" value="<%=m.getId()%>">
                            <% if (m.isActive()) { %>
                                <input type="hidden" name="status" value="INACTIVE">
                                <button type="submit" class="link-btn danger">停用</button>
                            <% } else { %>
                                <input type="hidden" name="status" value="ACTIVE">
                                <button type="submit" class="link-btn">启用</button>
                            <% } %>
                        </form>
                    </div>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
        <% } else { %>
        <div class="empty">
            <strong>还没有物资</strong>
            点右上角“新增物资”，把社团的帐篷、桌子、音响先登记进来。
        </div>
        <% } %>
    </div>

    <p class="hint">
        读数规则：<b>总量 = 可用 + 预留 + 借出</b>，这三列任何时刻都必须加起来等于总量。<br>
        数量只能通过“入库”“审批通过”“确认借出”“归还”这些动作变化，编辑表单里改不了数量，
        这样每一次变化都会留下一条流水，月底对账才不会对不上。
    </p>
</div>

</body>
</html>
