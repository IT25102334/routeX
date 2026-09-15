package com.routex.strategy;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Context class for the Strategy pattern (see {@link FareStrategy}).
 * Module: Ride Booking & Fare Estimation (Moses A.C.N. - IT25102334)
 *
 * Since the proposal's System Limitations note that this project uses
 * simulated GPS/location data rather than a live mapping API, distance
 * is derived deterministically from the pickup/drop-off text so the
 * same two addresses always produce the same fare estimate.
 */
public class FareCalculator {

    private final Map<String, FareStrategy> strategies = new HashMap<>();

    public FareCalculator() {
        register(new StandardFareStrategy());
        register(new PremiumFareStrategy());
        register(new PoolFareStrategy());
    }

    private void register(FareStrategy strategy) {
        strategies.put(strategy.getRideType(), strategy);
    }

    /** Simulated distance: a stable pseudo-random value in the 3-32 km range for a given route. */
    public double simulateDistanceKm(String pickup, String dropoff) {
        int seed = Math.abs((pickup + "|" + dropoff).hashCode());
        return 3 + (seed % 30);
    }

    /** Peak hours are simulated as 07:00-09:30 and 17:00-19:30. */
    public boolean isPeakHour() {
        LocalTime now = LocalTime.now();
        return (now.isAfter(LocalTime.of(7, 0)) && now.isBefore(LocalTime.of(9, 30)))
                || (now.isAfter(LocalTime.of(17, 0)) && now.isBefore(LocalTime.of(19, 30)));
    }

    public BigDecimal estimate(String pickup, String dropoff, String rideType) {
        FareStrategy strategy = strategies.getOrDefault(rideType, strategies.get("STANDARD"));
        double distanceKm = simulateDistanceKm(pickup, dropoff);
        return strategy.calculateFare(distanceKm, isPeakHour());
    }
}
