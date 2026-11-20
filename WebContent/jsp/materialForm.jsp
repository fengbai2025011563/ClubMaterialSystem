<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.club.material.Material" %>
<%@ page import="com.club.material.StockLog" %>
<%@ page import="com.club.material.MaterialServlet" %>
<%@ page import="com.club.user.User" %>
<%--
  物资表单：新增和编辑共用一页。
  id 为空 = 新增（这时可以填初始库存总量）；带 id = 编辑（数量只读，只能看流水）。
--%>
<%
    String ctx = request.getContextPath();
    Material material = (Material) request.getAttribute("material");
    List<StockLog> logs = (List<StockLog>) request.getAttribute("logs");
    User loginUser = (User) request.getAttribute("loginUser");
    String error = (String) request.getAttribute("error");

    boolean isEdit = material != null && material.getId() > 0;
    SimpleDateFormat timeFormat = new SimpleDateFormat("MM-dd HH:mm");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title><%= isEdit ? "编辑物资" : "新增物资" %> - 社团活动物资借还系统</title>
    <link rel="stylesheet" href="<%=ctx%>/css/material.css">
</head>
<body>

<div class="topbar">
    <div class="topbar-inner">
        <div class="brand">社团活动物资借还系统<span>物资库</span></div>
        <div>
            <% if (loginUser != null) { %>
                <span style="font-size:13px;color:#d9efe7;">
                    <%= MaterialServlet.escape(loginUser.getRealName()) %>（<%= loginUser.getRoleText() %>）
                </span>
            <% } %>
            <a href="<%=ctx%>/material/admin">物资管理</a>
            <a href="<%=ctx%>/material/list">物资浏览</a>
        </div>
    </div>
</div>

<div class="wrap">
    <div class="page-head">
        <div>
            <h1><%= isEdit ? "编辑物资" : "新增物资" %></h1>
            <div class="sub">
                <%= isEdit ? "只能改名称、分类、单位、备注；数量请用管理页的“入库”操作" : "登记一件新物资，并写明初始库存" %>
            </div>
        </div>
    </div>

    <% if (error != null && !error.isEmpty()) { %>
        <div class="msg-error"><%= MaterialServlet.escape(error) %></div>
    <% } %>

    <% if (isEdit) { %>
    <div class="summary">
        <div class="box">
            <div class="label">库存总量</div>
            <div class="value plain"><%= material.getTotalQty() %></div>
        </div>
        <div class="box">
            <div class="label">当前可借</div>
            <div class="value"><%= material.getAvailableQty() %></div>
        </div>
        <div class="box">
            <div class="label">预留（审批占用）</div>
            <div class="value plain"><%= material.getReservedQty() %></div>
        </div>
        <div class="box">
            <div class="label">借出（未归还）</div>
            <div class="value plain"><%= material.getBorrowedQty() %></div>
        </div>
        <div class="box">
            <div class="label">状态</div>
            <div class="value plain" style="font-size:16px;padding-top:6px;">
                <span class="pill <%= material.isActive() ? "pill-active" : "pill-inactive" %>"><%= material.getStatusText() %></span>
            </div>
        </div>
    </div>
    <% } %>

    <form class="form-card" method="post" action="<%=ctx%>/material/save">
        <input type="hidden" name="id" value="<%= isEdit ? material.getId() : 0 %>">

        <div class="form-row">
            <label for="name">物资名称<span class="req">*</span></label>
            <div class="field">
                <input type="text" id="name" name="name" maxlength="50"
                       placeholder="例如 折叠帐篷"
                       value="<%= MaterialServlet.escape(material != null ? material.getName() : "") %>">
                <div class="form-tip">同一个名称只能登记一次，重名时数据库会拒绝</div>
            </div>
        </div>

        <div class="form-row">
            <label for="category">物资分类<span class="req">*</span></label>
            <div class="field">
                <input type="text" id="category" name="category" maxlength="30" list="categoryOptions"
                       placeholder="例如 户外 / 家具 / 设备"
                       value="<%= MaterialServlet.escape(material != null ? material.getCategory() : "") %>">
                <datalist id="categoryOptions">
                    <option value="户外"></option>
                    <option value="家具"></option>
                    <option value="设备"></option>
                    <option value="消耗品"></option>
                </datalist>
                <div class="form-tip">分类可以自由填写，填过的分类会自动出现在搜索下拉框里</div>
            </div>
        </div>

        <div class="form-row">
            <label for="unit">计量单位</label>
            <div class="field">
                <input type="text" id="unit" name="unit" maxlength="10"
                       placeholder="件 / 顶 / 张 / 台"
                       value="<%= MaterialServlet.escape(material != null ? material.getUnit() : "件") %>">
                <div class="form-tip">不填时默认按“件”处理</div>
            </div>
        </div>

        <% if (!isEdit) { %>
        <div class="form-row">
            <label for="totalQty">初始库存</label>
            <div class="field">
                <input type="number" id="totalQty" name="totalQty" min="0" step="1"
                       value="<%= material != null ? material.getTotalQty() : 0 %>">
                <div class="form-tip">
                    新登记时可直接写现有数量；写 0 也可以，之后再从管理页“入库”。
                    保存后会自动生成一条“手工调整”的库存流水。
                </div>
            </div>
        </div>
        <% } else { %>
        <div class="form-row">
            <label>初始库存</label>
            <div class="field">
                <span class="muted">已登记，不支持在此修改；需要增加请回管理页用“入库”。</span>
            </div>
        </div>
        <% } %>

        <div class="form-row">
            <label for="remark">备注</label>
            <div class="field">
                <textarea id="remark" name="remark" rows="3" maxlength="200"
                          placeholder="存放地点、负责人等，例如 3 号储物间 A 架"><%= MaterialServlet.escape(material != null ? material.getRemark() : "") %></textarea>
            </div>
        </div>

        <div class="form-actions">
            <button type="submit" class="btn-primary"><%= isEdit ? "保存修改" : "确认新增" %></button>
            <a class="btn" href="<%=ctx%>/material/admin">返回列表</a>
        </div>
    </form>

    <% if (isEdit) { %>
    <div class="card" style="padding:20px 22px 24px;">
        <div class="section-title">库存流水（最近 100 条）</div>
        <% if (logs != null && !logs.isEmpty()) { %>
        <table class="list">
            <thead>
            <tr>
                <th>时间</th>
                <th>类型</th>
                <th>数量变化</th>
                <th class="num">关联申请</th>
                <th class="num">操作人</th>
                <th>备注</th>
            </tr>
            </thead>
            <tbody>
            <% for (StockLog log : logs) { %>
            <tr>
                <td class="muted">
                    <%= log.getCreatedAt() == null ? "" : timeFormat.format(log.getCreatedAt()) %>
                </td>
                <td><%= log.getChangeTypeText() %></td>
                <td class="muted"><%= log.getDeltaText() %></td>
                <td class="num muted"><%= log.getApplicationId() == null ? "-" : "#" + log.getApplicationId() %></td>
                <td class="num muted"><%= log.getOperatorId() == null ? "-" : "#" + log.getOperatorId() %></td>
                <td class="muted"><%= MaterialServlet.escape(log.getNote()) %></td>
            </tr>
            <% } %>
            </tbody>
        </table>
        <% } else { %>
        <div class="empty" style="padding:26px 10px;">这件物资还没有库存变动记录。</div>
        <% } %>
    </div>
    <% } %>
</div>

<div class="footer">
    【调试入口，正式登录上线后删除】模拟登录：
    <a href="<%=ctx%>/dev-login?as=admin">管理员</a>
    <a href="<%=ctx%>/dev-login?as=member">普通成员</a>
</div>

</body>
</html>
