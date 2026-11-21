<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, com.club.application.bean.LoanApplication, com.club.application.bean.ApplicationItem, com.club.common.HtmlUtil" %>
<%
    String ctx = request.getContextPath();
    LoanApplication app = (LoanApplication) request.getAttribute("app");
    List<ApplicationItem> items = (List<ApplicationItem>) request.getAttribute("items");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <title>归还验收 - 社团活动物资借还系统</title>
  <link rel="stylesheet" href="<%=ctx%>/css/material.css">
</head>
<body>
<div class="topbar">
  <div class="topbar-inner">
    <div class="brand">社团活动物资借还系统<span>归还验收</span></div>
    <div style="margin-left:auto">
      <a href="<%=ctx%>/admin/applications?status=BORROWED">待归还</a>
      <a href="<%=ctx%>/logout">退出</a>
    </div>
  </div>
</div>

<div class="wrap">
  <div class="page-head">
    <h1>归还验收 · 申请 #<%= app.getId() %></h1>
    <div class="sub">损坏与遗失从库存总量中核销，不再回到可用量；完好数量回补可用量。</div>
  </div>

  <form method="post" action="<%=ctx%>/admin/loans/return">
    <input type="hidden" name="id" value="<%= app.getId() %>">

    <div class="card">
      <table class="list">
        <tr>
          <th>物资</th>
          <th>单位</th>
          <th class="num">申请数量</th>
          <th class="num">损坏数量</th>
          <th class="num">遗失数量</th>
        </tr>
        <% for (ApplicationItem item : items) {
               int qty = item.getQuantity(); %>
        <tr>
          <td class="name-cell"><%= HtmlUtil.escape(item.getMaterialName()) %></td>
          <td><%= HtmlUtil.escape(item.getUnit()) %></td>
          <td class="num"><%= qty %></td>
          <td class="num">
            <input type="hidden" name="itemId" value="<%= item.getId() %>">
            <input class="qty-input" type="number" name="damagedQty" value="0" min="0" max="<%= qty %>">
          </td>
          <td class="num">
            <input class="qty-input" type="number" name="lostQty" value="0" min="0" max="<%= qty %>">
          </td>
        </tr>
        <% } %>
      </table>
    </div>

    <p>
      <button type="submit" class="btn-primary">提交归还</button>
      <a class="btn" href="<%=ctx%>/admin/applications?status=BORROWED">取消</a>
    </p>
  </form>
</div>
</body>
</html>
