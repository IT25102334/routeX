package com.routex.service;

import com.routex.dao.DriverDao;
import com.routex.dao.RideDao;
import com.routex.model.Ride;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Module: Driver Matching & Dispatch (Abeygunawardhana S.N.J. - IT25100107)
 *
 * Implements UC-02 (Match and Dispatch Driver): as soon as a ride is
 * REQUESTED, find the nearest (simulated by highest-rated) available
 * verified online driver whose vehicle type matches, and dispatch the
 * request to them. The driver then explicitly accepts or rejects it
 * through DriverDispatchServlet.
 */
public class DispatchService {

    private final DriverDao driverDao = new DriverDao();
    private final RideDao rideDao = new RideDao();

    /**
     * @return true if a driver was found and the ride was dispatched to them,
     *         false if no driver was available (UC-02 extension 2a).
     */
    public boolean dispatchRide(long rideId) throws SQLException {
        Ride ride = rideDao.findById(rideId).orElse(null);
        if (ride == null) {
            return false;
        }
        Optional<Long> driverId = driverDao.findBestAvailableDriver(ride.getVehicleType());
        if (driverId.isEmpty()) {
            return false;
        }
        rideDao.dispatchToDriver(rideId, driverId.get());
        return true;
    }

    /**
     * Called whenever a driver comes ONLINE: since dispatch normally only
     * fires once, at the moment a ride is booked, a ride that found no
     * driver back then would otherwise sit at REQUESTED forever. This
     * gives it a second chance the moment a new driver becomes available.
     * Only the single oldest waiting ride is retried per "go online"
     * event, so one driver can't be dispatched several rides at once.
     */
    public void retryOldestPendingRide() throws SQLException {
        List<Ride> pending = rideDao.findRequested();
        if (!pending.isEmpty()) {
            dispatchRide(pending.get(0).getId());
        }
    }
}
