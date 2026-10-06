<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
    <title>RouteX - Edit Ride</title>
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
    <div class="card" style="max-width:480px;margin:40px auto;">
        <h2>Edit Ride #${ride.id}</h2>
        <p class="muted">You can only edit a ride while it's still REQUESTED (before a driver is dispatched). The fare will be recalculated for your new details.</p>

        <% if (request.getAttribute("error") != null) { %>
            <p class="error"><%= request.getAttribute("error") %></p>
        <% } %>

        <form method="post" action="${pageContext.request.contextPath}/rider/edit">
            <input type="hidden" name="rideId" value="${ride.id}">

            <label class="muted">Pickup</label>
            <input name="pickup" type="text" value="${ride.pickup}" required>

            <label class="muted">Drop-off</label>
            <input name="dropoff" type="text" value="${ride.dropoff}" required>

            <label class="muted">Ride tier</label>
            <select name="rideType" required>
                <option value="STANDARD" ${ride.rideType == 'STANDARD' ? 'selected' : ''}>Standard</option>
                <option value="PREMIUM" ${ride.rideType == 'PREMIUM' ? 'selected' : ''}>Premium</option>
                <option value="POOL" ${ride.rideType == 'POOL' ? 'selected' : ''}>Pool</option>
            </select>

            <label class="muted">Vehicle type</label>
            <select name="vehicleType" required>
                <option value="CAR" ${ride.vehicleType == 'CAR' ? 'selected' : ''}>Car</option>
                <option value="VAN" ${ride.vehicleType == 'VAN' ? 'selected' : ''}>Van</option>
                <option value="TUK_TUK" ${ride.vehicleType == 'TUK_TUK' ? 'selected' : ''}>Tuk-tuk</option>
                <option value="BIKE" ${ride.vehicleType == 'BIKE' ? 'selected' : ''}>Motorbike</option>
            </select>

            <button type="submit" class="btn" style="width:100%;margin-top:10px;">Save Changes</button>
            <a href="${pageContext.request.contextPath}/rider/dashboard" class="btn secondary" style="width:100%;display:block;text-align:center;margin-top:8px;">Cancel</a>
        </form>
    </div>
</div>
</body>
</html>
