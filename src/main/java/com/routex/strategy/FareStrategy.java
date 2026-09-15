package com.routex.strategy;

import java.math.BigDecimal;

/**
 * DESIGN PATTERN: Strategy.
 *
 * Module: Ride Booking & Fare Estimation (Moses A.C.N. - IT25102334)
 *
 * Each ride type (Standard, Premium, Pool) prices a trip differently.
 * Rather than one method full of if/else on ride type, each pricing
 * algorithm is its own class implementing this interface, and
 * {@link FareCalculator} just calls whichever one matches the rider's
 * chosen ride type. Adding a new ride type later (e.g. "XL") only
 * means writing one new class - nothing else changes.
 */
public interface FareStrategy {

    /**
     * @param distanceKm   simulated trip distance in kilometres
     * @param peakHour     true if the booking falls in a surge window
     * @return the estimated fare for this ride type
     */
    BigDecimal calculateFare(double distanceKm, boolean peakHour);

    /** The ENUM value stored in rides.ride_type that this strategy is responsible for. */
    String getRideType();
}
