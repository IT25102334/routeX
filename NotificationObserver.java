package com.routex.observer;

import com.routex.dao.NotificationDao;
import com.routex.model.Ride;

import java.sql.SQLException;

/**
 * Concrete Observer: writes a row into the "notifications" table so the
 * rider (and, for SOS, the admin) can see it on their dashboard. This is
 * the "Send notifications for ride status changes... and cancellations"
 * requirement from the Lab02 functional requirements, and covers the
 * "Rider triggers an SOS alert" extension in UC-03 (Live Trip Management).
 */
public class NotificationObserver implements RideObserver {

    private final NotificationDao notificationDao = new NotificationDao();

    @Override
    public void onRideStatusChanged(Ride ride, String newStatus) {
        String message = "Your ride #" + ride.getId() + " status is now " + newStatus + ".";
        persist(ride.getRiderId(), message, "RIDE_STATUS");
    }

    @Override
    public void onSosTriggered(Ride ride) {
        String riderMessage = "SOS alert sent for ride #" + ride.getId() + ". Help is on the way.";
        persist(ride.getRiderId(), riderMessage, "SOS");

        // Also alert every admin so an operator can respond immediately.
        notifyAllAdmins(ride);
    }

    private void notifyAllAdmins(Ride ride) {
        try {
            for (long adminId : notificationDao.findAllAdminIds()) {
                persist(adminId, "SOS triggered on ride #" + ride.getId() + " - please review.", "SOS");
            }
        } catch (SQLException e) {
            System.err.println("Failed to notify admins of SOS: " + e.getMessage());
        }
    }

    private void persist(long userId, String message, String type) {
        try {
            notificationDao.create(userId, message, type);
        } catch (SQLException e) {
            // A failed notification should never break the underlying ride update.
            System.err.println("Failed to save notification: " + e.getMessage());
        }
    }
}
