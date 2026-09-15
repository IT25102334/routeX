package com.routex.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * A Driver account. Extends User with the extra columns that live in
 * the "drivers" table (Driver Matching & Dispatch module owns this data).
 */
public class Driver extends User {

    private String licenseNo;
    private String vehicleType;    // CAR | VAN | TUK_TUK | BIKE
    private String vehicleInfo;
    private String availability;   // ONLINE | OFFLINE
    private boolean verified;
    private BigDecimal rating;

    public Driver(long id, String name, String email, String phone, String status, Timestamp createdAt,
                  String licenseNo, String vehicleType, String vehicleInfo, String availability, boolean verified, BigDecimal rating) {
        super(id, name, email, phone, status, createdAt);
        this.licenseNo = licenseNo;
        this.vehicleType = vehicleType;
        this.vehicleInfo = vehicleInfo;
        this.availability = availability;
        this.verified = verified;
        this.rating = rating;
    }

    @Override
    public String getRole() {
        return "DRIVER";
    }

    public String getLicenseNo() { return licenseNo; }
    public String getVehicleType() { return vehicleType; }
    public String getVehicleInfo() { return vehicleInfo; }
    public String getAvailability() { return availability; }
    public boolean isOnline() { return "ONLINE".equals(availability); }
    public boolean isVerified() { return verified; }
    public BigDecimal getRating() { return rating; }
}
