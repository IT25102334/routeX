package com.routex.factory;

import com.routex.model.Admin;
import com.routex.model.Driver;
import com.routex.model.Rider;
import com.routex.model.User;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * DESIGN PATTERN: Factory Method.
 *
 * Nothing outside this class needs an "if role == DRIVER" chain.
 * UserDao reads a row from the users table (joined with drivers when
 * needed) and hands it to this factory, which decides whether to
 * build a Rider, Driver or Admin object. Every controller then just
 * works with the abstract User type (or safely casts once it already
 * knows the role from the session), so adding a new role in future
 * only means changing this one class.
 */
public final class UserFactory {

    private UserFactory() {
    }

    /** Builds a Rider or Admin (no extra table involved) from a "users" row. */
    public static User fromUsersRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String status = rs.getString("status");
        Timestamp createdAt = rs.getTimestamp("created_at");
        String role = rs.getString("role");

        User user = switch (role) {
            case "DRIVER" -> new Driver(id, name, email, phone, status, createdAt,
                    null, "CAR", null, "OFFLINE", false, new BigDecimal("5.00"));
            case "ADMIN" -> new Admin(id, name, email, phone, status, createdAt);
            default -> new Rider(id, name, email, phone, status, createdAt);
        };
        user.setPasswordHash(rs.getString("password_hash"));
        return user;
    }

    /** Builds a fully-populated Driver from a joined "users JOIN drivers" row. */
    public static Driver fromJoinedDriverRow(ResultSet rs) throws SQLException {
        Driver driver = new Driver(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("status"),
                rs.getTimestamp("created_at"),
                rs.getString("license_no"),
                rs.getString("vehicle_type"),
                rs.getString("vehicle_info"),
                rs.getString("availability"),
                rs.getBoolean("verified"),
                rs.getBigDecimal("rating"));
        driver.setPasswordHash(rs.getString("password_hash"));
        return driver;
    }
}
