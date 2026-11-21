<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.club.common.HtmlUtil" %>
<%--
  注册页：和登录页同一套版式（校门照片 + 右侧卡片）
  注册出来的账号一律是社团成员，管理员不开放注册。
--%>
<%
    String ctx = request.getContextPath();
    String error = (String) request.getAttribute("error");
    String username = (String) request.getAttribute("username");
    String realName = (String) request.getAttribute("realName");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>注册 - 社团活动物资借还系统</title>
  <style>
    * { box-sizing: border-box; }
    body {
      margin: 0;
      font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
      color: #1f2d3d;
      background: #f5f7fa;
    }

    .login-top {
      height: 64px;
      background: #0b4a9e;
      display: flex;
      align-items: center;
      padding: 0 32px;
      gap: 12px;
      color: #fff;
    }
    .login-logo {
      width: 42px; height: 42px;
      display: block;
      flex: none;
    }
    .login-title { font-size: 20px; letter-spacing: 3px; font-weight: 700; }
    .login-title small {
      display: block; font-size: 10px; letter-spacing: 1.5px;
      font-weight: 400; color: rgba(255,255,255,.72);
    }

    .login-stage {
      min-height: calc(100vh - 64px);
      background: url("<%=ctx%>/images/campus-gate.png") center center / cover no-repeat;
      display: flex;
      justify-content: flex-end;
      align-items: flex-start;
      padding: 56px 8vw 40px;
    }

    .login-card {
      width: 380px;
      max-width: 100%;
      background: #fff;
      border-radius: 4px;
      box-shadow: 0 16px 46px rgba(6, 30, 60, .28);
      padding: 24px 26px 22px;
    }

    .login-tabs {
      display: flex;
      gap: 22px;
      border-bottom: 1px solid #e4e9f0;
      margin: -4px 0 18px;
    }
    .login-tabs span { padding: 8px 2px 10px; font-size: 14px; color: #6b7a8c; }
    .login-tabs .on {
      color: #0f4c9c; font-weight: 700;
      border-bottom: 2px solid #0f4c9c; margin-bottom: -1px;
    }

    .login-card h1 { margin: 0 0 4px; font-size: 18px; letter-spacing: 1px; }
    .login-card .sub { margin: 0 0 14px; font-size: 12px; color: #6b7a8c; }

    .login-card label { display: block; margin: 10px 0 4px; font-size: 13px; color: #4a5a6e; }
    .login-card input {
      width: 100%; height: 38px; padding: 0 11px;
      font-size: 14px; color: #1f2d3d;
      border: 1px solid #cfd8e3; border-radius: 3px; outline: 0; background: #fff;
    }
    .login-card input:focus {
      border-color: #0f4c9c;
      box-shadow: 0 0 0 3px rgba(15, 76, 156, .12);
    }

    .login-card button {
      width: 100%; height: 40px; margin-top: 18px;
      border: 0; border-radius: 3px;
      background: #0f4c9c; color: #fff;
      font-size: 15px; letter-spacing: 6px;
      cursor: pointer; font-family: inherit;
    }
    .login-card button:hover { background: #0a3b7d; }

    .login-error {
      margin-top: 10px; padding: 8px 11px;
      background: #fdf0ee; color: #b3261e;
      border: 1px solid #e5b7ae; border-radius: 3px; font-size: 13px;
    }

    .login-links {
      margin-top: 14px;
      display: flex;
      justify-content: space-between;
      font-size: 13px;
    }
    .login-links a { color: #0f4c9c; text-decoration: none; }
    .login-links a:hover { text-decoration: underline; }

    .login-note {
      margin-top: 16px; padding-top: 12px;
      border-top: 1px dashed #e4e9f0;
      font-size: 12px; line-height: 1.9; color: #6b7a8c;
    }

    @media (max-width: 760px) {
      .login-top { padding: 0 18px; }
      .login-title { font-size: 17px; letter-spacing: 1px; }
      .login-stage { justify-content: center; padding: 28px 18px; }
    }
  </style>
</head>
<body>

<div class="login-top">
  <img class="login-logo" src="<%=ctx%>/images/bistu-logo-white.png" alt="北京信息科技大学">
  <div class="login-title">
    北京信息科技大学 · 社团活动物资借还系统
    <small>CLUB MATERIAL BORROWING &amp; RETURN SYSTEM</small>
  </div>
</div>

<div class="login-stage">
  <div class="login-card">
    <div class="login-tabs">
      <span class="on">注册账号</span>
    </div>

    <h1>社团成员注册</h1>
    <p class="sub">注册后即可提交物资借用申请</p>

    <% if (error != null) { %>
      <div class="login-error"><%= HtmlUtil.escape(error) %></div>
    <% } %>

    <form method="post" action="<%=ctx%>/register">
      <label for="username">学号 / 账号</label>
      <input id="username" type="text" name="username" maxlength="20"
             value="<%= HtmlUtil.escape(username == null ? "" : username) %>"
             placeholder="4-20 位字母或数字" autocomplete="username" autofocus>

      <label for="realName">真实姓名</label>
      <input id="realName" type="text" name="realName" maxlength="20"
             value="<%= HtmlUtil.escape(realName == null ? "" : realName) %>"
             placeholder="用于借还登记时显示" autocomplete="name">

      <label for="password">密码</label>
      <input id="password" type="password" name="password" maxlength="32"
             placeholder="6-32 位" autocomplete="new-password">

      <label for="confirmPassword">确认密码</label>
      <input id="confirmPassword" type="password" name="confirmPassword" maxlength="32"
             placeholder="再输入一次密码" autocomplete="new-password">

      <button type="submit">注 册</button>
    </form>

    <div class="login-links">
      <span></span>
      <a href="<%=ctx%>/login">已有账号？去登录</a>
    </div>

    <div class="login-note">
      管理员账号不开放注册；<br>
      注册只用于提交申请，审批与借还由管理员操作。
    </div>
  </div>
</div>

</body>
</html>
