package com.routex.controller;

import com.routex.dao.RideDao;
import com.routex.model.Ride;
import com.routex.model.User;
import com.routex.service.DispatchService;
import com.routex.strategy.FareCalculator;
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
 * MODULE: Ride Booking & Fare Estimation
 * OWNER:  Moses A.C.N. (IT25102334)
 * Implements UC-01 (Book Ride and Estimate Fare).
 * ================================================================
 *
 * Routes:
 *   GET  /rider/dashboard  - list this rider's rides
 *   GET  /rider/book       - show the booking form
 *   POST /rider/book       - validate input, estimate fare, create the
 *                            ride, then hand it off to Driver Matching
 *   POST /rider/cancel     - cancel a ride before a driver has accepted
 */
@WebServlet(urlPatterns = {"/rider/dashboard", "/rider/book", "/rider/cancel"})
public class RideBookingServlet extends HttpServlet {

    private final RideDao rideDao = new RideDao();
    private final FareCalculator fareCalculator = new FareCalculator();
    private final DispatchService dispatchService = new DispatchService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User user = currentUser(req);
        if (!"RIDER".equals(user.getRole())) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try {
            if (req.getServletPath().endsWith("book")) {
                req.getRequestDispatcher("/WEB-INF/views/booking.jsp").forward(req, res);
            } else {
                req.setAttribute("rides", rideDao.findByRider(user.getId()));
                req.getRequestDispatcher("/WEB-INF/views/rider-dashboard.jsp").forward(req, res);
            }
        } catch (SQLException e) {
            throw new ServletException("Failed to load rider data", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User user = currentUser(req);
        try {
            if (req.getServletPath().endsWith("cancel")) {
                handleCancel(req, res);
            } else {
                handleBooking(req, res, user);
            }
        } catch (SQLException e) {
            throw new ServletException("Failed to process booking", e);
        }
    }

    private void handleBooking(HttpServletRequest req, HttpServletResponse res, User user)
            throws SQLException, ServletException, IOException {

        String pickup = req.getParameter("pickup");
        String dropoff = req.getParameter("dropoff");
        String rideType = req.getParameter("rideType");
        String vehicleType = req.getParameter("vehicleType");
        Double pickupLat = parseOrNull(req.getParameter("lat"));
        Double pickupLng = parseOrNull(req.getParameter("lng"));

        // UC-01 extension 2a: reject an incomplete booking form.
        if (isBlank(pickup) || isBlank(dropoff) || isBlank(rideType) || isBlank(vehicleType)) {
            req.setAttribute("error", "Please fill in pickup, drop-off, ride type and vehicle type.");
            req.getRequestDispatcher("/WEB-INF/views/booking.jsp").forward(req, res);
            return;
        }

        // UC-01 step 3: system calculates the fare (peak-hour surge handled by the Strategy classes).
        BigDecimal estimatedFare = fareCalculator.estimate(pickup, dropoff, rideType);

        // UC-01 steps 5-6: rider confirms, ride record is created with status REQUESTED.
        long rideId = rideDao.create(user.getId(), pickup, dropoff, rideType, vehicleType, estimatedFare, pickupLat, pickupLng);
        // Trigger UC-02: hand the freshly-requested ride to Driver Matching & Dispatch.
        dispatchService.dispatchRide(rideId);

        res.sendRedirect(req.getContextPath() + "/rider/trip?id=" + rideId);
    }

    private void handleCancel(HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException {
        long rideId = Long.parseLong(req.getParameter("rideId"));
        Ride ride = rideDao.findById(rideId).orElse(null);

        // UC-01 extension 5a: only an un-matched ride can be cancelled this way.
        if (ride != null && ("REQUESTED".equals(ride.getStatus()) || "DISPATCHED".equals(ride.getStatus()))) {
            rideDao.updateStatus(rideId, "CANCELLED");
        }
        res.sendRedirect(req.getContextPath() + "/rider/dashboard");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Double parseOrNull(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try { return Double.parseDouble(value); } catch (NumberFormatException e) { return null; }
    }

    private User currentUser(HttpServletRequest req) {
        return (User) req.getSession().getAttribute("user");
    }
}
