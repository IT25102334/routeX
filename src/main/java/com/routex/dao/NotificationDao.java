package com.routex.dao;

import com.routex.model.Notification;
import com.routex.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data access backing {@link com.routex.observer.NotificationObserver}. */
public class NotificationDao {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public void create(long userId, String message, String type) throws SQLException {
        String sql = "INSERT INTO notifications(user_id, message, type) VALUES (?, ?, ?)";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, message);
            ps.setString(3, type);
            ps.executeUpdate();
        }
    }

    public List<Notification> findForUser(long userId) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY id DESC LIMIT 20";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(new Notification(rs.getLong("id"), rs.getLong("user_id"),
                            rs.getString("message"), rs.getString("type"), rs.getBoolean("is_read"), rs.getTimestamp("created_at")));
                }
            }
        }
        return notifications;
    }

    public List<Long> findAllAdminIds() throws SQLException {
        List<Long> ids = new ArrayList<>();
        try (Connection c = db.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT id FROM users WHERE role = 'ADMIN'")) {
            while (rs.next()) {
                ids.add(rs.getLong(1));
            }
        }
        return ids;
    }
}
