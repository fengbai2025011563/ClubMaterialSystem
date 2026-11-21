<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>我的申请</title>
    <style>
        body { font-family: "Microsoft YaHei", sans-serif; margin: 24px; color: #1f2937; }
        h2 { border-left: 4px solid #0e7490; padding-left: 10px; }
        table { border-collapse: collapse; width: 100%; margin-top: 12px; }
        th, td { border: 1px solid #d8dee7; padding: 7px 10px; text-align: left; font-size: 14px; }
        th { background: #eef3f7; }
        .empty { color: #5b6472; padding: 20px 0; }
        a { color: #0e7490; text-decoration: none; }
        a:hover { text-decoration: underline; }
        .btn-cancel { color: #b91c1c; border: none; background: none; cursor: pointer; font-size: 13px; }
        .status { font-size: 12px; padding: 2px 8px; border-radius: 4px; }
        .PENDING   { background: #fef3c7; color: #92400e; }
        .APPROVED  { background: #eff6ff; color: #1e40af; }
        .REJECTED  { background: #fee2e2; color: #991b1b; }
        .BORROWED  { background: #f5f3ff; color: #5b21b6; }
        .RETURNED  { background: #ecfdf5; color: #065f46; }
        .CANCELLED { background: #f3f4f6; color: #374151; }
        .flash { padding: 8px 12px; border-radius: 4px; margin-bottom: 12px; font-size: 14px; }
        .flash.success { background: #ecfdf5; color: #065f46; border: 1px solid #a7f3d0; }
        .flash.error { background: #fef2f2; color: #991b1b; border: 1px solid #fecaca; }
    </style>
</head>
<body>

<h2>我的申请</h2>

<%-- 提示消息：stu3 的 nav.jsp 到位后由它统一显示并清除 --%>
<c:if test="${not empty sessionScope.flashMsg}">
    <div class="flash ${sessionScope.flashType}">${sessionScope.flashMsg}</div>
    <c:remove var="flashMsg" scope="session"/>
    <c:remove var="flashType" scope="session"/>
</c:if>

<div style="margin-bottom: 10px;">
    状态：
    <a href="${pageContext.request.contextPath}/applications">全部</a> |
    <a href="${pageContext.request.contextPath}/applications?status=PENDING">待审批</a> |
    <a href="${pageContext.request.contextPath}/applications?status=APPROVED">已批准</a> |
    <a href="${pageContext.request.contextPath}/applications?status=BORROWED">已借出</a> |
    <a href="${pageContext.request.contextPath}/applications?status=RETURNED">已归还</a> |
    <a href="${pageContext.request.contextPath}/applications?status=REJECTED">已驳回</a> |
    <a href="${pageContext.request.contextPath}/applications?status=CANCELLED">已取消</a>
    &nbsp;&nbsp;
    <a href="${pageContext.request.contextPath}/applications/new" style="font-weight:bold;">+ 新建申请</a>
    &nbsp;&nbsp;
    <a href="${pageContext.request.contextPath}/material/list">物资库</a>
</div>

<c:choose>
    <c:when test="${empty list}">
        <p class="empty">还没有申请。<a href="${pageContext.request.contextPath}/applications/new">去申请 →</a></p>
    </c:when>
    <c:otherwise>
        <table>
            <thead>
            <tr>
                <th>编号</th><th>活动名称</th><th>物资种类数</th><th>状态</th><th>申请时间</th><th>操作</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="app" items="${list}">
                <tr>
                    <td>${app.id}</td>
                    <td>${app.activityName}</td>
                    <td>${app.itemTypes}</td>
                    <td><span class="status ${app.status}">${app.statusText}</span></td>
                    <td>${app.applyTimeText}</td>
                    <td>
                        <a href="${pageContext.request.contextPath}/applications/detail?id=${app.id}">详情</a>
                        <c:if test="${app.status == 'PENDING'}">
                            <form style="display:inline" method="post"
                                  action="${pageContext.request.contextPath}/applications/cancel"
                                  onsubmit="return confirm('确认撤回此申请？');">
                                <input type="hidden" name="id" value="${app.id}">
                                <input type="submit" class="btn-cancel" value="撤回">
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>

</body>
</html>
