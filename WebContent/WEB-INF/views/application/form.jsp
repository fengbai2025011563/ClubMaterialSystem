<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>新建申请 - 社团活动物资借还系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/material.css">
</head>
<body>

<%@ include file="/jsp/common/header.jsp" %>

<div class="wrap">
    <div class="page-head">
        <div>
            <h1>新建申请</h1>
            <div class="sub">填写活动信息和需要借用的物资，提交后等待管理员审批</div>
        </div>
        <a class="btn" href="${pageContext.request.contextPath}/applications">返回我的申请</a>
    </div>

    <c:if test="${not empty error}">
        <div class="msg-error">${error}</div>
    </c:if>

    <form class="form-card" method="post" action="${pageContext.request.contextPath}/applications/create">
        <%-- 防重复提交：与 loan_applications.client_token 唯一键配合 --%>
        <input type="hidden" name="clientToken" value="${clientToken}">

        <div class="form-row">
            <label>活动名称<span class="req">*</span></label>
            <div class="field">
                <input type="text" name="activityName" value="${form.activityName}" maxlength="80" required>
                <div class="form-tip">1–80 字，写清社团名称和活动主题</div>
            </div>
        </div>

        <div class="form-row">
            <label>借用用途<span class="req">*</span></label>
            <div class="field">
                <textarea name="purpose" rows="3" maxlength="300" required>${form.purpose}</textarea>
                <div class="form-tip">5–300 字，说明用途和大概使用人数</div>
            </div>
        </div>

        <div class="form-row">
            <label>预计开始<span class="req">*</span></label>
            <div class="field">
                <input type="date" name="expectedStart" value="${form.expectedStart}" required>
            </div>
        </div>

        <div class="form-row">
            <label>预计归还<span class="req">*</span></label>
            <div class="field">
                <input type="date" name="expectedReturn" value="${form.expectedReturn}" required>
                <div class="form-tip">不得早于开始日期</div>
            </div>
        </div>

        <div class="form-row">
            <label>借用物资<span class="req">*</span></label>
            <div class="field">
                <table class="list" style="max-width:640px">
                    <thead>
                    <tr><th>物资</th><th style="width:130px">数量</th></tr>
                    </thead>
                    <tbody>
                    <c:forEach begin="0" end="${formRows - 1}" var="i">
                        <tr>
                            <td>
                                <select name="materialId" style="width:100%">
                                    <option value="">— 请选择 —</option>
                                    <c:forEach var="m" items="${materials}">
                                        <option value="${m.id}"
                                            <c:if test="${not empty formItems and formItems[i].materialId == m.id}">selected</c:if>>
                                            ${m.name}（可用 ${m.availableQty}${m.unit}）
                                        </option>
                                    </c:forEach>
                                </select>
                            </td>
                            <td>
                                <input type="number" name="quantity" min="1" max="999" style="width:100%"
                                       value="${not empty formItems and formItems[i].quantity > 0 ? formItems[i].quantity : ''}"
                                       placeholder="数量">
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                <div class="form-tip">只列出当前可用量大于 0 的物资；空行会跳过；同一物资请合并成一行。</div>
            </div>
        </div>

        <div class="form-actions">
            <button type="submit" class="btn-primary">提交申请</button>
            <a class="btn" href="${pageContext.request.contextPath}/applications">取消</a>
        </div>
    </form>
</div>

</body>
</html>
