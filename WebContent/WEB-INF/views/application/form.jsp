<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>新建申请</title>
    <style>
        body { font-family: "Microsoft YaHei", sans-serif; margin: 24px; color: #1f2937; }
        h2 { border-left: 4px solid #0e7490; padding-left: 10px; }
        .row { margin-bottom: 12px; }
        label { display: inline-block; width: 110px; color: #374151; font-size: 14px; vertical-align: top; }
        input[type=text], input[type=date], textarea, select {
            border: 1px solid #cbd5e1; border-radius: 4px; padding: 6px 8px; font-size: 14px;
            font-family: inherit;
        }
        textarea { width: 420px; height: 70px; }
        .hint { color: #64748b; font-size: 12px; margin-left: 4px; }
        table { border-collapse: collapse; margin-top: 6px; }
        td { padding: 4px 6px; font-size: 14px; }
        .error { background: #fef2f2; color: #991b1b; border: 1px solid #fecaca;
                 padding: 8px 12px; border-radius: 4px; margin-bottom: 12px; font-size: 14px; }
        button { background: #0e7490; color: #fff; border: none; border-radius: 4px;
                 padding: 8px 18px; font-size: 14px; cursor: pointer; }
        button:hover { background: #155e75; }
        .back { color: #0e7490; text-decoration: none; font-size: 14px; margin-left: 12px; }
    </style>
</head>
<body>

<h2>新建申请</h2>

<c:if test="${not empty error}">
    <div class="error">${error}</div>
</c:if>

<form method="post" action="${pageContext.request.contextPath}/applications/create">
    <%-- 防重复提交：与 loan_applications.client_token 唯一键配合 --%>
    <input type="hidden" name="clientToken" value="${clientToken}">

    <div class="row">
        <label>活动名称</label>
        <input type="text" name="activityName" value="${form.activityName}" maxlength="80" size="40" required>
        <span class="hint">1–80 字</span>
    </div>

    <div class="row">
        <label>用途</label>
        <textarea name="purpose" maxlength="300" required>${form.purpose}</textarea>
        <span class="hint">5–300 字</span>
    </div>

    <div class="row">
        <label>预计开始</label>
        <input type="date" name="expectedStart" value="${form.expectedStart}" required>
    </div>

    <div class="row">
        <label>预计归还</label>
        <input type="date" name="expectedReturn" value="${form.expectedReturn}" required>
        <span class="hint">不得早于开始日期</span>
    </div>

    <div class="row">
        <label>物资</label>
        <table>
            <c:forEach begin="0" end="${formRows - 1}" var="i">
                <tr>
                    <td>
                        <select name="materialId">
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
                        <input type="number" name="quantity" min="1" max="999" style="width:90px"
                               value="${not empty formItems and formItems[i].quantity > 0 ? formItems[i].quantity : ''}"
                               placeholder="数量">
                    </td>
                </tr>
            </c:forEach>
        </table>
        <span class="hint">只列当前可用量 &gt; 0 的物资；空行会跳过；同一物资请合并成一行</span>
    </div>

    <div class="row" style="margin-top:16px;">
        <label></label>
        <button type="submit">提交申请</button>
        <a class="back" href="${pageContext.request.contextPath}/applications">返回列表</a>
    </div>
</form>

</body>
</html>
