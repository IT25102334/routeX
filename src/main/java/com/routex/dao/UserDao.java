package com.routex.dao;

import com.routex.factory.UserFactory;
import com.routex.model.User;
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
 * Data access for the "users" table. Shared authentication infrastructure
 * used by every module, and directly relied on by Administration &
 * Reporting (Nimnadi R.D.S.) for account management.
 */
public class UserDao {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(UserFactory.fromUsersRow(rs)) : Optional.empty();
            }
        }
    }

    public Optional<User> findById(long id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(UserFactory.fromUsersRow(rs)) : Optional.empty();
            }
        }
    }

    /**
     * Registers a new account and, in the same transaction, creates the
     * supporting rows every user needs: a wallet, plus a drivers row
     * (with license/vehicle details) if they registered as a driver.
     */
    public long register(String name, String email, String phone, String passwordHash, String role,
                          String licenseNo, String vehicleType, String vehicleInfo) throws SQLException {
        String insertUser = "INSERT INTO users(name, email, phone, password_hash, role, status) VALUES (?, ?, ?, ?, ?, 'ACTIVE')";
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try {
                long newId;
                try (PreparedStatement ps = c.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.setString(2, email);
                    ps.setString(3, phone);
                    ps.setString(4, passwordHash);
                    ps.setString(5, role);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        newId = keys.getLong(1);
                    }
                }

                try (PreparedStatement wallet = c.prepareStatement(
                        "INSERT INTO wallets(user_id, balance, points) VALUES (?, 0, 0)")) {
                    wallet.setLong(1, newId);
                    wallet.executeUpdate();
                }

                if ("DRIVER".equals(role)) {
                    try (PreparedStatement driver = c.prepareStatement(
                            "INSERT INTO drivers(user_id, license_no, vehicle_type, vehicle_info, availability, verified, rating) " +
                            "VALUES (?, ?, ?, ?, 'OFFLINE', 0, 5.00)")) {
                        driver.setLong(1, newId);
                        driver.setString(2, licenseNo);
                        driver.setString(3, vehicleType != null ? vehicleType : "CAR");
                        driver.setString(4, vehicleInfo);
                        driver.executeUpdate();
                    }
                }

                c.commit();
                return newId;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id DESC";
        try (Connection c = db.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                users.add(UserFactory.fromUsersRow(rs));
            }
        }
        return users;
    }

    /** Suspend or re-activate an account (Administration & Reporting module). */
    public void setStatus(long userId, String status) throws SQLException {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }
}
