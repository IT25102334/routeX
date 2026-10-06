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
 *   POST /admin/report-update  - edit an existing report's title/details
 */
@WebServlet(urlPatterns = {"/admin/dashboard", "/admin/user-status", "/admin/verify-driver", "/admin/report", "/admin/report-update", "/admin/report-resolve", "/admin/report-delete", "/admin/dispatch"})
public class AdminReportServlet extends HttpServlet {

    private static final int TITLE_MAX_LEN = 150;
    private static final int DETAILS_MAX_LEN = 2000;
    private static final java.util.Set<String> VALID_USER_STATUSES = java.util.Set.of("ACTIVE", "SUSPENDED");

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

            // Pick up any validation error left by a redirected POST (flash-message pattern)
            Object flashError = req.getSession().getAttribute("flashError");
            if (flashError != null) {
                req.setAttribute("errorMessage", flashError);
                req.getSession().removeAttribute("flashError");
            }

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
                Long userId = parseId(req.getParameter("userId"));
                String status = req.getParameter("status");
                if (userId == null) {
                    fail(req, res, "Invalid user ID.");
                    return;
                }
                if (status == null || !VALID_USER_STATUSES.contains(status)) {
                    fail(req, res, "Invalid status value.");
                    return;
                }
                userDao.setStatus(userId, status);

            } else if (path.endsWith("verify-driver")) {
                Long userId = parseId(req.getParameter("userId"));
                if (userId == null) {
                    fail(req, res, "Invalid driver ID.");
                    return;
                }
                driverDao.verify(userId);
                // A newly-verified driver might already be online and waiting - give
                // the oldest stuck ride an immediate chance to match against them.
                dispatchService.retryOldestPendingRide();

            } else if (path.endsWith("dispatch")) {
                Long rideId = parseId(req.getParameter("rideId"));
                if (rideId == null) {
                    fail(req, res, "Invalid ride ID.");
                    return;
                }
                // Manual admin override: force a re-match attempt on a specific ride
                // that's stuck at REQUESTED (e.g. no driver was free earlier).
                dispatchService.dispatchRide(rideId);

            } else if (path.endsWith("report-update")) {
                // Edit: completes the missing CRUD piece - update an existing report's title/details.
                Long reportId = parseId(req.getParameter("reportId"));
                String title = trimToNull(req.getParameter("title"));
                String details = trimToNull(req.getParameter("details"));

                if (reportId == null) {
                    fail(req, res, "Invalid report ID.");
                    return;
                }
                String validationError = validateReportFields(title, details);
                if (validationError != null) {
                    fail(req, res, validationError);
                    return;
                }
                if (!reportDao.exists(reportId)) {
                    fail(req, res, "That report no longer exists.");
                    return;
                }
                reportDao.updateReport(reportId, title, details);

            } else if (path.endsWith("report-resolve")) {
                // UC-06 support: Update status - mark a report resolved without removing its history.
                Long reportId = parseId(req.getParameter("reportId"));
                if (reportId == null) {
                    fail(req, res, "Invalid report ID.");
                    return;
                }
                reportDao.resolveReport(reportId);

            } else if (path.endsWith("report-delete")) {
                // Delete - remove a report entirely once it's no longer needed.
                Long reportId = parseId(req.getParameter("reportId"));
                if (reportId == null) {
                    fail(req, res, "Invalid report ID.");
                    return;
                }
                reportDao.deleteReport(reportId);

            } else if (path.endsWith("report")) {
                // Create.
                String title = trimToNull(req.getParameter("title"));
                String details = trimToNull(req.getParameter("details"));
                String validationError = validateReportFields(title, details);
                if (validationError != null) {
                    fail(req, res, validationError);
                    return;
                }
                reportDao.createReport(admin.getId(), title, details);
            }

            res.sendRedirect(req.getContextPath() + "/admin/dashboard");
        } catch (SQLException e) {
            throw new ServletException("Failed to process admin action", e);
        }
    }

    /** Shared field checks for both create and edit, so the two CRUD paths can't drift apart. */
    private String validateReportFields(String title, String details) {
        if (title == null) return "Report title is required.";
        if (title.length() > TITLE_MAX_LEN) return "Report title must be " + TITLE_MAX_LEN + " characters or fewer.";
        if (details == null) return "Report details are required.";
        if (details.length() > DETAILS_MAX_LEN) return "Report details must be " + DETAILS_MAX_LEN + " characters or fewer.";
        return null;
    }

    /** Parses a request parameter as a positive numeric ID, returning null instead of throwing on bad input. */
    private Long parseId(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            long id = Long.parseLong(raw.trim());
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String trimToNull(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** Stores a validation error for the next GET to display, then redirects back to the dashboard. */
    private void fail(HttpServletRequest req, HttpServletResponse res, String message) throws IOException {
        req.getSession().setAttribute("flashError", message);
        res.sendRedirect(req.getContextPath() + "/admin/dashboard");
    }

    private User currentUser(HttpServletRequest req) {
        return (User) req.getSession().getAttribute("user");
    }
}
