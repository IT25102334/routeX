package com.routex.controller;

import com.routex.dao.DriverDao;
import com.routex.model.Driver;
import com.routex.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * ================================================================
 * MODULE: Driver Matching & Dispatch
 * OWNER:  Abeygunawardhana S.N.J. (IT25100107)
 * ================================================================
 * Admin-facing CRUD on driver profiles (the "drivers" table).
 *
 * Routes:
 *   GET  /driver/profile/manage - list all driver profiles + create form
 *   POST /driver/profile/create - create a profile for an existing user
 *   POST /driver/profile/update - edit an existing profile
 *   POST /driver/profile/delete - remove a profile
 */
@WebServlet(urlPatterns = {"/driver/profile/manage", "/driver/profile/create", "/driver/profile/update", "/driver/profile/delete"})
public class DriverProfileServlet extends HttpServlet {

    private final DriverDao driverDao = new DriverDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!requireAdmin(req, res)) {
            return;
        }
        try {
            List<Driver> drivers = driverDao.findAll();
            req.setAttribute("drivers", drivers);
            req.getRequestDispatcher("/WEB-INF/views/driver-profile-manage.jsp").forward(req, res);
        } catch (SQLException e) {
            throw new ServletException("Failed to load driver profiles", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!requireAdmin(req, res)) {
            return;
        }
        String path = req.getServletPath();
        try {
            if (path.endsWith("create")) {
                handleCreate(req, res);
            } else if (path.endsWith("update")) {
                handleUpdate(req, res);
            } else {
                handleDelete(req, res);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error during driver profile operation", e);
        }
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse res) throws SQLException, ServletException, IOException {
        try {
            long userId = Long.parseLong(req.getParameter("userId"));
            driverDao.createDriverProfile(userId, req.getParameter("licenseNo"),
                    req.getParameter("vehicleType"), req.getParameter("vehicleInfo"));
            res.sendRedirect(req.getContextPath() + "/driver/profile/manage");
        } catch (IllegalArgumentException | IllegalStateException e) {
            showError(req, res, e.getMessage());
        }
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse res) throws SQLException, ServletException, IOException {
        try {
            long userId = Long.parseLong(req.getParameter("userId"));
            driverDao.updateDriverProfile(userId, req.getParameter("licenseNo"),
                    req.getParameter("vehicleType"), req.getParameter("vehicleInfo"));
            res.sendRedirect(req.getContextPath() + "/driver/profile/manage");
        } catch (IllegalArgumentException | IllegalStateException e) {
            showError(req, res, e.getMessage());
        }
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse res) throws SQLException, ServletException, IOException {
        try {
            long userId = Long.parseLong(req.getParameter("userId"));
            driverDao.deleteDriverProfile(userId);
            res.sendRedirect(req.getContextPath() + "/driver/profile/manage");
        } catch (IllegalArgumentException | IllegalStateException e) {
            showError(req, res, e.getMessage());
        }
    }

    private void showError(HttpServletRequest req, HttpServletResponse res, String message) throws SQLException, ServletException, IOException {
        req.setAttribute("error", message);
        req.setAttribute("drivers", driverDao.findAll());
        req.getRequestDispatcher("/WEB-INF/views/driver-profile-manage.jsp").forward(req, res);
    }

    private boolean requireAdmin(HttpServletRequest req, HttpServletResponse res) throws IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }
}
