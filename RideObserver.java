package com.routex.observer;

import com.routex.model.Ride;

/**
 * DESIGN PATTERN: Observer.
 *
 * Module: Live Trip Management (Halloluwa T.T. - IT25100099)
 *
 * Anything that needs to react when a ride's status changes (rider
 * arrival notice, SOS alert, trip completion) implements this
 * interface and registers with a {@link RideSubject}. This means the
 * code that updates a ride's status never needs to know who, or how
 * many parties, care about that change.
 */
public interface RideObserver {

    void onRideStatusChanged(Ride ride, String newStatus);

    void onSosTriggered(Ride ride);
}
