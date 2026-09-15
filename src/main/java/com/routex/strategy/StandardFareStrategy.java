package com.routex.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Standard ride: base fare + per-km rate, with a 1.5x surge multiplier at peak hours. */
public class StandardFareStrategy implements FareStrategy {

    private static final BigDecimal BASE_FARE = new BigDecimal("100.00");
    private static final BigDecimal PER_KM_RATE = new BigDecimal("55.00");
    private static final BigDecimal SURGE_MULTIPLIER = new BigDecimal("1.5");

    @Override
    public BigDecimal calculateFare(double distanceKm, boolean peakHour) {
        BigDecimal distance = BigDecimal.valueOf(distanceKm);
        BigDecimal fare = BASE_FARE.add(PER_KM_RATE.multiply(distance));
        if (peakHour) {
            fare = fare.multiply(SURGE_MULTIPLIER);
        }
        return fare.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getRideType() {
        return "STANDARD";
    }
}
