package com.routex.dao;

import com.routex.factory.UserFactory;
import com.routex.model.Driver;
import com.routex.util.DatabaseConnection;
import com.routex.validation.DriverValidator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for driver-specific data (the "drivers" table).
 * Module: Driver Matching & Dispatch (Abeygunawardhana S.N.J. - IT25100107)
 *
 * Full CRUD on the driver profile extension data, each write guarded
 * by DriverValidator so invalid input never reaches the database.
 */
public class DriverDao {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private static final String JOIN_SELECT =
            "SELECT u.*, d.license_no, d.vehicle_type, d.vehicle_info, d.availability, d.verified, d.rating " +
            "FROM users u JOIN drivers d ON d.user_id = u.id ";

    // ---------------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------------

    /**
     * Creates a driver profile for an existing user account (admin
     * onboarding an existing account onto the driver roster).
     */
    public void createDriverProfile(long userId, String licenseNo, String vehicleType, String vehicleInfo) throws SQLException {
        DriverValidator.validateDriverProfile(userId, licenseNo, vehicleType, vehicleInfo);

        if (findById(userId).isPresent()) {
            throw new IllegalStateException("A driver profile already exists for this user.");
        }

        String sql = "INSERT INTO drivers (user_id, license_no, vehicle_type, vehicle_info, availability, verified, rating) " +
                "VALUES (?, ?, ?, ?, 'OFFLINE', 0, 5.00)";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, licenseNo);
            ps.setString(3, vehicleType);
            ps.setString(4, vehicleInfo);
            ps.executeUpdate();
        }
    }

    // ---------------------------------------------------------------
    // READ
    // ---------------------------------------------------------------

    public Optional<Driver> findById(long driverId) throws SQLException {
        String sql = JOIN_SELECT + "WHERE u.id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, driverId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(UserFactory.fromJoinedDriverRow(rs)) : Optional.empty();
            }
        }
    }

    public List<Driver> findAll() throws SQLException {
        List<Driver> drivers = new ArrayList<>();
        try (Connection c = db.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(JOIN_SELECT + "ORDER BY u.id DESC")) {
            while (rs.next()) {
                drivers.add(UserFactory.fromJoinedDriverRow(rs));
            }
        }
        return drivers;
    }

    /** Backward-compatible overload: no ride context, so no rejection exclusion. */
    public Optional<Long> findBestAvailableDriver(String preferredVehicleType) throws SQLException {
        return findBestAvailableDriver(preferredVehicleType, 0);
    }

    /**
     * Core of the Driver Matching & Dispatch module (UC-02): finds the
     * best candidate driver for a given ride - online, admin-verified,
     * active account, not already tied up on another active trip, and
     * not someone who already rejected THIS specific ride - ordered by
     * rating (proximity is simulated since this is an academic
     * prototype; the highest-rated available driver is treated as the
     * "nearest" match).
     *
     * Tries to match the rider's requested vehicle type first. If no
     * driver of that exact vehicle type is free, it falls back to any
     * available driver rather than leaving the rider stranded - a small
     * class-demo driver pool won't always have every vehicle type online.
     *
     * @param rideId pass 0 (or use the single-arg overload) when there is
     *               no specific ride to exclude rejections for.
     */
    public Optional<Long> findBestAvailableDriver(String preferredVehicleType, long rideId) throws SQLException {
        Optional<Long> exactMatch = queryBestAvailableDriver(preferredVehicleType, rideId);
        return exactMatch.isPresent() ? exactMatch : queryBestAvailableDriver(null, rideId);
    }

    private Optional<Long> queryBestAvailableDriver(String vehicleType, long rideId) throws SQLException {
        String sql = "SELECT d.user_id FROM drivers d JOIN users u ON u.id = d.user_id " +
                "WHERE d.availability = 'ONLINE' AND d.verified = 1 AND u.status = 'ACTIVE' " +
                (vehicleType != null ? "AND d.vehicle_type = ? " : "") +
                "AND d.user_id NOT IN (" +
                "  SELECT driver_id FROM rides WHERE driver_id IS NOT NULL " +
                "  AND status IN ('DISPATCHED','ACCEPTED','ARRIVED','IN_PROGRESS')" +
                ") " +
                (rideId > 0 ? "AND d.user_id NOT IN (SELECT driver_id FROM ride_rejections WHERE ride_id = ?) " : "") +
                "ORDER BY d.rating DESC, d.user_id ASC LIMIT 1";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int idx = 1;
            if (vehicleType != null) {
                ps.setString(idx++, vehicleType);
            }
            if (rideId > 0) {
                ps.setLong(idx++, rideId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getLong(1)) : Optional.empty();
            }
        }
    }

    // ---------------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------------

    public void setAvailability(long driverId, String availability) throws SQLException {
        DriverValidator.validateUserId(driverId);
        DriverValidator.validateAvailability(availability);
        String sql = "UPDATE drivers SET availability = ? WHERE user_id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, availability);
            ps.setLong(2, driverId);
            ps.executeUpdate();
        }
    }

    /** Admin edits an existing driver's profile details (license/vehicle). */
    public void updateDriverProfile(long driverId, String licenseNo, String vehicleType, String vehicleInfo) throws SQLException {
        DriverValidator.validateDriverProfile(driverId, licenseNo, vehicleType, vehicleInfo);
        if (findById(driverId).isEmpty()) {
            throw new IllegalStateException("No driver profile exists for this user.");
        }
        String sql = "UPDATE drivers SET license_no = ?, vehicle_type = ?, vehicle_info = ? WHERE user_id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, licenseNo);
            ps.setString(2, vehicleType);
            ps.setString(3, vehicleInfo);
            ps.setLong(4, driverId);
            ps.executeUpdate();
        }
    }

    public void verify(long driverId) throws SQLException {
        DriverValidator.validateUserId(driverId);
        String sql = "UPDATE drivers SET verified = 1 WHERE user_id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, driverId);
            ps.executeUpdate();
        }
    }

    // ---------------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------------

    /**
     * Removes a driver's profile from the roster. Refuses to delete a
     * driver who is currently tied up on an active ride, so an in-progress
     * trip can never lose its driver record out from under it.
     */
    public void deleteDriverProfile(long driverId) throws SQLException {
        DriverValidator.validateUserId(driverId);

        String activeCheckSql = "SELECT COUNT(*) FROM rides WHERE driver_id = ? " +
                "AND status IN ('DISPATCHED','ACCEPTED','ARRIVED','IN_PROGRESS')";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(activeCheckSql)) {
            ps.setLong(1, driverId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    throw new IllegalStateException("Cannot delete: this driver has an active ride in progress.");
                }
            }
        }

        String sql = "DELETE FROM drivers WHERE user_id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, driverId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new IllegalStateException("No driver profile exists for this user.");
            }
        }
    }
}
