package com.routex.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * A single ride, from booking through to completion.
 * Shared by three modules:
 *  - Ride Booking & Fare Estimation (creates it, status REQUESTED)
 *  - Driver Matching & Dispatch (moves it to DISPATCHED / ACCEPTED)
 *  - Live Trip Management (ARRIVED / IN_PROGRESS / COMPLETED / CANCELLED)
 */
public class Ride {

    private long id;
    private long riderId;
    private Long driverId;          // null until a driver is matched
    private String pickup;
    private String dropoff;
    private String rideType;        // STANDARD | PREMIUM | POOL - pricing tier, drives the Strategy pattern
    private String vehicleType;     // CAR | VAN | TUK_TUK | BIKE - physical vehicle class, drives driver matching
    private BigDecimal estimatedFare;
    private BigDecimal finalFare;
    private String status;
    private Double currentLat;
    private Double currentLng;
    private boolean sosTriggered;
    private Timestamp requestedAt;
    private Timestamp startedAt;
    private Timestamp completedAt;

    public Ride() {
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getRiderId() { return riderId; }
    public void setRiderId(long riderId) { this.riderId = riderId; }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public String getPickup() { return pickup; }
    public void setPickup(String pickup) { this.pickup = pickup; }

    public String getDropoff() { return dropoff; }
    public void setDropoff(String dropoff) { this.dropoff = dropoff; }

    public String getRideType() { return rideType; }
    public void setRideType(String rideType) { this.rideType = rideType; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public BigDecimal getEstimatedFare() { return estimatedFare; }
    public void setEstimatedFare(BigDecimal estimatedFare) { this.estimatedFare = estimatedFare; }

    public BigDecimal getFinalFare() { return finalFare; }
    public void setFinalFare(BigDecimal finalFare) { this.finalFare = finalFare; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getCurrentLat() { return currentLat; }
    public void setCurrentLat(Double currentLat) { this.currentLat = currentLat; }

    public Double getCurrentLng() { return currentLng; }
    public void setCurrentLng(Double currentLng) { this.currentLng = currentLng; }

    public boolean isSosTriggered() { return sosTriggered; }
    public void setSosTriggered(boolean sosTriggered) { this.sosTriggered = sosTriggered; }

    public Timestamp getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Timestamp requestedAt) { this.requestedAt = requestedAt; }

    public Timestamp getStartedAt() { return startedAt; }
    public void setStartedAt(Timestamp startedAt) { this.startedAt = startedAt; }

    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }
}
