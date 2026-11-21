<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>申请详情 - 社团活动物资借还系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/material.css">
</head>
<body>

<%@ include file="/jsp/common/header.jsp" %>

<div class="wrap">
    <div class="page-head">
        <div>
            <h1>申请详情</h1>
            <div class="sub">查看申请内容、物资明细和审批流转记录</div>
        </div>
        <a class="btn" href="${pageContext.request.contextPath}/applications">返回我的申请</a>
    </div>

    <c:choose>
        <c:when test="${not empty error}">
            <%-- 非本人访问：不泄露任何单头内容 --%>
            <div class="msg-error">${error}</div>
        </c:when>
        <c:otherwise>
            <div class="card">
                <table class="list">
                    <tr><th style="width:120px">编号</th><td>${app.id}</td></tr>
                    <tr><th>活动名称</th><td class="name-cell">${app.activityName}</td></tr>
                    <tr><th>用途</th><td>${app.purpose}</td></tr>
                    <tr><th>预计开始</th><td>${app.expectedStart}</td></tr>
                    <tr><th>预计归还</th><td>${app.expectedReturn}</td></tr>
                    <tr><th>状态</th><td><span class="status ${app.status}">${app.statusText}</span></td></tr>
                    <tr><th>申请时间</th><td>${app.applyTimeText}</td></tr>
                    <tr><th>审批意见</th><td>${empty app.reviewRemark ? '—' : app.reviewRemark}</td></tr>
                    <tr><th>审批时间</th><td>${empty app.reviewTimeText ? '—' : app.reviewTimeText}</td></tr>
                    <tr><th>借出时间</th><td>${empty app.borrowTimeText ? '—' : app.borrowTimeText}</td></tr>
                    <tr><th>归还时间</th><td>${empty app.returnTimeText ? '—' : app.returnTimeText}</td></tr>
                </table>
            </div>

            <div class="section-title">物资明细</div>
            <div class="card">
                <c:choose>
                    <c:when test="${empty items}">
                        <div class="empty">这张申请没有物资明细。</div>
                    </c:when>
                    <c:otherwise>
                        <table class="list">
                            <thead>
                            <tr>
                                <th>物资</th>
                                <th class="num">数量</th>
                                <th>单位</th>
                                <th class="num">损坏</th>
                                <th class="num">遗失</th>
                                <th>备注</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="it" items="${items}">
                                <tr>
                                    <td class="name-cell">${it.materialName}</td>
                                    <td class="num">${it.quantity}</td>
                                    <td>${it.unit}</td>
                                    <td class="num">${it.damagedQty}</td>
                                    <td class="num">${it.lostQty}</td>
                                    <td class="muted">${it.remark}</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>

            <c:if test="${app.status == 'PENDING'}">
                <form method="post" action="${pageContext.request.contextPath}/applications/cancel"
                      onsubmit="return confirm('确认撤回此申请？');">
                    <input type="hidden" name="id" value="${app.id}">
                    <button type="submit" class="btn-danger">撤回申请</button>
                </form>
            </c:if>
        </c:otherwise>
    </c:choose>
</div>

</body>
</html>
