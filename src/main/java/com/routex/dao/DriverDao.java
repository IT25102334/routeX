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
            "SELECT u.*, d.license_no, d.vehicle_type, d.vehicle_info, d.availability, d.verified, d.rating, d.current_lat, d.current_lng " +
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

    /** Driver Matching & Dispatch: records where a driver currently is, so dispatch can find the nearest one. */
    public void updateLocation(long driverId, double lat, double lng) throws SQLException {
        String sql = "UPDATE drivers SET current_lat = ?, current_lng = ? WHERE user_id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, lat);
            ps.setDouble(2, lng);
            ps.setLong(3, driverId);
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
     * active account, not already tied up on another active trip.
     *
     * When the ride has a pickup location and at least one eligible
     * driver has reported their own location, candidates are ranked by
     * real straight-line distance (Haversine) to the pickup point - the
     * closest driver wins. If no location data is available for the
     * ride or for any candidate, this falls back to the original
     * highest-rated-driver rule so the app still works without GPS data.
     *
     * Tries to match the rider's requested vehicle type first. If no
     * driver of that exact vehicle type is free, it falls back to any
     * available driver rather than leaving the rider stranded - a small
     * class-demo driver pool won't always have every vehicle type online.
     */
    public Optional<Long> findBestAvailableDriver(String preferredVehicleType) throws SQLException {
        return findBestAvailableDriver(preferredVehicleType, null, null);
    }

    public Optional<Long> findBestAvailableDriver(String preferredVehicleType, Double pickupLat, Double pickupLng) throws SQLException {
        Optional<Long> exactMatch = queryBestAvailableDriver(preferredVehicleType, pickupLat, pickupLng);
        return exactMatch.isPresent() ? exactMatch : queryBestAvailableDriver(null, pickupLat, pickupLng);
    }

    private Optional<Long> queryBestAvailableDriver(String vehicleType, Double pickupLat, Double pickupLng) throws SQLException {
        String sql = "SELECT d.user_id, d.rating, d.current_lat, d.current_lng FROM drivers d JOIN users u ON u.id = d.user_id " +
                "WHERE d.availability = 'ONLINE' AND d.verified = 1 AND u.status = 'ACTIVE' " +
                (vehicleType != null ? "AND d.vehicle_type = ? " : "") +
                "AND d.user_id NOT IN (" +
                "  SELECT driver_id FROM rides WHERE driver_id IS NOT NULL " +
                "  AND status IN ('DISPATCHED','ACCEPTED','ARRIVED','IN_PROGRESS')" +
                ")";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (vehicleType != null) {
                ps.setString(1, vehicleType);
            }
            try (ResultSet rs = ps.executeQuery()) {
                Long bestId = null;
                double bestDistance = Double.MAX_VALUE;
                java.math.BigDecimal bestRating = null;
                long bestRatingId = -1;

                boolean canUseDistance = (pickupLat != null && pickupLng != null);

                while (rs.next()) {
                    long candidateId = rs.getLong("user_id");
                    java.math.BigDecimal rating = rs.getBigDecimal("rating");

                    // Track the highest-rated driver as a fallback in case no one has a location set.
                    if (bestRating == null || rating.compareTo(bestRating) > 0) {
                        bestRating = rating;
                        bestRatingId = candidateId;
                    }

                    if (canUseDistance) {
                        double dLat = rs.getDouble("current_lat");
                        boolean hasLat = !rs.wasNull();
                        double dLng = rs.getDouble("current_lng");
                        boolean hasLng = !rs.wasNull();
                        if (hasLat && hasLng) {
                            double distance = com.routex.util.GeoUtils.distanceKm(pickupLat, pickupLng, dLat, dLng);
                            if (distance < bestDistance) {
                                bestDistance = distance;
                                bestId = candidateId;
                            }
                        }
                    }
                }

                if (bestId != null) {
                    return Optional.of(bestId);
                }
                return bestRatingId != -1 ? Optional.of(bestRatingId) : Optional.empty();
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