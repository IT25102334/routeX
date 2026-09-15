package com.routex.model;

import java.sql.Timestamp;

/**
 * A Rider account. Riders book rides (Ride Booking & Fare Estimation),
 * track them (Live Trip Management), pay via wallet (Wallet & Rewards)
 * and rate drivers (Ratings & Reviews).
 */
public class Rider extends User {

    public Rider(long id, String name, String email, String phone, String status, Timestamp createdAt) {
        super(id, name, email, phone, status, createdAt);
    }

    @Override
    public String getRole() {
        return "RIDER";
    }
}
