<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, com.club.application.bean.LoanApplication, com.club.application.bean.ApplicationItem, com.club.common.StatusUtil, com.club.common.HtmlUtil" %>
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
    LoanApplication app = (LoanApplication) request.getAttribute("app");
    List<ApplicationItem> items = (List<ApplicationItem>) request.getAttribute("items");
    String status = app.getStatus();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <title>申请详情 - 社团活动物资借还系统</title>
  <link rel="stylesheet" href="<%=ctx%>/css/material.css">
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

<div class="wrap">
  <div class="page-head">
    <h1>申请 #<%= app.getId() %></h1>
    <div class="sub">
      申请人：<%= HtmlUtil.escape(app.getApplicantName()) %> ·
      状态：<%= StatusUtil.label(status) %>
    </div>
  </div>

  <div class="card">
    <table class="list">
      <tr><th>活动名称</th><td><%= HtmlUtil.escape(app.getActivityName()) %></td></tr>
      <tr><th>借用用途</th><td><%= HtmlUtil.escape(app.getPurpose()) %></td></tr>
      <tr><th>预计日期</th><td><%= app.getExpectedStart() %> 至 <%= app.getExpectedReturn() %></td></tr>
      <tr><th>申请时间</th><td><%= fmt(app.getApplyTime()) %></td></tr>
      <tr><th>审批时间</th><td><%= fmt(app.getReviewTime()) %></td></tr>
      <tr><th>审批意见</th><td><%= HtmlUtil.escape(app.getReviewRemark()) %></td></tr>
      <tr><th>借出时间</th><td><%= fmt(app.getBorrowTime()) %></td></tr>
      <tr><th>归还时间</th><td><%= fmt(app.getReturnTime()) %></td></tr>
    </table>
  </div>

  <div class="card">
    <table class="list">
      <tr>
        <th>物资</th>
        <th>单位</th>
        <th class="num">申请数量</th>
        <th class="num">损坏</th>
        <th class="num">遗失</th>
      </tr>
      <% if (items != null) {
           for (ApplicationItem item : items) { %>
      <tr>
        <td class="name-cell"><%= HtmlUtil.escape(item.getMaterialName()) %></td>
        <td><%= HtmlUtil.escape(item.getUnit()) %></td>
        <td class="num"><%= item.getQuantity() %></td>
        <td class="num"><%= item.getDamagedQty() == null ? 0 : item.getDamagedQty() %></td>
        <td class="num"><%= item.getLostQty() == null ? 0 : item.getLostQty() %></td>
      </tr>
      <%     }
         } %>
    </table>
  </div>

  <% if (LoanApplication.PENDING.equals(status)) { %>
  <div class="card">
    <form method="post" action="<%=ctx%>/admin/applications/review">
      <input type="hidden" name="id" value="<%= app.getId() %>">
      <div class="toolbar">
        <label for="remark">审批意见</label>
        <input type="text" id="remark" name="remark" class="grow" maxlength="200"
               placeholder="驳回时建议写明原因">
      </div>
      <button type="submit" class="btn-primary" name="action" value="approve">通过并占用库存</button>
      <button type="submit" class="btn-danger" name="action" value="reject">驳回</button>
    </form>
  </div>
  <% } else if (LoanApplication.APPROVED.equals(status)) { %>
  <div class="card">
    <form method="post" action="<%=ctx%>/admin/loans/borrow" style="display:inline">
      <input type="hidden" name="id" value="<%= app.getId() %>">
      <button type="submit" class="btn-primary">确认借出</button>
    </form>
    <form method="post" action="<%=ctx%>/admin/applications/review" style="display:inline;margin-left:12px">
      <input type="hidden" name="id" value="<%= app.getId() %>">
      <input type="hidden" name="action" value="void">
      <input type="text" name="remark" placeholder="作废原因（可空）" maxlength="200">
      <button type="submit" class="btn-danger">作废并释放预留</button>
    </form>
  </div>
  <% } else if (LoanApplication.BORROWED.equals(status)) { %>
  <div class="card">
    <a class="btn btn-primary" href="<%=ctx%>/admin/loans/return?id=<%= app.getId() %>">去归还验收</a>
  </div>
  <% } %>
</div>
</body>
</html>
