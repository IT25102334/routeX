package com.routex.dao;

import com.routex.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for Admin & Reporting module.
 * Adjust table/column names below to match your actual schema.sql.
 */
public class AdminDao {

    // ---------- USER MANAGEMENT ----------

    public List<UserRow> getAllUsers() {
        List<UserRow> users = new ArrayList<>();
        String sql = "SELECT id, name, email, role, status FROM users";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(new UserRow(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean updateUserStatus(int userId, String newStatus) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------- RIDE REPORTS ----------

    public List<RideRow> getAllRides() {
        List<RideRow> rides = new ArrayList<>();
        String sql = "SELECT id, rider_id, driver_id, status, fare, created_at FROM rides ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rides.add(new RideRow(
                        rs.getInt("id"),
                        rs.getInt("rider_id"),
                        rs.getInt("driver_id"),
                        rs.getString("status"),
                        rs.getDouble("fare"),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rides;
    }

    // ---------- REVENUE / SUMMARY REPORTS ----------

    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(fare), 0) AS total FROM rides WHERE status = 'COMPLETED'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getDouble("total");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public int getTotalRideCount() {
        String sql = "SELECT COUNT(*) AS cnt FROM rides";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt("cnt");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<TopDriverRow> getTopDrivers(int limit) {
        List<TopDriverRow> topDrivers = new ArrayList<>();
        String sql = "SELECT driver_id, COUNT(*) AS trips, SUM(fare) AS earnings " +
                     "FROM rides WHERE status = 'COMPLETED' " +
                     "GROUP BY driver_id ORDER BY earnings DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    topDrivers.add(new TopDriverRow(
                            rs.getInt("driver_id"),
                            rs.getInt("trips"),
                            rs.getDouble("earnings")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topDrivers;
    }

    // ---------- Simple row/DTO classes ----------

    public static class UserRow {
        public int id;
        public String name, email, role, status;
        public UserRow(int id, String name, String email, String role, String status) {
            this.id = id; this.name = name; this.email = email;
            this.role = role; this.status = status;
        }
    }

    public static class RideRow {
        public int id, riderId, driverId;
        public String status;
        public double fare;
        public Timestamp createdAt;
        public RideRow(int id, int riderId, int driverId, String status, double fare, Timestamp createdAt) {
            this.id = id; this.riderId = riderId; this.driverId = driverId;
            this.status = status; this.fare = fare; this.createdAt = createdAt;
        }
    }

    public static class TopDriverRow {
        public int driverId, trips;
        public double earnings;
        public TopDriverRow(int driverId, int trips, double earnings) {
            this.driverId = driverId; this.trips = trips; this.earnings = earnings;
        }
    }
}
