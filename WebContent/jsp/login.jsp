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
    body {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 24px;
    }
    .login-wrap {
      width: 380px;
      max-width: 100%;
      background: rgba(255, 255, 255, 0.86);
      -webkit-backdrop-filter: blur(16px);
      backdrop-filter: blur(16px);
      border: 1px solid rgba(255, 255, 255, 0.75);
      border-radius: 18px;
      padding: 34px 32px 30px;
      box-shadow: 0 24px 60px rgba(15, 23, 42, 0.18);
    }
    .campus-tag {
      display: inline-block;
      font-size: 12px;
      letter-spacing: 2px;
      color: #2f7f68;
      background: rgba(231, 244, 238, 0.9);
      border: 1px solid rgba(31, 95, 79, 0.16);
      padding: 3px 10px;
      border-radius: 999px;
      margin-bottom: 14px;
    }
    .login-wrap h1 { font-size: 22px; margin: 0 0 6px; color: #173a31; letter-spacing: 0.5px; }
    .login-wrap .sub { color: #5f6f69; font-size: 13px; margin: 0 0 18px; }
    .login-wrap label { display: block; margin: 14px 0 6px; font-size: 13px; color: #33443d; }
    .login-wrap input { width: 100%; padding: 10px 12px; border: 1px solid #cfd8d4;
                        border-radius: 9px; font-size: 14px; background: rgba(255,255,255,0.95); }
    .login-wrap input:focus { border-color: #2f7f68; box-shadow: 0 0 0 3px rgba(47,127,104,0.14); }
    .login-wrap button { width: 100%; margin-top: 22px; padding: 11px; border: 0; border-radius: 9px;
                         background: linear-gradient(135deg, #1f7a64, #145c49); color: #fff;
                         font-size: 14px; cursor: pointer; box-shadow: 0 8px 20px rgba(31,95,79,0.25); }
    .login-wrap button:hover { background: linear-gradient(135deg, #238a70, #17654f); }
    .login-error { margin-top: 12px; padding: 9px 12px; background: rgba(254,242,242,0.9);
                   color: #b91c1c; border: 1px solid #fecaca; border-radius: 9px; font-size: 13px; }
    .login-foot { margin-top: 18px; text-align: center; color: #7d8b85; font-size: 12px; letter-spacing: 1px; }
  </style>
</head>
<body>
<div class="login-wrap">
  <div class="campus-tag">北京信息科技大学</div>
  <h1>社团活动物资借还</h1>
  <p class="sub">请使用社团账号登录</p>

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
  <div class="login-foot">校园物资 · 借还有据</div>
</div>
</body>
</html>
