package com.routex.dao;

import com.routex.model.Ride;
import com.routex.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for the "rides" table. Touched by Ride Booking & Fare
 * Estimation (create), Driver Matching & Dispatch (assignDriver) and
 * Live Trip Management (updateStatus / updateLocation / SOS).
 */
public class RideDao {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public long create(long riderId, String pickup, String dropoff, String rideType, String vehicleType, BigDecimal estimatedFare) throws SQLException {
        return create(riderId, pickup, dropoff, rideType, vehicleType, estimatedFare, null, null);
    }

    public long create(long riderId, String pickup, String dropoff, String rideType, String vehicleType, BigDecimal estimatedFare, Double pickupLat, Double pickupLng) throws SQLException {
        String sql = "INSERT INTO rides(rider_id, pickup, dropoff, ride_type, vehicle_type, estimated_fare, pickup_lat, pickup_lng, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'REQUESTED')";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, riderId);
            ps.setString(2, pickup);
            ps.setString(3, dropoff);
            ps.setString(4, rideType);
            ps.setString(5, vehicleType);
            ps.setBigDecimal(6, estimatedFare);
            if (pickupLat != null) ps.setDouble(7, pickupLat); else ps.setNull(7, java.sql.Types.DECIMAL);
            if (pickupLng != null) ps.setDouble(8, pickupLng); else ps.setNull(8, java.sql.Types.DECIMAL);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    /** Driver Matching & Dispatch: link a driver to a ride and mark it dispatched, awaiting acceptance. */
    public void dispatchToDriver(long rideId, long driverId) throws SQLException {
        String sql = "UPDATE rides SET driver_id = ?, status = 'DISPATCHED' WHERE id = ? AND status = 'REQUESTED'";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, driverId);
            ps.setLong(2, rideId);
            ps.executeUpdate();
        }
    }

    /** Driver accepts a dispatched ride. */
    public void markAccepted(long rideId) throws SQLException {
        updateStatus(rideId, "ACCEPTED");
    }

    /** Driver rejects a dispatched ride - free it up so it can be re-dispatched to the next driver. */
    public void revertToRequested(long rideId) throws SQLException {
        String sql = "UPDATE rides SET driver_id = NULL, status = 'REQUESTED' WHERE id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, rideId);
            ps.executeUpdate();
        }
    }

    public void updateStatus(long rideId, String status) throws SQLException {
        String sql = "UPDATE rides SET status = ?, " +
                "started_at = CASE WHEN ? = 'IN_PROGRESS' THEN COALESCE(started_at, NOW()) ELSE started_at END, " +
                "completed_at = CASE WHEN ? = 'COMPLETED' THEN COALESCE(completed_at, NOW()) ELSE completed_at END " +
                "WHERE id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, status);
            ps.setString(3, status);
            ps.setLong(4, rideId);
            ps.executeUpdate();
        }
    }

    public void updateLocation(long rideId, double lat, double lng) throws SQLException {
        String sql = "UPDATE rides SET current_lat = ?, current_lng = ? WHERE id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, lat);
            ps.setDouble(2, lng);
            ps.setLong(3, rideId);
            ps.executeUpdate();
        }
    }

    public void triggerSos(long rideId) throws SQLException {
        String sql = "UPDATE rides SET sos_triggered = 1 WHERE id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, rideId);
            ps.executeUpdate();
        }
    }

    public Optional<Ride> findById(long id) throws SQLException {
        String sql = "SELECT * FROM rides WHERE id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<Ride> findByRider(long riderId) throws SQLException {
        return queryList("SELECT * FROM rides WHERE rider_id = ? ORDER BY id DESC", riderId);
    }

    public List<Ride> findByDriver(long driverId) throws SQLException {
        return queryList("SELECT * FROM rides WHERE driver_id = ? ORDER BY id DESC", driverId);
    }

    /** Rides waiting to be dispatched (used by Driver Matching & Dispatch). */
    public List<Ride> findRequested() throws SQLException {
        List<Ride> rides = new ArrayList<>();
        try (Connection c = db.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM rides WHERE status = 'REQUESTED' ORDER BY id ASC")) {
            while (rs.next()) {
                rides.add(map(rs));
            }
        }
        return rides;
    }

    public List<Ride> findAll() throws SQLException {
        List<Ride> rides = new ArrayList<>();
        try (Connection c = db.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM rides ORDER BY id DESC")) {
            while (rs.next()) {
                rides.add(map(rs));
            }
        }
        return rides;
    }

    private List<Ride> queryList(String sql, long id) throws SQLException {
        List<Ride> rides = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rides.add(map(rs));
                }
            }
        }
        return rides;
    }

    private Ride map(ResultSet rs) throws SQLException {
        Ride ride = new Ride();
        ride.setId(rs.getLong("id"));
        ride.setRiderId(rs.getLong("rider_id"));

        long driverId = rs.getLong("driver_id");
        ride.setDriverId(rs.wasNull() ? null : driverId);

        ride.setPickup(rs.getString("pickup"));
        ride.setDropoff(rs.getString("dropoff"));
        ride.setRideType(rs.getString("ride_type"));
        ride.setVehicleType(rs.getString("vehicle_type"));
        ride.setStatus(rs.getString("status"));
        ride.setEstimatedFare(rs.getBigDecimal("estimated_fare"));
        ride.setFinalFare(rs.getBigDecimal("final_fare"));
        ride.setSosTriggered(rs.getBoolean("sos_triggered"));
        ride.setRequestedAt(rs.getTimestamp("requested_at"));
        ride.setStartedAt(rs.getTimestamp("started_at"));
        ride.setCompletedAt(rs.getTimestamp("completed_at"));

        double lat = rs.getDouble("current_lat");
        ride.setCurrentLat(rs.wasNull() ? null : lat);
        double lng = rs.getDouble("current_lng");
        ride.setCurrentLng(rs.wasNull() ? null : lng);

        double plat = rs.getDouble("pickup_lat");
        ride.setPickupLat(rs.wasNull() ? null : plat);
        double plng = rs.getDouble("pickup_lng");
        ride.setPickupLng(rs.wasNull() ? null : plng);

        return ride;
    }
}