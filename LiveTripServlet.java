package com.routex.controller;

import com.routex.dao.RideDao;
import com.routex.dao.WalletDao;
import com.routex.model.Ride;
import com.routex.model.User;
import com.routex.observer.NotificationObserver;
import com.routex.observer.RideSubject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * ================================================================
 * MODULE: Live Trip Management
 * OWNER:  Halloluwa T.T. (IT25100099)
 * Implements UC-03 (Track and Update Trip Status).
 * Uses the Observer pattern (see com.routex.observer) to broadcast
 * every status change / SOS alert without coupling this servlet to
 * however notifications end up being delivered.
 * ================================================================
 *
 * Routes:
 *   GET  /rider/trip   - rider's live view of an in-progress ride
 *   POST /rider/sos    - rider triggers an SOS alert mid-trip
 *   GET  /driver/trip  - driver's active-trip control panel
 *   POST /driver/trip  - driver marks arrived / starts / sends location / completes
 */
@WebServlet(urlPatterns = {"/rider/trip", "/rider/sos", "/driver/trip"})
public class LiveTripServlet extends HttpServlet {

    private final RideDao rideDao = new RideDao();
    private final WalletDao walletDao = new WalletDao();

    // Subject/observer wiring done once - this is the whole Observer pattern in action.
    private final RideSubject rideSubject = new RideSubject();

    public LiveTripServlet() {
        rideSubject.addObserver(new NotificationObserver());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User user = currentUser(req);
        try {
            long rideId = Long.parseLong(req.getParameter("id"));
            Ride ride = rideDao.findById(rideId).orElse(null);
            if (ride == null) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            req.setAttribute("ride", ride);

            if (req.getServletPath().startsWith("/driver")) {
                req.getRequestDispatcher("/WEB-INF/views/driver-trip.jsp").forward(req, res);
            } else {
                req.getRequestDispatcher("/WEB-INF/views/trip.jsp").forward(req, res);
            }
        } catch (SQLException e) {
            throw new ServletException("Failed to load trip", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            if (req.getServletPath().endsWith("sos")) {
                handleSos(req);
                res.sendRedirect(req.getContextPath() + "/rider/trip?id=" + req.getParameter("rideId"));
            } else {
                handleDriverAction(req);
                res.sendRedirect(req.getContextPath() + "/driver/trip?id=" + req.getParameter("rideId"));
            }
        } catch (SQLException e) {
            throw new ServletException("Failed to update trip", e);
        }
    }

    // UC-03 extension 5a: rider triggers SOS mid-trip without interrupting tracking.
    private void handleSos(HttpServletRequest req) throws SQLException {
        long rideId = Long.parseLong(req.getParameter("rideId"));
        rideDao.triggerSos(rideId);
        Ride ride = rideDao.findById(rideId).orElseThrow();
        rideSubject.notifySos(ride);
    }

    private void handleDriverAction(HttpServletRequest req) throws SQLException {
        long rideId = Long.parseLong(req.getParameter("rideId"));
        String action = req.getParameter("action"); // ARRIVED | START | LOCATION | COMPLETE

        switch (action) {
            case "ARRIVED" -> transition(rideId, "ARRIVED");     // UC-03 step 2-3
            case "START" -> transition(rideId, "IN_PROGRESS");   // UC-03 step 4
            case "LOCATION" -> rideDao.updateLocation(rideId,    // UC-03 step 5 (live tracking)
                    Double.parseDouble(req.getParameter("lat")),
                    Double.parseDouble(req.getParameter("lng")));
            case "COMPLETE" -> completeTrip(rideId);             // UC-03 steps 6-8
            default -> { /* ignore unknown action */ }
        }
    }

    private void transition(long rideId, String newStatus) throws SQLException {
        rideDao.updateStatus(rideId, newStatus);
        Ride ride = rideDao.findById(rideId).orElseThrow();
        rideSubject.notifyStatusChanged(ride, newStatus);
    }

    private void completeTrip(long rideId) throws SQLException {
        rideDao.updateStatus(rideId, "COMPLETED");
        Ride ride = rideDao.findById(rideId).orElseThrow();

        // UC-03 step 8: hand trip data to Payment (Wallet & Rewards) and Ratings.
        java.math.BigDecimal finalFare = ride.getEstimatedFare();
        walletDao.payForRide(ride.getRiderId(), finalFare, rideId);
        walletDao.addRewardPoints(ride.getRiderId(), WalletDao.POINTS_EARNED_PER_COMPLETED_RIDE);

        rideSubject.notifyStatusChanged(ride, "COMPLETED");
    }

    private User currentUser(HttpServletRequest req) {
        return (User) req.getSession().getAttribute("user");
    }
}
