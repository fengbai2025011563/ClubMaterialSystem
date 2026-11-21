<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>我的申请 - 社团活动物资借还系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/material.css">
</head>
<body>

<%@ include file="/jsp/common/header.jsp" %>

<div class="wrap">
    <div class="page-head">
        <div>
            <h1>我的申请</h1>
            <div class="sub">查看每张申请的进度，待审批的可以撤回</div>
        </div>
        <a class="btn-primary" href="${pageContext.request.contextPath}/applications/new">+ 新建申请</a>
    </div>

    <%-- 提示消息：stu3 的 nav.jsp 到位后由它统一显示并清除 --%>
    <c:if test="${not empty sessionScope.flashMsg}">
        <div class="flash ${sessionScope.flashType}">${sessionScope.flashMsg}</div>
        <c:remove var="flashMsg" scope="session"/>
        <c:remove var="flashType" scope="session"/>
    </c:if>

    <div class="toolbar">
        <a class="btn" href="${pageContext.request.contextPath}/applications">全部</a>
        <a class="btn" href="${pageContext.request.contextPath}/applications?status=PENDING">待审批</a>
        <a class="btn" href="${pageContext.request.contextPath}/applications?status=APPROVED">已批准</a>
        <a class="btn" href="${pageContext.request.contextPath}/applications?status=BORROWED">已借出</a>
        <a class="btn" href="${pageContext.request.contextPath}/applications?status=RETURNED">已归还</a>
        <a class="btn" href="${pageContext.request.contextPath}/applications?status=REJECTED">已驳回</a>
        <a class="btn" href="${pageContext.request.contextPath}/applications?status=CANCELLED">已取消</a>
    </div>

    <div class="card">
        <c:choose>
            <c:when test="${empty list}">
                <div class="empty">
                    <strong>还没有申请</strong>
                    点右上角“新建申请”，选择物资并填写活动信息即可提交。
                </div>
            </c:when>
            <c:otherwise>
                <table class="list">
                    <thead>
                    <tr>
                        <th class="num">编号</th>
                        <th>活动名称</th>
                        <th>物资种类数</th>
                        <th>状态</th>
                        <th>申请时间</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="app" items="${list}">
                        <tr>
                            <td class="num">${app.id}</td>
                            <td class="name-cell">${app.activityName}</td>
                            <td>${app.itemTypes}</td>
                            <td><span class="status ${app.status}">${app.statusText}</span></td>
                            <td class="muted">${app.applyTimeText}</td>
                            <td class="actions">
                                <a class="link-btn" href="${pageContext.request.contextPath}/applications/detail?id=${app.id}">详情</a>
                                <c:if test="${app.status == 'PENDING'}">
                                    <form class="inline-form" method="post"
                                          action="${pageContext.request.contextPath}/applications/cancel"
                                          onsubmit="return confirm('确认撤回此申请？');">
                                        <input type="hidden" name="id" value="${app.id}">
                                        <button type="submit" class="link-btn danger">撤回</button>
                                    </form>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </div>
</div>

</body>
</html>
