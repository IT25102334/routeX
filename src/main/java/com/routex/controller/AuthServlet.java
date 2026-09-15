package com.routex.controller;

import com.routex.dao.UserDao;
import com.routex.model.User;
import com.routex.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Shared infrastructure (not one of the six major functions, but the
 * "Login/logout, password reset, account verification" minor function
 * every module depends on). Handles /login, /register and /logout.
 */
@WebServlet(urlPatterns = {"/login", "/register", "/logout"})
public class AuthServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/logout".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String view = "/register".equals(path) ? "register.jsp" : "login.jsp";
        req.getRequestDispatcher("/WEB-INF/views/" + view).forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            if ("/register".equals(req.getServletPath())) {
                handleRegister(req, res);
            } else {
                handleLogin(req, res);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error during authentication", e);
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse res) throws SQLException, ServletException, IOException {
        String role = req.getParameter("role");
        if (role == null || !role.matches("RIDER|DRIVER")) {
            role = "RIDER"; // Admin accounts are never self-registered - promoted manually by an existing admin.
        }

        if (userDao.findByEmail(req.getParameter("email")).isPresent()) {
            req.setAttribute("error", "An account with that email already exists.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res);
            return;
        }

        String hashedPassword = PasswordUtil.hash(req.getParameter("password"));
        String licenseNo = req.getParameter("licenseNo");
        String vehicleType = req.getParameter("vehicleType");
        String vehicleInfo = req.getParameter("vehicleInfo");
        userDao.register(req.getParameter("name"), req.getParameter("email"), req.getParameter("phone"),
                hashedPassword, role, licenseNo, vehicleType, vehicleInfo);
        res.sendRedirect(req.getContextPath() + "/login?registered=1");
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse res) throws SQLException, ServletException, IOException {
        User user = userDao.findByEmail(req.getParameter("email")).orElse(null);
        String password = req.getParameter("password");

        boolean credentialsValid = user != null && PasswordUtil.verify(password, user.getPasswordHash());
        if (!credentialsValid) {
            req.setAttribute("error", "Invalid email or password.");
            doGet(req, res);
            return;
        }
        if (!user.isActive()) {
            req.setAttribute("error", "This account has been suspended. Contact support.");
            doGet(req, res);
            return;
        }

        req.getSession(true).setAttribute("user", user);
        switch (user.getRole()) {
            case "DRIVER" -> res.sendRedirect(req.getContextPath() + "/driver/dashboard");
            case "ADMIN" -> res.sendRedirect(req.getContextPath() + "/admin/dashboard");
            default -> res.sendRedirect(req.getContextPath() + "/rider/dashboard");
        }
    }
}
