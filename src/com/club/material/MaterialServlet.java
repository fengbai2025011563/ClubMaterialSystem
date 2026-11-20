package com.club.material;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.club.common.BusinessException;
import com.club.user.User;

/**
 * 物资模块的控制器（Servlet），地址统一挂在 /material/ 下。
 *
 * <p>【它的使命】只做三件事：接参数、叫 Service 干活、决定下一步去哪个页面。
 * 它自己不写 SQL、也不判断库存够不够，那些都在 Service 里。</p>
 *
 * <p>【地址一览】</p>
 * <p>GET  /material/list   —— 物资浏览（谁都能看，只显示可借的）</p>
 * <p>GET  /material/admin  —— 物资管理列表（仅管理员）</p>
 * <p>GET  /material/form   —— 新增 / 编辑表单（仅管理员）</p>
 * <p>POST /material/save   —— 保存新增或修改</p>
 * <p>POST /material/status —— 启用 / 停用</p>
 * <p>POST /material/stock  —— 入库</p>
 *
 * <p>【为什么用注解】@WebServlet 写在类上就不用改 web.xml，
 * 这样和晏思汗维护的 web.xml 不会互相冲突，合并时只保留他的配置即可。</p>
 */
@WebServlet("/material/*")
public class MaterialServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final MaterialService service = new MaterialService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();
        if (path == null) {
            path = "/list";
        }

        try {
            if ("/admin".equals(path)) {
                showAdminList(request, response);
            } else if ("/form".equals(path)) {
                showForm(request, response);
            } else {
                showBrowseList(request, response);
            }
        } catch (BusinessException e) {
            // 把业务提示带到浏览页，页面顶部用黄条显示
            request.setAttribute("error", e.getMessage());
            showBrowseList(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();
        if (path == null) {
            path = "/list";
        }

        if ("/save".equals(path)) {
            saveMaterial(request, response);
        } else if ("/status".equals(path)) {
            changeStatus(request, response);
        } else if ("/stock".equals(path)) {
            addStock(request, response);
        } else if ("/reduce".equals(path)) {
            reduceStock(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/material/list");
        }
    }

    // ======================== 页面：浏览 ========================

    /**
     * 成员视角的物资列表：只看可借的，支持按名称搜索、按分类筛选。
     */
    private void showBrowseList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = trimToNull(request.getParameter("keyword"));
        String category = trimToNull(request.getParameter("category"));

        List<Material> materials = service.list(keyword, category, "ACTIVE");
        request.setAttribute("materials", materials);
        request.setAttribute("categories", service.categories());
        request.setAttribute("keyword", keyword == null ? "" : keyword);
        request.setAttribute("category", category == null ? "" : category);
        request.setAttribute("loginUser", request.getSession().getAttribute("loginUser"));

        request.getRequestDispatcher("/jsp/materialList.jsp").forward(request, response);
    }

    // ======================== 页面：管理列表 ========================

    /**
     * 管理员视角：能看到停用的物资，也能看到预留与借出的数量。
     */
    private void showAdminList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User admin = requireAdmin(request, response);
        if (admin == null) {
            return;
        }

        String keyword = trimToNull(request.getParameter("keyword"));
        String category = trimToNull(request.getParameter("category"));
        String status = trimToNull(request.getParameter("status"));

        request.setAttribute("materials", service.list(keyword, category, status));
        request.setAttribute("categories", service.categories());
        request.setAttribute("keyword", keyword == null ? "" : keyword);
        request.setAttribute("category", category == null ? "" : category);
        request.setAttribute("status", status == null ? "" : status);
        request.setAttribute("loginUser", admin);

        request.getRequestDispatcher("/jsp/materialAdmin.jsp").forward(request, response);
    }

    // ======================== 页面：表单 ========================

    /**
     * 新增（id 为空）或编辑（带 id）。编辑时顺带把库存流水查出来放在页面下方。
     */
    private void showForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User admin = requireAdmin(request, response);
        if (admin == null) {
            return;
        }

        int id = parseInt(request.getParameter("id"), 0);
        Material material = null;
        if (id > 0) {
            material = service.get(id);
            if (material == null) {
                redirect(request, response, "/material/admin", "物资不存在，可能已被删除");
                return;
            }
            request.setAttribute("logs", service.logs(id));
        }

        request.setAttribute("material", material);
        request.setAttribute("loginUser", admin);
        request.getRequestDispatcher("/jsp/materialForm.jsp").forward(request, response);
    }

    // ======================== 动作：保存 ========================

    /**
     * 保存。校验失败时把用户填的内容原样带回表单，避免重新输入。
     */
    private void saveMaterial(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User admin = requireAdmin(request, response);
        if (admin == null) {
            return;
        }

        Material material = new Material();
        material.setId(parseInt(request.getParameter("id"), 0));
        material.setName(request.getParameter("name"));
        material.setCategory(request.getParameter("category"));
        material.setUnit(request.getParameter("unit"));
        material.setRemark(request.getParameter("remark"));
        material.setTotalQty(parseInt(request.getParameter("totalQty"), 0));

        try {
            if (material.getId() > 0) {
                service.update(material, admin.getId());
                redirect(request, response, "/material/admin", "保存成功");
            } else {
                service.create(material, admin.getId());
                redirect(request, response, "/material/admin", "新增成功，已生成入库流水");
            }
        } catch (BusinessException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("material", material);
            if (material.getId() > 0) {
                request.setAttribute("logs", service.logs(material.getId()));
            }
            request.getRequestDispatcher("/jsp/materialForm.jsp").forward(request, response);
        }
    }

    // ======================== 动作：启用/停用 ========================

    private void changeStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        User admin = requireAdmin(request, response);
        if (admin == null) {
            return;
        }

        int id = parseInt(request.getParameter("id"), 0);
        String status = request.getParameter("status");

        try {
            service.changeStatus(id, status);
            redirect(request, response, "/material/admin",
                    "ACTIVE".equals(status) ? "已启用" : "已停用");
        } catch (BusinessException e) {
            redirect(request, response, "/material/admin", e.getMessage());
        }
    }

    // ======================== 动作：入库 ========================

    private void addStock(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        User admin = requireAdmin(request, response);
        if (admin == null) {
            return;
        }

        int id = parseInt(request.getParameter("id"), 0);
        int quantity = parseInt(request.getParameter("quantity"), 0);
        String note = request.getParameter("note");

        try {
            service.addStock(id, quantity, admin.getId(), note);
            redirect(request, response, "/material/admin", "入库成功");
        } catch (BusinessException e) {
            redirect(request, response, "/material/admin", e.getMessage());
        }
    }

    // ======================== 动作：调减 ========================

    private void reduceStock(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        User admin = requireAdmin(request, response);
        if (admin == null) {
            return;
        }

        int id = parseInt(request.getParameter("id"), 0);
        int quantity = parseInt(request.getParameter("quantity"), 0);

        try {
            service.reduceStock(id, quantity, admin.getId(), null);
            redirect(request, response, "/material/admin", "调减成功");
        } catch (BusinessException e) {
            redirect(request, response, "/material/admin", e.getMessage());
        }
    }

    // ======================== 工具方法 ========================

    /**
     * 取当前登录用户并要求管理员身份。不满足就地跳转并返回 null，调用方直接 return。
     */
    private User requireAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Object value = request.getSession().getAttribute("loginUser");
        if (!(value instanceof User)) {
            redirect(request, response, "/material/list", "请先登录，再操作物资");
            return null;
        }
        User user = (User) value;
        if (!user.isAdmin()) {
            redirect(request, response, "/material/list", "只有管理员才能维护物资");
            return null;
        }
        return user;
    }

    /**
     * 带提示信息跳转。提示放在 URL 参数里，目标页面顶部显示一次。
     * 用重定向而不是转发，是为了避免刷新页面时重复提交表单。
     */
    private void redirect(HttpServletRequest request, HttpServletResponse response,
                          String path, String message) throws IOException {
        StringBuilder url = new StringBuilder(request.getContextPath()).append(path);
        if (message != null && !message.isEmpty()) {
            url.append(path.contains("?") ? "&" : "?").append("msg=")
                    .append(URLEncoder.encode(message, "UTF-8"));
        }
        response.sendRedirect(url.toString());
    }

    /**
     * 字符串转整数，转不动就用默认值（例如用户在数量框里敲了字母）。
     */
    private int parseInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 去掉首尾空格，空串当成 null，方便 Service 判断"这个筛选项没填"。
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 页面输出统一走这里做 HTML 转义。
     * 备注里如果写了 &lt;b&gt; 之类的内容，直接输出会把页面结构搞乱，转义后就只是普通文字。
     */
    public static String escape(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
