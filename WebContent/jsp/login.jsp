<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%--
  登录页：模仿北京信息科技大学统一身份认证页
  —— 顶部蓝色品牌栏，整幅校门照片做背景，登录卡片靠右
--%>
<%
    String ctx = request.getContextPath();
    String error = (String) request.getAttribute("error");
    String msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>登录 - 社团活动物资借还系统</title>
  <style>
    * { box-sizing: border-box; }
    body {
      margin: 0;
      font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
      color: #1f2d3d;
      background: #f5f7fa;
    }

    /* 顶部蓝色品牌栏 */
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
    .login-title {
      font-size: 20px;
      letter-spacing: 3px;
      font-weight: 700;
    }
    .login-title small {
      display: block;
      font-size: 10px;
      letter-spacing: 1.5px;
      font-weight: 400;
      color: rgba(255,255,255,.72);
    }

    /* 背景照片 + 右侧登录卡片 */
    .login-stage {
      position: relative;
      min-height: calc(100vh - 64px);
      background: url("<%=ctx%>/images/campus-gate.png") center center / cover no-repeat;
      display: flex;
      justify-content: flex-end;
      align-items: flex-start;
      padding: 72px 8vw 40px;
    }

    .login-card {
      width: 360px;
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
    .login-tabs span {
      padding: 8px 2px 10px;
      font-size: 14px;
      color: #6b7a8c;
    }
    .login-tabs .on {
      color: #0f4c9c;
      font-weight: 700;
      border-bottom: 2px solid #0f4c9c;
      margin-bottom: -1px;
    }

    .login-card h1 {
      margin: 0 0 4px;
      font-size: 18px;
      letter-spacing: 1px;
    }
    .login-card .sub {
      margin: 0 0 16px;
      font-size: 12px;
      color: #6b7a8c;
    }

    .login-card label {
      display: block;
      margin: 12px 0 5px;
      font-size: 13px;
      color: #4a5a6e;
    }
    .login-card input {
      width: 100%;
      height: 38px;
      padding: 0 11px;
      font-size: 14px;
      color: #1f2d3d;
      border: 1px solid #cfd8e3;
      border-radius: 3px;
      outline: 0;
      background: #fff;
    }
    .login-card input:focus {
      border-color: #0f4c9c;
      box-shadow: 0 0 0 3px rgba(15, 76, 156, .12);
    }

    .login-card button {
      width: 100%;
      height: 40px;
      margin-top: 20px;
      border: 0;
      border-radius: 3px;
      background: #0f4c9c;
      color: #fff;
      font-size: 15px;
      letter-spacing: 6px;
      cursor: pointer;
      font-family: inherit;
    }
    .login-card button:hover { background: #0a3b7d; }

    .login-error {
      margin-top: 12px;
      padding: 8px 11px;
      background: #fdf0ee;
      color: #b3261e;
      border: 1px solid #e5b7ae;
      border-radius: 3px;
      font-size: 13px;
    }

    .login-ok {
      margin-top: 12px;
      padding: 8px 11px;
      background: #eefaf3;
      color: #1a7f4b;
      border: 1px solid #a9d8bf;
      border-radius: 3px;
      font-size: 13px;
    }

    .login-register {
      margin-top: 14px;
      text-align: right;
      font-size: 13px;
    }
    .login-register a { color: #0f4c9c; text-decoration: none; }
    .login-register a:hover { text-decoration: underline; }

    .login-warn {
      margin-top: 18px;
      padding-top: 14px;
      border-top: 1px dashed #e4e9f0;
      font-size: 12px;
      line-height: 1.9;
      color: #6b7a8c;
    }
    .login-warn b {
      display: block;
      color: #b3261e;
      font-weight: 700;
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
      <span class="on">账号登录</span>
    </div>

    <h1>社团物资借还平台</h1>
    <p class="sub">请使用学号或管理员账号登录</p>

    <% if (error != null) { %>
      <div class="login-error"><%= error %></div>
    <% } %>

    <% if ("register".equals(msg)) { %>
      <div class="login-ok">注册成功，请使用刚设置的密码登录。</div>
    <% } %>

    <form method="post" action="<%=ctx%>/login">
      <label for="username">学号 / 账号</label>
      <input id="username" type="text" name="username" autocomplete="username" autofocus
             placeholder="请输入学号或管理员账号">

      <label for="password">密码</label>
      <input id="password" type="password" name="password" autocomplete="current-password"
             placeholder="请输入密码">

      <button type="submit">登 录</button>
    </form>

    <div class="login-register">
      还没有账号？<a href="<%=ctx%>/register">注册一个</a>
    </div>

    <div class="login-warn">
      <b>请勿将账号借给他人使用</b>
      账号为本人学号，初始密码请向社团管理员获取；<br>
      借用期间请按约定时间归还，损坏与遗失需照价赔偿。
    </div>
  </div>
</div>

</body>
</html>
