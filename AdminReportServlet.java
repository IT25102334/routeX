package com.routex.servlet;

import com.routex.dao.AdminDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/report")
public class AdminReportServlet extends HttpServlet {

    private final AdminDao adminDao = new AdminDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String reportType = req.getParameter("type");
        if (reportType == null) reportType = "summary";

        switch (reportType) {
            case "users":
                req.setAttribute("users", adminDao.getAllUsers());
                break;

            case "rides":
                req.setAttribute("rides", adminDao.getAllRides());
                break;

            case "topDrivers":
                req.setAttribute("topDrivers", adminDao.getTopDrivers(5));
                break;

            case "summary":
            default:
                req.setAttribute("totalRevenue", adminDao.getTotalRevenue());
                req.setAttribute("totalRides", adminDao.getTotalRideCount());
                req.setAttribute("topDrivers", adminDao.getTopDrivers(5));
                break;
        }

        req.setAttribute("reportType", reportType);
        req.getRequestDispatcher("/adminReport.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Example: activate/deactivate a user from the admin panel
        String action = req.getParameter("action");
        if ("updateStatus".equals(action)) {
            int userId = Integer.parseInt(req.getParameter("userId"));
            String newStatus = req.getParameter("status");
            adminDao.updateUserStatus(userId, newStatus);
        }

        resp.sendRedirect(req.getContextPath() + "/admin/report?type=users");
    }
}
