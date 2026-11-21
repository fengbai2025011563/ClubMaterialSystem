<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, com.club.application.bean.LoanApplication, com.club.common.StatusUtil, com.club.common.HtmlUtil" %>
<%!
    private String fmt(java.sql.Timestamp t) {
        if (t == null) {
            return "";
        }
        String s = t.toString();
        return s.length() >= 16 ? s.substring(0, 16) : s;
    }
%>
<%
    String ctx = request.getContextPath();
    List<LoanApplication> list = (List<LoanApplication>) request.getAttribute("list");
    String status = (String) request.getAttribute("status");
    String msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <title>申请管理 - 社团活动物资借还系统</title>
  <link rel="stylesheet" href="<%=ctx%>/css/material.css">
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

<div class="wrap">
  <div class="page-head">
    <h1>申请管理</h1>
    <div class="sub">审批通过会占用库存；借出后转为借出量；归还会结算损坏与遗失。</div>
  </div>

  <% if (msg != null && !msg.isEmpty()) { %>
    <div class="msg"><%= HtmlUtil.escape(msg) %></div>
  <% } %>

  <div class="toolbar">
    <a class="btn <%= "PENDING".equals(status) ? "btn-primary" : "" %>"
       href="<%=ctx%>/admin/applications?status=PENDING">待审批</a>
    <a class="btn <%= "APPROVED".equals(status) ? "btn-primary" : "" %>"
       href="<%=ctx%>/admin/applications?status=APPROVED">待借出</a>
    <a class="btn <%= "BORROWED".equals(status) ? "btn-primary" : "" %>"
       href="<%=ctx%>/admin/applications?status=BORROWED">待归还</a>
    <a class="btn <%= "RETURNED".equals(status) ? "btn-primary" : "" %>"
       href="<%=ctx%>/admin/applications?status=RETURNED">已归还</a>
  </div>

  <div class="card">
    <% if (list == null || list.isEmpty()) { %>
      <div class="empty">这个状态下没有申请。</div>
    <% } else { %>
    <table class="list">
      <tr>
        <th class="num">编号</th>
        <th>申请人</th>
        <th>活动</th>
        <th class="num">物资种类</th>
        <th>状态</th>
        <th>申请时间</th>
        <th>操作</th>
      </tr>
      <% for (LoanApplication a : list) { %>
      <tr>
        <td class="num"><%= a.getId() %></td>
        <td><%= HtmlUtil.escape(a.getApplicantName()) %></td>
        <td class="name-cell"><%= HtmlUtil.escape(a.getActivityName()) %></td>
        <td class="num"><%= a.getItemTypes() %></td>
        <td><span class="pill"><%= StatusUtil.label(a.getStatus()) %></span></td>
        <td class="muted"><%= fmt(a.getApplyTime()) %></td>
        <td class="actions">
          <a class="btn btn-ghost" href="<%=ctx%>/admin/applications/detail?id=<%= a.getId() %>">查看</a>
        </td>
      </tr>
      <% } %>
    </table>
    <% } %>
  </div>
</div>
</body>
</html>
