package com.routex.observer;

import com.routex.model.Ride;

import java.util.ArrayList;
import java.util.List;

/**
 * The "subject" half of the Observer pattern. LiveTripServlet holds one
 * of these, registers observers against it once at startup, and calls
 * {@link #notifyStatusChanged} / {@link #notifySos} whenever a trip
 * event happens instead of writing notification logic inline.
 */
public class RideSubject {

    private final List<RideObserver> observers = new ArrayList<>();

    public void addObserver(RideObserver observer) {
        observers.add(observer);
    }

    public void notifyStatusChanged(Ride ride, String newStatus) {
        for (RideObserver observer : observers) {
            observer.onRideStatusChanged(ride, newStatus);
        }
    }

    public void notifySos(Ride ride) {
        for (RideObserver observer : observers) {
            observer.onSosTriggered(ride);
        }
    }
}
