<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html>
<head>
    <title>RouteX - Trip #${ride.id}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <script src="${pageContext.request.contextPath}/assets/js/app.js"></script>
</head>
<body onload="startTripAutoRefresh(5)">
<div class="topbar">
    <span class="brand">RouteX</span>
    <div>
        <a href="${pageContext.request.contextPath}/rider/dashboard">My Rides</a>
        <a href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
</div>

<div class="container">
    <div class="card">
        <h2>Trip #${ride.id} <span class="badge ${ride.status}">${ride.status}</span></h2>
        <p><strong>Pickup:</strong> ${ride.pickup}</p>
        <p><strong>Drop-off:</strong> ${ride.dropoff}</p>
        <p><strong>Ride type:</strong> ${ride.rideType}</p>
        <p><strong>Estimated fare:</strong> <fmt:formatNumber value="${ride.estimatedFare}" type="currency" currencySymbol="Rs. "/></p>

        <c:if test="${ride.currentLat != null}">
            <p class="muted">Live location (simulated GPS): ${ride.currentLat}, ${ride.currentLng}</p>
        </c:if>

        <c:if test="${not empty sosAlerts}">
            <c:forEach var="alert" items="${sosAlerts}">
                <p class="error">🚨 SOS alert active for this trip: "${alert.message}" (triggered ${alert.triggeredAt}). Admin has been notified.</p>
                <form method="post" action="${pageContext.request.contextPath}/rider/sos">
                    <input type="hidden" name="rideId" value="${ride.id}">
                    <input type="hidden" name="action" value="RESOLVE">
                    <input type="hidden" name="alertId" value="${alert.id}">
                    <button type="submit" class="btn secondary">I'm safe now - resolve this SOS</button>
                </form>
            </c:forEach>
        </c:if>

        <c:if test="${not empty param.sosError}">
            <p class="error">${param.sosError}</p>
        </c:if>

        <c:if test="${ride.status == 'IN_PROGRESS' || ride.status == 'ARRIVED' || ride.status == 'ACCEPTED'}">
            <form method="post" action="${pageContext.request.contextPath}/rider/sos">
                <input type="hidden" name="rideId" value="${ride.id}">
                <label for="sosReason" class="muted">Describe the emergency:</label>
                <textarea id="sosReason" name="reason" rows="2"
                          placeholder="e.g. Driver is driving recklessly"
                          required minlength="5" maxlength="200"></textarea>
                <button type="submit" class="btn danger">🚨 Trigger SOS</button>
            </form>
        </c:if>

        <c:if test="${ride.status == 'COMPLETED'}">
            <a class="btn" href="${pageContext.request.contextPath}/rider/rate?rideId=${ride.id}">Rate your driver</a>
        </c:if>
    </div>
</div>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
</body>
</html>
