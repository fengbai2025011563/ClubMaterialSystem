package com.club.workflow;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.club.application.bean.ApplicationItem;
import com.club.application.bean.LoanApplication;
import com.club.common.BusinessException;
import com.club.user.User;

/**
 * 管理端：审批、驳回、作废、借出、归还验收。
 */
@WebServlet({
        "/admin/applications",
        "/admin/applications/detail",
        "/admin/applications/review",
        "/admin/loans/borrow",
        "/admin/loans/return"
})
public class WorkflowServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final WorkflowService service = new WorkflowService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User admin = requireAdmin(request, response);
        if (admin == null) {
            return;
        }

        String path = request.getServletPath();

        if ("/admin/applications".equals(path)) {
            String status = request.getParameter("status");
            if (status == null || status.trim().isEmpty()) {
                status = LoanApplication.PENDING;
            }
            request.setAttribute("list", service.listByStatus(status.trim()));
            request.setAttribute("status", status.trim());
            request.getRequestDispatcher("/jsp/admin/applicationList.jsp").forward(request, response);
            return;
        }

        if ("/admin/applications/detail".equals(path)) {
            int id = parseInt(request.getParameter("id"), "申请编号");
            LoanApplication app = service.detail(id);
            request.setAttribute("app", app);
            request.setAttribute("items", service.items(id));
            request.getRequestDispatcher("/jsp/admin/applicationDetail.jsp").forward(request, response);
            return;
        }

        if ("/admin/loans/return".equals(path)) {
            int id = parseInt(request.getParameter("id"), "申请编号");
            LoanApplication app = service.detail(id);
            request.setAttribute("app", app);
            request.setAttribute("items", service.items(id));
            request.getRequestDispatcher("/jsp/admin/returnForm.jsp").forward(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User admin = requireAdmin(request, response);
        if (admin == null) {
            return;
        }

        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        try {
            if ("/admin/applications/review".equals(path)) {
                int id = parseInt(request.getParameter("id"), "申请编号");
                String action = request.getParameter("action");
                String remark = request.getParameter("remark");

                if ("approve".equals(action)) {
                    service.approve(id, admin.getId(), remark);
                    redirect(request, response, "/admin/applications?status=PENDING", "已批准，库存已预留");
                } else if ("reject".equals(action)) {
                    service.reject(id, admin.getId(), remark);
                    redirect(request, response, "/admin/applications?status=PENDING", "已驳回");
                } else if ("void".equals(action)) {
                    service.voidApproved(id, admin.getId(), remark);
                    redirect(request, response, "/admin/applications?status=APPROVED", "已作废，预留已释放");
                } else {
                    throw new BusinessException("未知的审批动作");
                }
                return;
            }

            if ("/admin/loans/borrow".equals(path)) {
                int id = parseInt(request.getParameter("id"), "申请编号");
                service.borrow(id, admin.getId());
                redirect(request, response, "/admin/applications?status=APPROVED", "已登记借出");
                return;
            }

            if ("/admin/loans/return".equals(path)) {
                int id = parseInt(request.getParameter("id"), "申请编号");
                Map<Integer, int[]> damagedLost = readReturnForm(request);
                service.returnAll(id, admin.getId(), damagedLost);
                redirect(request, response, "/admin/applications?status=RETURNED", "归还完成，库存已结算");
                return;
            }

            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (BusinessException e) {
            redirect(request, response, "/admin/applications", e.getMessage());
        }
    }

    private Map<Integer, int[]> readReturnForm(HttpServletRequest request) {
        String[] itemIds = request.getParameterValues("itemId");
        String[] damaged = request.getParameterValues("damagedQty");
        String[] lost = request.getParameterValues("lostQty");
        Map<Integer, int[]> map = new HashMap<>();

        if (itemIds == null) {
            return map;
        }
        for (int i = 0; i < itemIds.length; i++) {
            int itemId = parseInt(itemIds[i], "明细编号");
            int d = parseQty(damaged, i);
            int l = parseQty(lost, i);
            map.put(itemId, new int[]{d, l});
        }
        return map;
    }

    private int parseQty(String[] values, int index) {
        if (values == null || index >= values.length || values[index] == null
                || values[index].trim().isEmpty()) {
            return 0;
        }
        try {
            int v = Integer.parseInt(values[index].trim());
            if (v < 0) {
                throw new BusinessException("损坏/遗失数量不能为负数");
            }
            return v;
        } catch (NumberFormatException e) {
            throw new BusinessException("损坏/遗失数量必须是整数");
        }
    }

    private int parseInt(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(name + "不能为空");
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(name + "格式不正确");
        }
    }

    private User requireAdmin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Object value = request.getSession().getAttribute("loginUser");
        if (!(value instanceof User)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        User user = (User) value;
        if (!user.isAdmin()) {
            request.getRequestDispatcher("/jsp/403.jsp").forward(request, response);
            return null;
        }
        return user;
    }

    private void redirect(HttpServletRequest request, HttpServletResponse response,
                          String path, String message) throws IOException {
        String url = request.getContextPath() + path;
        if (message != null && !message.isEmpty()) {
            url += (path.contains("?") ? "&" : "?") + "msg=" + URLEncoder.encode(message, "UTF-8");
        }
        response.sendRedirect(url);
    }
}
