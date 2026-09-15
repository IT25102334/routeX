package com.routex.dao;

import com.routex.model.Wallet;
import com.routex.model.WalletTransaction;
import com.routex.util.DatabaseConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the Wallet & Rewards Management module
 * (Adikarie J.A.D.C. - IT25102349).
 *
 * Every balance-changing operation writes both the wallet row and a
 * wallet_transactions row inside one JDBC transaction, so the balance
 * and the audit trail can never drift apart.
 */
public class WalletDao {

    private static final int MIN_POINTS_TO_REDEEM = 100;
    private static final BigDecimal POINTS_TO_CURRENCY_DIVISOR = new BigDecimal("10");
    public static final int POINTS_EARNED_PER_COMPLETED_RIDE = 50;

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public Wallet find(long userId) throws SQLException {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        String sql = "SELECT balance, points FROM wallets WHERE user_id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    wallet.setBalance(rs.getBigDecimal("balance"));
                    wallet.setPoints(rs.getInt("points"));
                }
            }
        }
        return wallet;
    }

    public void topUp(long userId, BigDecimal amount) throws SQLException {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Top-up amount must be positive");
        }
        applyBalanceChange(userId, amount, "TOPUP", "Wallet top-up");
    }

    /** Loyalty points credited automatically when a ride completes (Live Trip Management calls this). */
    public void addRewardPoints(long userId, int points) throws SQLException {
        String sql = "UPDATE wallets SET points = points + ? WHERE user_id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, points);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    /** Converts loyalty points into a wallet credit if the rider has enough points. */
    public boolean redeemPoints(long userId, int points) throws SQLException {
        if (points < MIN_POINTS_TO_REDEEM) {
            return false;
        }
        BigDecimal credit = new BigDecimal(points).divide(POINTS_TO_CURRENCY_DIVISOR, 2, RoundingMode.DOWN);

        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try {
                String sql = "UPDATE wallets SET points = points - ?, balance = balance + ? " +
                        "WHERE user_id = ? AND points >= ?";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setInt(1, points);
                    ps.setBigDecimal(2, credit);
                    ps.setLong(3, userId);
                    ps.setInt(4, points);
                    if (ps.executeUpdate() == 0) {
                        c.rollback();
                        return false;
                    }
                }
                insertTransaction(c, userId, "REDEEM", credit, points + " points redeemed");
                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Pays for a completed ride out of the rider's wallet balance. */
    public void payForRide(long riderId, BigDecimal fare, long rideId) throws SQLException {
        applyBalanceChange(riderId, fare.negate(), "RIDE_PAYMENT", "Payment for ride #" + rideId);
    }

    public List<WalletTransaction> history(long userId) throws SQLException {
        List<WalletTransaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM wallet_transactions WHERE user_id = ? ORDER BY id DESC";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new WalletTransaction(
                            rs.getLong("id"), rs.getLong("user_id"), rs.getString("type"),
                            rs.getBigDecimal("amount"), rs.getString("note"), rs.getTimestamp("created_at")));
                }
            }
        }
        return transactions;
    }

    private void applyBalanceChange(long userId, BigDecimal delta, String type, String note) throws SQLException {
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement("UPDATE wallets SET balance = balance + ? WHERE user_id = ?")) {
                    ps.setBigDecimal(1, delta);
                    ps.setLong(2, userId);
                    ps.executeUpdate();
                }
                insertTransaction(c, userId, type, delta, note);
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private void insertTransaction(Connection c, long userId, String type, BigDecimal amount, String note) throws SQLException {
        String sql = "INSERT INTO wallet_transactions(user_id, type, amount, note) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, type);
            ps.setBigDecimal(3, amount);
            ps.setString(4, note);
            ps.executeUpdate();
        }
    }
}
