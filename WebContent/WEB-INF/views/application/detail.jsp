<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>申请详情</title>
    <style>
        body { font-family: "Microsoft YaHei", sans-serif; margin: 24px; color: #1f2937; }
        h2 { border-left: 4px solid #0e7490; padding-left: 10px; }
        table { border-collapse: collapse; margin-top: 12px; }
        th, td { border: 1px solid #d8dee7; padding: 7px 12px; text-align: left; font-size: 14px; }
        th { background: #eef3f7; }
        .field { margin: 6px 0; font-size: 14px; }
        .field b { display: inline-block; width: 100px; color: #374151; font-weight: normal; }
        .status { font-size: 12px; padding: 2px 8px; border-radius: 4px; }
        .PENDING   { background: #fef3c7; color: #92400e; }
        .APPROVED  { background: #eff6ff; color: #1e40af; }
        .REJECTED  { background: #fee2e2; color: #991b1b; }
        .BORROWED  { background: #f5f3ff; color: #5b21b6; }
        .RETURNED  { background: #ecfdf5; color: #065f46; }
        .CANCELLED { background: #f3f4f6; color: #374151; }
        .error { background: #fef2f2; color: #991b1b; border: 1px solid #fecaca;
                 padding: 12px; border-radius: 4px; font-size: 14px; }
        a { color: #0e7490; text-decoration: none; }
        .btn-cancel { color: #b91c1c; border: 1px solid #fecaca; background: #fff; cursor: pointer;
                      font-size: 13px; padding: 5px 14px; border-radius: 4px; }
    </style>
</head>
<body>

<h2>申请详情</h2>

<c:choose>
    <c:when test="${not empty error}">
        <%-- 非本人访问：不泄露任何单头内容 --%>
        <div class="error">${error}</div>
        <p style="margin-top:16px;"><a href="${pageContext.request.contextPath}/applications">返回我的申请</a></p>
    </c:when>
    <c:otherwise>
        <div class="field"><b>编号</b>${app.id}</div>
        <div class="field"><b>活动名称</b>${app.activityName}</div>
        <div class="field"><b>用途</b>${app.purpose}</div>
        <div class="field"><b>预计开始</b>${app.expectedStart}</div>
        <div class="field"><b>预计归还</b>${app.expectedReturn}</div>
        <div class="field"><b>状态</b><span class="status ${app.status}">${app.statusText}</span></div>
        <div class="field"><b>申请时间</b>${app.applyTimeText}</div>

        <h3 style="font-size:15px;margin-top:20px;">物资明细</h3>
        <c:choose>
            <c:when test="${empty items}">
                <p style="color:#64748b;font-size:14px;">无明细</p>
            </c:when>
            <c:otherwise>
                <table>
                    <thead>
                    <tr>
                        <th>物资</th><th>数量</th><th>单位</th><th>损坏</th><th>遗失</th><th>备注</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="it" items="${items}">
                        <tr>
                            <td>${it.materialName}</td>
                            <td>${it.quantity}</td>
                            <td>${it.unit}</td>
                            <td>${it.damagedQty}</td>
                            <td>${it.lostQty}</td>
                            <td>${it.remark}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>

        <h3 style="font-size:15px;margin-top:20px;">审批 / 流转</h3>
        <div class="field"><b>审批意见</b>${empty app.reviewRemark ? '—' : app.reviewRemark}</div>
        <div class="field"><b>审批时间</b>${empty app.reviewTimeText ? '—' : app.reviewTimeText}</div>
        <div class="field"><b>借出时间</b>${empty app.borrowTimeText ? '—' : app.borrowTimeText}</div>
        <div class="field"><b>归还时间</b>${empty app.returnTimeText ? '—' : app.returnTimeText}</div>

        <div style="margin-top:20px;">
            <c:if test="${app.status == 'PENDING'}">
                <form method="post" action="${pageContext.request.contextPath}/applications/cancel"
                      onsubmit="return confirm('确认撤回此申请？');" style="display:inline">
                    <input type="hidden" name="id" value="${app.id}">
                    <button type="submit" class="btn-cancel">撤回申请</button>
                </form>
            </c:if>
            <a href="${pageContext.request.contextPath}/applications" style="margin-left:12px;">返回我的申请</a>
        </div>
    </c:otherwise>
</c:choose>

</body>
</html>
