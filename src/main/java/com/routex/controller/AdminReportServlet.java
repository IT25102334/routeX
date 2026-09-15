package com.routex.controller;

import com.routex.dao.DriverDao;
import com.routex.dao.ReportDao;
import com.routex.dao.RideDao;
import com.routex.dao.UserDao;
import com.routex.model.User;
import com.routex.service.DispatchService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * ================================================================
 * MODULE: Administration & Reporting
 * OWNER:  Nimnadi R.D.S. (IT25100122)
 * Implements UC-06 (View System Analytics & Reports), plus account
 * management (suspend/reactivate) and driver verification.
 * ================================================================
 *
 * Routes:
 *   GET  /admin/dashboard      - metrics, user list, ride list, reports
 *   POST /admin/user-status    - suspend / reactivate a user account
 *   POST /admin/verify-driver  - approve a pending driver registration
 *   POST /admin/report         - log a new operational/dispute report
 */
@WebServlet(urlPatterns = {"/admin/dashboard", "/admin/user-status", "/admin/verify-driver", "/admin/report", "/admin/report-resolve", "/admin/report-delete", "/admin/dispatch"})
public class AdminReportServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();
    private final DriverDao driverDao = new DriverDao();
    private final RideDao rideDao = new RideDao();
    private final ReportDao reportDao = new ReportDao();
    private final DispatchService dispatchService = new DispatchService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User user = currentUser(req);
        if (!"ADMIN".equals(user.getRole())) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try {
            // UC-06 steps 3-8: select report type / filters / retrieve data / display.
            // Filters are omitted from this prototype UI - all platform metrics are shown at once.
            req.setAttribute("metrics", reportDao.platformMetrics());
            req.setAttribute("users", userDao.findAll());
            req.setAttribute("drivers", driverDao.findAll());
            req.setAttribute("rides", rideDao.findAll());
            req.setAttribute("reports", reportDao.findAll());
            req.getRequestDispatcher("/WEB-INF/views/admin-dashboard.jsp").forward(req, res);
        } catch (SQLException e) {
            throw new ServletException("Failed to load admin dashboard", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User admin = currentUser(req);
        try {
            String path = req.getServletPath();
            if (path.endsWith("user-status")) {
                userDao.setStatus(Long.parseLong(req.getParameter("userId")), req.getParameter("status"));
            } else if (path.endsWith("verify-driver")) {
                driverDao.verify(Long.parseLong(req.getParameter("userId")));
                // A newly-verified driver might already be online and waiting - give
                // the oldest stuck ride an immediate chance to match against them.
                dispatchService.retryOldestPendingRide();
            } else if (path.endsWith("dispatch")) {
                // Manual admin override: force a re-match attempt on a specific ride
                // that's stuck at REQUESTED (e.g. no driver was free earlier).
                dispatchService.dispatchRide(Long.parseLong(req.getParameter("rideId")));
            } else if (path.endsWith("report-resolve")) {
                // UC-06 support: Update - mark a report resolved without removing its history.
                reportDao.resolveReport(Long.parseLong(req.getParameter("reportId")));
            } else if (path.endsWith("report-delete")) {
                // Completes CRUD for this module: Delete - remove a report entirely once it's no longer needed.
                reportDao.deleteReport(Long.parseLong(req.getParameter("reportId")));
            } else if (path.endsWith("report")) {
                // Create.
                reportDao.createReport(admin.getId(), req.getParameter("title"), req.getParameter("details"));
            }
            res.sendRedirect(req.getContextPath() + "/admin/dashboard");
        } catch (SQLException e) {
            throw new ServletException("Failed to process admin action", e);
        }
    }

    private User currentUser(HttpServletRequest req) {
        return (User) req.getSession().getAttribute("user");
    }
}
