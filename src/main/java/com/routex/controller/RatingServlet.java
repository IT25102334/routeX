package com.routex.controller;

import com.routex.dao.RatingDao;
import com.routex.dao.RideDao;
import com.routex.model.Ride;
import com.routex.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * ================================================================
 * MODULE: Ratings, Reviews & Driver Performance
 * OWNER:  Gopiga A. (IT25102335)
 * Implements UC-05 (Rate, Review and Monitor Driver Performance).
 * ================================================================
 *
 * Routes:
 *   GET  /rider/rate  - show the post-ride rating form
 *   POST /rider/rate  - submit a rider's rating of their driver
 */
@WebServlet(urlPatterns = {"/rider/rate", "/rider/reviews", "/rider/rate-delete"})
public class RatingServlet extends HttpServlet {

    private final RatingDao ratingDao = new RatingDao();
    private final RideDao rideDao = new RideDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            if (req.getServletPath().endsWith("reviews")) {
                // Read: a rider's own submitted reviews (completes CRUD visibility for this module).
                User rider = (User) req.getSession().getAttribute("user");
                req.setAttribute("reviews", ratingDao.findSubmittedBy(rider.getId()));
                req.getRequestDispatcher("/WEB-INF/views/reviews.jsp").forward(req, res);
            } else {
                long rideId = Long.parseLong(req.getParameter("rideId"));
                req.setAttribute("ride", rideDao.findById(rideId).orElse(null));
                req.getRequestDispatcher("/WEB-INF/views/rate.jsp").forward(req, res);
            }
        } catch (SQLException e) {
            throw new ServletException("Failed to load ratings data", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User rider = (User) req.getSession().getAttribute("user");
        try {
            if (req.getServletPath().endsWith("rate-delete")) {
                // Delete: retract a review you submitted by mistake.
                long ratingId = Long.parseLong(req.getParameter("ratingId"));
                ratingDao.delete(ratingId, rider.getId());
                res.sendRedirect(req.getContextPath() + "/rider/reviews");
                return;
            }

            long rideId = Long.parseLong(req.getParameter("rideId"));
            Ride ride = rideDao.findById(rideId).orElse(null);

            // UC-05 precondition: ride must be completed and must have a driver to rate.
            if (ride != null && "COMPLETED".equals(ride.getStatus()) && ride.getDriverId() != null) {
                int stars = Integer.parseInt(req.getParameter("stars"));
                String review = req.getParameter("review");
                ratingDao.submit(rideId, rider.getId(), ride.getDriverId(), stars, review);
            }
            res.sendRedirect(req.getContextPath() + "/rider/dashboard");
        } catch (SQLException e) {
            throw new ServletException("Failed to process rating", e);
        }
    }
}
