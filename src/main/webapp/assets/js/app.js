// RouteX client-side helpers.
// Location data is simulated (per the project's stated System Limitations) -
// this just nudges a starting coordinate slightly every few seconds while
// a trip is IN_PROGRESS, and posts the new point back to LiveTripServlet.

function startSimulatedLocationUpdates(contextPath, rideId, startLat, startLng) {
    let lat = startLat;
    let lng = startLng;

    setInterval(function () {
        lat += (Math.random() - 0.5) * 0.002;
        lng += (Math.random() - 0.5) * 0.002;

        const body = new URLSearchParams();
        body.append("rideId", rideId);
        body.append("action", "LOCATION");
        body.append("lat", lat.toFixed(6));
        body.append("lng", lng.toFixed(6));

        fetch(contextPath + "/driver/trip", { method: "POST", body: body })
            .catch(function (err) { console.error("Location update failed", err); });
    }, 5000);
}

// Riders auto-refresh the trip page every few seconds to see driver progress.
function startTripAutoRefresh(seconds) {
    setTimeout(function () { window.location.reload(); }, seconds * 1000);
}
