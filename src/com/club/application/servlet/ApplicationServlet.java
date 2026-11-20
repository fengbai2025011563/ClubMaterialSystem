package com.club.application.servlet;

import com.club.application.bean.ApplicationItem;
import com.club.application.bean.LoanApplication;
import com.club.application.service.ApplicationService;
import com.club.application.temp.BusinessException;
import com.club.application.temp.TempMaterialService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 申请侧 Servlet，5 个 URL 全部分发在这里。
 * 注解映射，web.xml 不碰（stu3 维护）。
 * POST 成功一律 sendRedirect（PRG）。
 */
@WebServlet({
        "/applications",
        "/applications/new",
        "/applications/create",
        "/applications/detail",
        "/applications/cancel"
})
public class ApplicationServlet extends HttpServlet {

    private final ApplicationService appService = new ApplicationService();
    /* 【临时】stu2 的 MaterialService 到位后替换 */
    private final TempMaterialService materialService = new TempMaterialService();

    /* 表单最多 5 行物资 */
    private static final int FORM_ROWS = 5;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = pathOf(req);
        switch (path) {
            case "/applications":
                doList(req, resp);
                break;
            case "/applications/new":
                doNew(req, resp);
                break;
            case "/applications/detail":
                doDetail(req, resp);
                break;
            default:
                resp.sendError(404);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = pathOf(req);
        switch (path) {
            case "/applications/create":
                doCreate(req, resp);
                break;
            case "/applications/cancel":
                doCancel(req, resp);
                break;
            default:
                resp.sendError(404);
                break;
        }
    }

    private String pathOf(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String ctx = req.getContextPath();
        return uri.substring(ctx.length());
    }

    /* GET /applications 我的申请列表 */
    private void doList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String status = req.getParameter("status");
        int userId = currentUserId(req);
        List<LoanApplication> list = appService.listMine(userId, status);
        req.setAttribute("list", list);
        req.setAttribute("status", status);
        req.getRequestDispatcher("/WEB-INF/views/application/list.jsp").forward(req, resp);
    }

    /* GET /applications/new 填申请表单 */
    private void doNew(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("materials", materialService.listActive(null));
        req.setAttribute("clientToken", UUID.randomUUID().toString());
        req.setAttribute("formRows", FORM_ROWS);
        req.getRequestDispatcher("/WEB-INF/views/application/form.jsp").forward(req, resp);
    }

    /* POST /applications/create 提交申请 */
    private void doCreate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        LoanApplication app = new LoanApplication();
        app.setActivityName(req.getParameter("activityName"));
        app.setPurpose(req.getParameter("purpose"));
        app.setExpectedStart(req.getParameter("expectedStart"));
        app.setExpectedReturn(req.getParameter("expectedReturn"));
        app.setClientToken(req.getParameter("clientToken"));
        app.setStatus(LoanApplication.PENDING);

        List<ApplicationItem> items = parseItems(req);

        try {
            appService.create(currentUserId(req), app, items);
        } catch (BusinessException e) {
            // 回填表单，避免用户重新输入；clientToken 沿用，才能挡住重复提交
            req.setAttribute("error", e.getMessage());
            req.setAttribute("materials", materialService.listActive(null));
            req.setAttribute("clientToken", app.getClientToken());
            req.setAttribute("formRows", FORM_ROWS);
            req.setAttribute("form", app);
            req.setAttribute("formItems", padToRows(items));
            req.getRequestDispatcher("/WEB-INF/views/application/form.jsp").forward(req, resp);
            return;
        }

        req.getSession().setAttribute("flashMsg", "申请已提交，等待审批");
        req.getSession().setAttribute("flashType", "success");
        resp.sendRedirect(req.getContextPath() + "/applications");
    }

    /* GET /applications/detail 申请详情 */
    private void doDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer id = intParam(req, "id");
        if (id == null) {
            resp.sendError(400, "缺少参数 id");
            return;
        }
        try {
            LoanApplication app = appService.detail(id, currentUserId(req));
            req.setAttribute("app", app);
            req.setAttribute("items", appService.items(id));
            req.getRequestDispatcher("/WEB-INF/views/application/detail.jsp").forward(req, resp);
        } catch (BusinessException e) {
            req.setAttribute("error", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            req.getRequestDispatcher("/WEB-INF/views/application/detail.jsp").forward(req, resp);
        }
    }

    /* POST /applications/cancel 撤回申请 */
    private void doCancel(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer id = intParam(req, "id");
        HttpSession session = req.getSession();
        if (id == null) {
            session.setAttribute("flashMsg", "参数错误");
            session.setAttribute("flashType", "error");
            resp.sendRedirect(req.getContextPath() + "/applications");
            return;
        }
        try {
            appService.cancel(id, currentUserId(req));
            session.setAttribute("flashMsg", "已撤回该申请");
            session.setAttribute("flashType", "success");
        } catch (BusinessException e) {
            session.setAttribute("flashMsg", e.getMessage());
            session.setAttribute("flashType", "error");
        }
        resp.sendRedirect(req.getContextPath() + "/applications");
    }

    // ================== 辅助方法 ==================

    /**
     * 把表单里 materialId[] / quantity[] 两组下标数组组装成明细。
     * 某一行的 materialId 或 quantity 为空视为空行，直接跳过。
     */
    private List<ApplicationItem> parseItems(HttpServletRequest req) {
        String[] materialIds = req.getParameterValues("materialId");
        String[] quantities = req.getParameterValues("quantity");
        List<ApplicationItem> items = new ArrayList<>();
        if (materialIds == null) {
            return items;
        }
        for (int i = 0; i < materialIds.length; i++) {
            String mid = materialIds[i] == null ? "" : materialIds[i].trim();
            String qty = (quantities != null && i < quantities.length && quantities[i] != null)
                    ? quantities[i].trim() : "";
            if (mid.isEmpty() || qty.isEmpty()) {
                continue;   // 空行跳过
            }
            ApplicationItem item = new ApplicationItem();
            try {
                item.setMaterialId(Integer.valueOf(mid));
            } catch (NumberFormatException e) {
                throw new BusinessException("物资选择不合法");
            }
            try {
                item.setQuantity(Integer.valueOf(qty));
            } catch (NumberFormatException e) {
                // 交给 Service 统一提示“必须是 1–999 的整数”
                item.setQuantity(-1);
            }
            items.add(item);
        }
        return items;
    }

    /** 把明细补齐到表单行数，保证 JSP 能按索引安全取值 */
    private List<ApplicationItem> padToRows(List<ApplicationItem> items) {
        List<ApplicationItem> padded = new ArrayList<>(items);
        while (padded.size() < FORM_ROWS) {
            padded.add(new ApplicationItem());
        }
        return padded;
    }

    private Integer intParam(HttpServletRequest req, String name) {        String v = req.getParameter(name);
        if (v == null || v.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 当前登录用户 id。
     * 【临时】正式逻辑由 stu3 的 AuthFilter 保证 session 里一定有 loginUser；
     * 本地自测（还没有登录模块）时回退到 1，合并后请删除回退分支。
     */
    private int currentUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object loginUser = session.getAttribute("loginUser");
            if (loginUser != null) {
                try {
                    Object id = loginUser.getClass().getMethod("getId").invoke(loginUser);
                    if (id instanceof Number) {
                        return ((Number) id).intValue();
                    }
                } catch (ReflectiveOperationException ignored) {
                    // 落到下面的临时回退
                }
            }
        }
        return 1;   // TODO 合并 stu3 的登录模块后删除
    }
}
