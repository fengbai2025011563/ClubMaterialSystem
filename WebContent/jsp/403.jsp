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
<div style="max-width:520px;margin:100px auto;background:#fff;border:1px solid #d8dee7;border-radius:12px;padding:28px;">
  <h1 style="font-size:20px;margin:0 0 8px;">403 · 无权访问</h1>
  <p style="color:#5b6472;font-size:14px;">当前账号没有访问这个页面的权限。</p>
  <p><a href="<%=ctx%>/material/list" style="color:#0f766e;">返回物资列表</a></p>
</div>
</body>
</html>
