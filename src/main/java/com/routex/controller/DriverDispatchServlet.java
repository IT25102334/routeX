package com.routex.controller;

import com.routex.dao.DriverDao;
import com.routex.dao.RideDao;
import com.routex.model.Ride;
import com.routex.model.User;
import com.routex.service.DispatchService;
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
 * Implements UC-02 (Match and Dispatch Driver).
 * ================================================================
 *
 * Routes:
 *   GET  /driver/dashboard    - driver's incoming request + ride history
 *   POST /driver/availability - toggle ONLINE / OFFLINE
 *   POST /driver/respond      - accept or reject a dispatched request
 */
@WebServlet(urlPatterns = {"/driver/dashboard", "/driver/availability", "/driver/respond"})
public class DriverDispatchServlet extends HttpServlet {

    private final RideDao rideDao = new RideDao();
    private final DriverDao driverDao = new DriverDao();
    private final DispatchService dispatchService = new DispatchService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User user = currentUser(req);
        if (!"DRIVER".equals(user.getRole())) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try {
            List<Ride> rides = rideDao.findByDriver(user.getId());
            req.setAttribute("rides", rides);
            req.setAttribute("driver", driverDao.findById(user.getId()).orElse(null));
            req.getRequestDispatcher("/WEB-INF/views/driver-dashboard.jsp").forward(req, res);
        } catch (SQLException e) {
            throw new ServletException("Failed to load driver dashboard", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User user = currentUser(req);
        try {
            if (req.getServletPath().endsWith("availability")) {
                // UC: "Update Availability Status" (Lab02 PBI-08) - only ONLINE drivers receive requests.
                String availability = req.getParameter("availability");
                driverDao.setAvailability(user.getId(), availability);

                // Give a ride that was left stranded (no driver was available when it
                // was booked) a second chance now that a new driver just came online.
                if ("ONLINE".equals(availability)) {
                    dispatchService.retryOldestPendingRide();
                }
            } else {
                handleRespond(req, user);
            }
            res.sendRedirect(req.getContextPath() + "/driver/dashboard");
        } catch (SQLException e) {
            throw new ServletException("Failed to update dispatch state", e);
        }
    }

    private void handleRespond(HttpServletRequest req, User user) throws SQLException {
        long rideId = Long.parseLong(req.getParameter("rideId"));
        String decision = req.getParameter("decision"); // ACCEPT | REJECT

        Ride ride = rideDao.findById(rideId).orElse(null);
        if (ride == null || ride.getDriverId() == null || ride.getDriverId() != user.getId()) {
            return; // not this driver's request to answer
        }

        if ("ACCEPT".equals(decision)) {
            // UC-02 step 6: system updates the ride status to ACCEPTED and links the driver.
            rideDao.markAccepted(rideId);
        } else {
            // UC-02 extension 5a: driver declines - re-open the request and try the next best driver.
            rideDao.revertToRequested(rideId);
            dispatchService.dispatchRide(rideId);
        }
    }

    private User currentUser(HttpServletRequest req) {
        return (User) req.getSession().getAttribute("user");
    }
}
