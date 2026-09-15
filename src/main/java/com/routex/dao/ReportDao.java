package com.routex.dao;

import com.routex.model.AdminReport;
import com.routex.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data access for the Administration & Reporting module
 * (Nimnadi R.D.S. - IT25100122). Backs UC-06 (View System Analytics & Reports).
 */
public class ReportDao {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    /** Platform-wide numbers shown on the admin dashboard. */
    public Map<String, Object> platformMetrics() throws SQLException {
        Map<String, Object> metrics = new LinkedHashMap<>();
        try (Connection c = db.getConnection(); Statement st = c.createStatement()) {
            metrics.put("totalUsers", scalarLong(st, "SELECT COUNT(*) FROM users"));
            metrics.put("totalRiders", scalarLong(st, "SELECT COUNT(*) FROM users WHERE role='RIDER'"));
            metrics.put("totalDrivers", scalarLong(st, "SELECT COUNT(*) FROM users WHERE role='DRIVER'"));
            metrics.put("onlineDrivers", scalarLong(st, "SELECT COUNT(*) FROM drivers WHERE availability='ONLINE'"));
            metrics.put("totalRides", scalarLong(st, "SELECT COUNT(*) FROM rides"));
            metrics.put("completedRides", scalarLong(st, "SELECT COUNT(*) FROM rides WHERE status='COMPLETED'"));
            metrics.put("cancelledRides", scalarLong(st, "SELECT COUNT(*) FROM rides WHERE status='CANCELLED'"));
            metrics.put("totalRevenue", scalarDecimal(st,
                    "SELECT COALESCE(SUM(COALESCE(final_fare, estimated_fare)), 0) FROM rides WHERE status='COMPLETED'"));
            metrics.put("openSosAlerts", scalarLong(st, "SELECT COUNT(*) FROM rides WHERE sos_triggered = 1 AND status <> 'COMPLETED'"));
        }
        return metrics;
    }

    public void createReport(long adminId, String title, String details) throws SQLException {
        String sql = "INSERT INTO admin_reports(admin_id, title, details, status) VALUES (?, ?, ?, 'OPEN')";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, adminId);
            ps.setString(2, title);
            ps.setString(3, details);
            ps.executeUpdate();
        }
    }

    /** Completes the CRUD set for this module: an admin can remove a report once it's no longer needed. */
    public void deleteReport(long reportId) throws SQLException {
        String sql = "DELETE FROM admin_reports WHERE id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, reportId);
            ps.executeUpdate();
        }
    }

    public void resolveReport(long reportId) throws SQLException {
        String sql = "UPDATE admin_reports SET status = 'RESOLVED' WHERE id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, reportId);
            ps.executeUpdate();
        }
    }

    public List<AdminReport> findAll() throws SQLException {
        List<AdminReport> reports = new ArrayList<>();
        String sql = "SELECT * FROM admin_reports ORDER BY id DESC";
        try (Connection c = db.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                reports.add(new AdminReport(rs.getLong("id"), rs.getLong("admin_id"), rs.getString("title"),
                        rs.getString("details"), rs.getString("status"), rs.getTimestamp("created_at")));
            }
        }
        return reports;
    }

    private long scalarLong(Statement st, String sql) throws SQLException {
        try (ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private BigDecimal scalarDecimal(Statement st, String sql) throws SQLException {
        try (ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }
}
