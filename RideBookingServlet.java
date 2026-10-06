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
import java.util.List;
import java.util.Set;

/**
 * ================================================================
 * MODULE: Ride Booking & Fare Estimation
 * OWNER:  Moses A.C.N. (IT25102334)
 * Implements UC-01 (Book Ride, Estimate Fare, Cancel/Modify Booking).
 * ================================================================
 *
 * Routes:
 *   GET  /rider/dashboard  - list this rider's rides
 *   GET  /rider/book       - show the booking form
 *   POST /rider/book       - validate input, estimate fare, create the
 *                            ride, then hand it off to Driver Matching
 *   POST /rider/cancel     - cancel a ride before a driver has accepted
 *   POST /rider/delete     - permanently remove a ride that is already
 *                            CANCELLED (or still REQUESTED)
 *   GET  /rider/edit       - show the edit form, pre-filled with the
 *                            ride's current details (REQUESTED only)
 *   POST /rider/edit       - validate and apply the edit, recalculating
 *                            the fare for the new details
 */
@WebServlet(urlPatterns = {"/rider/dashboard", "/rider/book", "/rider/cancel", "/rider/delete", "/rider/edit"})
public class RideBookingServlet extends HttpServlet {

    private final RideDao rideDao = new RideDao();
    private final FareCalculator fareCalculator = new FareCalculator();
    private final DispatchService dispatchService = new DispatchService();

    private static final Set<String> VALID_RIDE_TYPES = Set.of("STANDARD", "PREMIUM", "POOL");
    private static final Set<String> VALID_VEHICLE_TYPES = Set.of("CAR", "VAN", "TUK_TUK", "BIKE");
    private static final int MAX_ADDRESS_LENGTH = 100;

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
            } else if (req.getServletPath().endsWith("edit")) {
                handleEditForm(req, res, user);
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
            } else if (req.getServletPath().endsWith("delete")) {
                handleDelete(req, res, user);
            } else if (req.getServletPath().endsWith("edit")) {
                handleEditSubmit(req, res, user);
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

        if (isBlank(pickup) || isBlank(dropoff) || isBlank(rideType) || isBlank(vehicleType)) {
            showBookingError(req, res, "Please fill in pickup, drop-off, ride type and vehicle type.");
            return;
        }

        pickup = pickup.trim();
        dropoff = dropoff.trim();

        if (pickup.equalsIgnoreCase(dropoff)) {
            showBookingError(req, res, "Pickup and drop-off cannot be the same location.");
            return;
        }
        if (pickup.length() > MAX_ADDRESS_LENGTH || dropoff.length() > MAX_ADDRESS_LENGTH) {
            showBookingError(req, res, "Pickup and drop-off must each be under " + MAX_ADDRESS_LENGTH + " characters.");
            return;
        }
        if (!VALID_RIDE_TYPES.contains(rideType)) {
            showBookingError(req, res, "Invalid ride type selected.");
            return;
        }
        if (!VALID_VEHICLE_TYPES.contains(vehicleType)) {
            showBookingError(req, res, "Invalid vehicle type selected.");
            return;
        }

        List<Ride> existing = rideDao.findByRider(user.getId());
        for (Ride r : existing) {
            boolean stillActive = "REQUESTED".equals(r.getStatus()) || "DISPATCHED".equals(r.getStatus());
            if (stillActive && r.getPickup().equalsIgnoreCase(pickup) && r.getDropoff().equalsIgnoreCase(dropoff)) {
                showBookingError(req, res, "You already have an active ride requested for this route.");
                return;
            }
        }

        BigDecimal estimatedFare = fareCalculator.estimate(pickup, dropoff, rideType);
        long rideId = rideDao.create(user.getId(), pickup, dropoff, rideType, vehicleType, estimatedFare);
        dispatchService.dispatchRide(rideId);

        res.sendRedirect(req.getContextPath() + "/rider/trip?id=" + rideId);
    }

    private void handleCancel(HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException {
        long rideId = Long.parseLong(req.getParameter("rideId"));
        Ride ride = rideDao.findById(rideId).orElse(null);

        if (ride != null && ("REQUESTED".equals(ride.getStatus()) || "DISPATCHED".equals(ride.getStatus()))) {
            rideDao.updateStatus(rideId, "CANCELLED");
        }
        res.sendRedirect(req.getContextPath() + "/rider/dashboard");
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse res, User user) throws SQLException, IOException {
        long rideId = Long.parseLong(req.getParameter("rideId"));
        Ride ride = rideDao.findById(rideId).orElse(null);

        if (ride != null && ride.getRiderId() == user.getId()
                && ("CANCELLED".equals(ride.getStatus()) || "REQUESTED".equals(ride.getStatus()))) {
            rideDao.delete(rideId);
        }
        res.sendRedirect(req.getContextPath() + "/rider/dashboard");
    }

    /**
     * UC-01 Modify Booking (GET): shows the edit form pre-filled with the
     * ride's current details. Only a REQUESTED ride owned by this rider can
     * be edited - once a driver is dispatched/accepted, details are locked.
     */
    private void handleEditForm(HttpServletRequest req, HttpServletResponse res, User user)
            throws SQLException, ServletException, IOException {
        long rideId = Long.parseLong(req.getParameter("id"));
        Ride ride = rideDao.findById(rideId).orElse(null);

        if (ride == null || ride.getRiderId() != user.getId() || !"REQUESTED".equals(ride.getStatus())) {
            res.sendRedirect(req.getContextPath() + "/rider/dashboard");
            return;
        }
        req.setAttribute("ride", ride);
        req.getRequestDispatcher("/WEB-INF/views/edit-ride.jsp").forward(req, res);
    }

    /**
     * UC-01 Modify Booking (POST): re-runs every booking com.routex.validation against
     * the edited values, then UPDATEs the existing ride row in place and
     * recalculates its fare - rather than deleting and recreating it, which
     * would lose the ride's original id/history.
     */
    private void handleEditSubmit(HttpServletRequest req, HttpServletResponse res, User user)
            throws SQLException, ServletException, IOException {

        long rideId = Long.parseLong(req.getParameter("rideId"));
        Ride ride = rideDao.findById(rideId).orElse(null);

        if (ride == null || ride.getRiderId() != user.getId() || !"REQUESTED".equals(ride.getStatus())) {
            res.sendRedirect(req.getContextPath() + "/rider/dashboard");
            return;
        }

        String pickup = req.getParameter("pickup");
        String dropoff = req.getParameter("dropoff");
        String rideType = req.getParameter("rideType");
        String vehicleType = req.getParameter("vehicleType");

        if (isBlank(pickup) || isBlank(dropoff) || isBlank(rideType) || isBlank(vehicleType)) {
            showEditError(req, res, ride, "Please fill in pickup, drop-off, ride type and vehicle type.");
            return;
        }

        pickup = pickup.trim();
        dropoff = dropoff.trim();

        if (pickup.equalsIgnoreCase(dropoff)) {
            showEditError(req, res, ride, "Pickup and drop-off cannot be the same location.");
            return;
        }
        if (pickup.length() > MAX_ADDRESS_LENGTH || dropoff.length() > MAX_ADDRESS_LENGTH) {
            showEditError(req, res, ride, "Pickup and drop-off must each be under " + MAX_ADDRESS_LENGTH + " characters.");
            return;
        }
        if (!VALID_RIDE_TYPES.contains(rideType)) {
            showEditError(req, res, ride, "Invalid ride type selected.");
            return;
        }
        if (!VALID_VEHICLE_TYPES.contains(vehicleType)) {
            showEditError(req, res, ride, "Invalid vehicle type selected.");
            return;
        }

        BigDecimal newFare = fareCalculator.estimate(pickup, dropoff, rideType);
        rideDao.update(rideId, pickup, dropoff, rideType, vehicleType, newFare);

        res.sendRedirect(req.getContextPath() + "/rider/dashboard");
    }

    private void showBookingError(HttpServletRequest req, HttpServletResponse res, String message)
            throws ServletException, IOException {
        req.setAttribute("error", message);
        req.getRequestDispatcher("/WEB-INF/views/booking.jsp").forward(req, res);
    }

    private void showEditError(HttpServletRequest req, HttpServletResponse res, Ride ride, String message)
            throws ServletException, IOException {
        req.setAttribute("error", message);
        req.setAttribute("ride", ride);
        req.getRequestDispatcher("/WEB-INF/views/edit-ride.jsp").forward(req, res);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private User currentUser(HttpServletRequest req) {
        return (User) req.getSession().getAttribute("user");
    }
}
