package com.routex.validation;

import java.util.Set;

/**
 * Module: Driver Matching & Dispatch (Abeygunawardhana S.N.J. - IT25100107)
 *
 * Pure validation rules for driver profile data - no database access.
 * Used by DriverDao before any write, so bad data never reaches SQL.
 */
public final class DriverValidator {

    private static final Set<String> VALID_VEHICLE_TYPES = Set.of("CAR", "VAN", "TUK_TUK", "BIKE");
    private static final Set<String> VALID_AVAILABILITY = Set.of("ONLINE", "OFFLINE");

    private DriverValidator() {
    }

    public static void validateUserId(long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("A valid user ID is required.");
        }
    }

    public static void validateLicenseNo(String licenseNo) {
        if (licenseNo == null || licenseNo.trim().isEmpty()) {
            throw new IllegalArgumentException("License number cannot be empty.");
        }
        if (licenseNo.length() > 60) {
            throw new IllegalArgumentException("License number cannot exceed 60 characters.");
        }
    }

    public static void validateVehicleType(String vehicleType) {
        if (vehicleType == null || !VALID_VEHICLE_TYPES.contains(vehicleType)) {
            throw new IllegalArgumentException("Vehicle type must be one of: " + VALID_VEHICLE_TYPES);
        }
    }

    public static void validateVehicleInfo(String vehicleInfo) {
        if (vehicleInfo == null || vehicleInfo.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle info cannot be empty.");
        }
        if (vehicleInfo.length() > 150) {
            throw new IllegalArgumentException("Vehicle info cannot exceed 150 characters.");
        }
    }

    public static void validateAvailability(String availability) {
        if (availability == null || !VALID_AVAILABILITY.contains(availability)) {
            throw new IllegalArgumentException("Availability must be ONLINE or OFFLINE.");
        }
    }

    public static void validateDriverProfile(long userId, String licenseNo, String vehicleType, String vehicleInfo) {
        validateUserId(userId);
        validateLicenseNo(licenseNo);
        validateVehicleType(vehicleType);
        validateVehicleInfo(vehicleInfo);
    }
}
