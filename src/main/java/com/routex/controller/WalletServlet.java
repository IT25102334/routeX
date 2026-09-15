package com.routex.controller;

import com.routex.dao.WalletDao;
import com.routex.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * ================================================================
 * MODULE: Wallet & Rewards Management
 * OWNER:  Adikarie J.A.D.C. (IT25102349)
 * Implements UC-04 (Manage Wallet Balance, Top-Ups, Loyalty Points).
 * ================================================================
 *
 * Routes:
 *   GET  /rider/wallet - view balance, points and transaction history
 *   POST /rider/wallet - top up the wallet, or redeem loyalty points
 */
@WebServlet(urlPatterns = {"/rider/wallet"})
public class WalletServlet extends HttpServlet {

    private final WalletDao walletDao = new WalletDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        try {
            req.setAttribute("wallet", walletDao.find(user.getId()));
            req.setAttribute("transactions", walletDao.history(user.getId()));
            req.getRequestDispatcher("/WEB-INF/views/wallet.jsp").forward(req, res);
        } catch (SQLException e) {
            throw new ServletException("Failed to load wallet", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        String action = req.getParameter("action"); // TOPUP | REDEEM

        try {
            if ("TOPUP".equals(action)) {
                BigDecimal amount = new BigDecimal(req.getParameter("amount"));
                walletDao.topUp(user.getId(), amount);
            } else if ("REDEEM".equals(action)) {
                int points = Integer.parseInt(req.getParameter("points"));
                boolean redeemed = walletDao.redeemPoints(user.getId(), points);
                if (!redeemed) {
                    req.getSession().setAttribute("walletError", "Not enough points to redeem (minimum 100).");
                }
            }
        } catch (IllegalArgumentException | SQLException e) {
            req.getSession().setAttribute("walletError", "Could not complete that wallet action: " + e.getMessage());
        }
        res.sendRedirect(req.getContextPath() + "/rider/wallet");
    }
}
