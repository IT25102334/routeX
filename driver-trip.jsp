<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!doctype html>
<html>
<head>
    <title>RouteX - Manage Trip #${ride.id}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <script src="${pageContext.request.contextPath}/assets/js/app.js"></script>
</head>
<body>
<div class="topbar">
    <span class="brand">RouteX</span>
    <div>
        <a href="${pageContext.request.contextPath}/driver/dashboard">Dashboard</a>
        <a href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
</div>

<div class="container">
    <div class="card">
        <h2>Trip #${ride.id} <span class="badge ${ride.status}">${ride.status}</span></h2>
        <p><strong>Pickup:</strong> ${ride.pickup}</p>
        <p><strong>Drop-off:</strong> ${ride.dropoff}</p>
        <p><strong>Fare:</strong> <fmt:formatNumber value="${ride.estimatedFare}" type="currency" currencySymbol="Rs. "/></p>

        <c:if test="${ride.sosTriggered}">
            <p class="error">🚨 Rider has triggered an SOS alert on this trip.</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/driver/trip">
            <input type="hidden" name="rideId" value="${ride.id}">

            <c:if test="${ride.status == 'ACCEPTED'}">
                <input type="hidden" name="action" value="ARRIVED">
                <button type="submit" class="btn">Mark Arrived at Pickup</button>
            </c:if>
            <c:if test="${ride.status == 'ARRIVED'}">
                <input type="hidden" name="action" value="START">
                <button type="submit" class="btn success">Start Trip (rider picked up)</button>
            </c:if>
            <c:if test="${ride.status == 'IN_PROGRESS'}">
                <input type="hidden" name="action" value="COMPLETE">
                <button type="submit" class="btn success">Complete Trip at Drop-off</button>
            </c:if>
        </form>

        <c:if test="${ride.status == 'COMPLETED'}">
            <p class="success-msg">Trip completed. Payment and reward points have been credited to the rider's wallet.</p>
        </c:if>
    </div>
</div>

<c:if test="${ride.status == 'IN_PROGRESS'}">
    <script>
        // Simulated GPS: starts from a fixed point near the pickup and drifts every 5 seconds
        // while the trip is in progress (see System Limitations: no live mapping API is used).
        startSimulatedLocationUpdates("${pageContext.request.contextPath}", ${ride.id}, 6.9271, 79.8612);
    </script>
</c:if>
</body>
</html>
