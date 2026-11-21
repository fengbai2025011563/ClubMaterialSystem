<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <title>无权访问 - 社团活动物资借还系统</title>
  <link rel="stylesheet" href="<%=ctx%>/css/material.css">
</head>
<body>

<div class="wrap" style="max-width:520px;padding-top:80px;">
  <div class="card" style="padding:30px 28px;">
    <h1 style="font-size:20px;margin:0 0 8px;">403 · 无权访问</h1>
    <p class="muted" style="margin:0 0 18px;">当前账号没有访问这个页面的权限。</p>
    <a class="btn-primary" href="<%=ctx%>/material/list">返回物资浏览</a>
  </div>
</div>

</body>
</html>
