<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
    <title>RouteX - Book a Ride</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<div class="topbar">
    <span class="brand">RouteX</span>
    <div>
        <a href="${pageContext.request.contextPath}/rider/dashboard">My Rides</a>
        <a href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
</div>

<div class="container">
    <div class="card" style="max-width:480px;margin:0 auto;">
        <h2>Book a Ride</h2>
        <p class="muted">Enter your trip details. Fare is estimated instantly - no surprises at drop-off.</p>

        <% if (request.getAttribute("error") != null) { %>
            <p class="error"><%= request.getAttribute("error") %></p>
        <% } %>

        <form method="post" action="${pageContext.request.contextPath}/rider/book">
            <input name="pickup" type="text" placeholder="Pickup location" required>
            <input name="dropoff" type="text" placeholder="Drop-off location" required>

            <label class="muted">Ride tier (affects fare)</label>
            <select name="rideType" required>
                <option value="STANDARD">Standard</option>
                <option value="PREMIUM">Premium</option>
                <option value="POOL">Pool (shared, lowest cost)</option>
            </select>

            <label class="muted">Vehicle type</label>
            <select name="vehicleType" required>
                <option value="CAR">Car</option>
                <option value="VAN">Van</option>
                <option value="TUK_TUK">Tuk-tuk</option>
                <option value="BIKE">Motorbike</option>
            </select>

            <button type="submit">Confirm Booking</button>
        </form>
    </div>
</div>
</body>
</html>
