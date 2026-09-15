package com.routex.dao;

import com.routex.factory.UserFactory;
import com.routex.model.Driver;
import com.routex.util.DatabaseConnection;

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
 */
public class DriverDao {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private static final String JOIN_SELECT =
            "SELECT u.*, d.license_no, d.vehicle_type, d.vehicle_info, d.availability, d.verified, d.rating " +
            "FROM users u JOIN drivers d ON d.user_id = u.id ";

    public void setAvailability(long driverId, String availability) throws SQLException {
        String sql = "UPDATE drivers SET availability = ? WHERE user_id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, availability);
            ps.setLong(2, driverId);
            ps.executeUpdate();
        }
    }

    public void verify(long driverId) throws SQLException {
        String sql = "UPDATE drivers SET verified = 1 WHERE user_id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, driverId);
            ps.executeUpdate();
        }
    }

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

    /**
     * Core of the Driver Matching & Dispatch module (UC-02): finds the
     * best candidate driver for a given ride - online, admin-verified,
     * active account, not already tied up on another active trip -
     * ordered by rating (proximity is simulated since this is an
     * academic prototype; the highest-rated available driver is treated
     * as the "nearest" match).
     *
     * Tries to match the rider's requested vehicle type first. If no
     * driver of that exact vehicle type is free, it falls back to any
     * available driver rather than leaving the rider stranded - a small
     * class-demo driver pool won't always have every vehicle type online.
     */
    public Optional<Long> findBestAvailableDriver(String preferredVehicleType) throws SQLException {
        Optional<Long> exactMatch = queryBestAvailableDriver(preferredVehicleType);
        return exactMatch.isPresent() ? exactMatch : queryBestAvailableDriver(null);
    }

    private Optional<Long> queryBestAvailableDriver(String vehicleType) throws SQLException {
        String sql = "SELECT d.user_id FROM drivers d JOIN users u ON u.id = d.user_id " +
                "WHERE d.availability = 'ONLINE' AND d.verified = 1 AND u.status = 'ACTIVE' " +
                (vehicleType != null ? "AND d.vehicle_type = ? " : "") +
                "AND d.user_id NOT IN (" +
                "  SELECT driver_id FROM rides WHERE driver_id IS NOT NULL " +
                "  AND status IN ('DISPATCHED','ACCEPTED','ARRIVED','IN_PROGRESS')" +
                ") " +
                "ORDER BY d.rating DESC, d.user_id ASC LIMIT 1";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (vehicleType != null) {
                ps.setString(1, vehicleType);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getLong(1)) : Optional.empty();
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
}
