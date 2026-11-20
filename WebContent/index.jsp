<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%--
  【临时调试入口】正式首页由晏思汗负责（导航、登录状态展示等）。
  这里的唯一作用是：单独测物资模块时，访问 http://localhost:8080/ClubMaterialSystem/
  就能直接跳到物资浏览页，不用手敲长地址。合并后可以删除本文件。
--%>
<%
    response.sendRedirect(request.getContextPath() + "/material/list");
%>
