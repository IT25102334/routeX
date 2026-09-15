package com.routex.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Pool ride: lowest base + per-km rate (shared ride discount), no surge applied. */
public class PoolFareStrategy implements FareStrategy {

    private static final BigDecimal BASE_FARE = new BigDecimal("60.00");
    private static final BigDecimal PER_KM_RATE = new BigDecimal("35.00");

    @Override
    public BigDecimal calculateFare(double distanceKm, boolean peakHour) {
        BigDecimal distance = BigDecimal.valueOf(distanceKm);
        BigDecimal fare = BASE_FARE.add(PER_KM_RATE.multiply(distance));
        return fare.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getRideType() {
        return "POOL";
    }
}
