<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String ctx = request.getContextPath();
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <title>登录 - 社团活动物资借还系统</title>
  <link rel="stylesheet" href="<%=ctx%>/css/material.css">
  <style>
    .login-wrap { max-width: 360px; margin: 90px auto; background: #fff; border: 1px solid #d8dee7;
                  border-radius: 12px; padding: 28px; box-shadow: 0 8px 24px rgba(15,23,42,.08); }
    .login-wrap h1 { font-size: 20px; margin: 0 0 6px; }
    .login-wrap label { display: block; margin: 14px 0 6px; font-size: 13px; color: #374151; }
    .login-wrap input { width: 100%; padding: 9px 10px; border: 1px solid #cbd5e1;
                        border-radius: 8px; font-size: 14px; }
    .login-wrap button { width: 100%; margin-top: 18px; padding: 10px; border: 0; border-radius: 8px;
                         background: #0f766e; color: #fff; font-size: 14px; cursor: pointer; }
    .login-error { margin-top: 12px; padding: 8px 10px; background: #fef2f2; color: #b91c1c;
                   border: 1px solid #fecaca; border-radius: 8px; font-size: 13px; }
  </style>
</head>
<body>
<div class="login-wrap">
  <h1>社团活动物资借还</h1>
  <p style="color:#5b6472;font-size:13px;margin:0;">请使用社团账号登录</p>

  <% if (error != null) { %>
    <div class="login-error"><%= error %></div>
  <% } %>

  <form method="post" action="<%=ctx%>/login">
    <label for="username">登录名</label>
    <input id="username" type="text" name="username" autocomplete="username" autofocus>

    <label for="password">密码</label>
    <input id="password" type="password" name="password" autocomplete="current-password">

    <button type="submit">登录</button>
  </form>
</div>
</body>
</html>
