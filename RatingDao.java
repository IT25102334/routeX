package com.routex.dao;

import com.routex.model.Rating;
import com.routex.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the Ratings, Reviews & Driver Performance module
 * (Gopiga A. - IT25102335).
 */
public class RatingDao {

    private static final int LOW_PERFORMANCE_THRESHOLD = 3; // out of 5
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    /**
     * Saves a rating and immediately recalculates the target user's
     * average score (UC-05, steps 5-7). Basic profanity screening keeps
     * reviews clean before they are stored (UC-05 extension 3a).
     */
    public void submit(long rideId, long fromUserId, long toUserId, int stars, String review) throws SQLException {
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Star rating must be between 1 and 5");
        }
        String cleanReview = sanitize(review);

        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try {
                String insert = "INSERT INTO ratings(ride_id, from_user_id, to_user_id, stars, review) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = c.prepareStatement(insert)) {
                    ps.setLong(1, rideId);
                    ps.setLong(2, fromUserId);
                    ps.setLong(3, toUserId);
                    ps.setInt(4, stars);
                    ps.setString(5, cleanReview);
                    ps.executeUpdate();
                }

                String recalc = "UPDATE drivers SET rating = (SELECT COALESCE(AVG(stars), 5) FROM ratings WHERE to_user_id = ?) WHERE user_id = ?";
                try (PreparedStatement ps = c.prepareStatement(recalc)) {
                    ps.setLong(1, toUserId);
                    ps.setLong(2, toUserId);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Simple word-filter so obviously offensive reviews never reach the database (extension 3a in UC-05). */
    private String sanitize(String review) {
        if (review == null) {
            return null;
        }
        String[] blocked = {"idiot", "stupid", "hate"};
        String cleaned = review;
        for (String word : blocked) {
            cleaned = cleaned.replaceAll("(?i)" + word, "****");
        }
        return cleaned;
    }

    /** Ratings a given user has submitted about others (as opposed to received). */
    public List<Rating> findSubmittedBy(long fromUserId) throws SQLException {
        List<Rating> ratings = new ArrayList<>();
        String sql = "SELECT * FROM ratings WHERE from_user_id = ? ORDER BY id DESC";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, fromUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ratings.add(map(rs));
                }
            }
        }
        return ratings;
    }

    /**
     * Completes the CRUD set for this module: lets the person who wrote a
     * review retract it (e.g. it was submitted by mistake), then
     * recalculates the target driver's average rating afterwards.
     * Only the original author can delete their own rating.
     */
    public boolean delete(long ratingId, long requestingUserId) throws SQLException {
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try {
                long toUserId;
                String find = "SELECT to_user_id FROM ratings WHERE id = ? AND from_user_id = ?";
                try (PreparedStatement ps = c.prepareStatement(find)) {
                    ps.setLong(1, ratingId);
                    ps.setLong(2, requestingUserId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            c.rollback();
                            return false; // not found, or doesn't belong to this user
                        }
                        toUserId = rs.getLong(1);
                    }
                }

                try (PreparedStatement ps = c.prepareStatement("DELETE FROM ratings WHERE id = ?")) {
                    ps.setLong(1, ratingId);
                    ps.executeUpdate();
                }

                String recalc = "UPDATE drivers SET rating = (SELECT COALESCE(AVG(stars), 5) FROM ratings WHERE to_user_id = ?) WHERE user_id = ?";
                try (PreparedStatement ps = c.prepareStatement(recalc)) {
                    ps.setLong(1, toUserId);
                    ps.setLong(2, toUserId);
                    ps.executeUpdate();
                }
                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public List<Rating> findForUser(long userId) throws SQLException {
        List<Rating> ratings = new ArrayList<>();
        String sql = "SELECT * FROM ratings WHERE to_user_id = ? ORDER BY id DESC";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ratings.add(map(rs));
                }
            }
        }
        return ratings;
    }

    public int countRidesRated(long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ratings WHERE to_user_id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Drivers whose average rating has fallen below the threshold (UC-05 extension 7a) - flagged for Admin review. */
    public boolean isLowPerformer(java.math.BigDecimal averageRating) {
        return averageRating != null && averageRating.compareTo(new java.math.BigDecimal(LOW_PERFORMANCE_THRESHOLD)) < 0;
    }

    private Rating map(ResultSet rs) throws SQLException {
        return new Rating(rs.getLong("id"), rs.getLong("ride_id"), rs.getLong("from_user_id"),
                rs.getLong("to_user_id"), rs.getInt("stars"), rs.getString("review"), rs.getTimestamp("created_at"));
    }
}
