package com.routex.model;

import java.sql.Timestamp;

/** A single star rating + optional review, owned by the Ratings & Reviews module. */
public class Rating {

    private long id;
    private long rideId;
    private long fromUserId;
    private long toUserId;
    private int stars;
    private String review;
    private Timestamp createdAt;

    public Rating(long id, long rideId, long fromUserId, long toUserId, int stars, String review, Timestamp createdAt) {
        this.id = id;
        this.rideId = rideId;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.stars = stars;
        this.review = review;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public long getRideId() { return rideId; }
    public long getFromUserId() { return fromUserId; }
    public long getToUserId() { return toUserId; }
    public int getStars() { return stars; }
    public String getReview() { return review; }
    public Timestamp getCreatedAt() { return createdAt; }
}
