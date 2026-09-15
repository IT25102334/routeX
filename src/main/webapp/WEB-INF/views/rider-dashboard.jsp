<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!doctype html>
<html>
<head>
    <title>RouteX - My Rides</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<div class="topbar">
    <span class="brand">RouteX</span>
    <div>
        <a href="${pageContext.request.contextPath}/rider/dashboard">My Rides</a>
        <a href="${pageContext.request.contextPath}/rider/book">Book a Ride</a>
        <a href="${pageContext.request.contextPath}/rider/wallet">Wallet</a>
        <a href="${pageContext.request.contextPath}/rider/reviews">My Reviews</a>
        <a href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
</div>

<div class="container">
    <div class="card">
        <h2>Welcome, ${sessionScope.user.name}</h2>
        <p class="muted">Book a ride, track it live, and rate your driver once it's done.</p>
        <a class="btn" href="${pageContext.request.contextPath}/rider/book">+ Book a New Ride</a>
    </div>

    <div class="card">
        <h3>Ride History</h3>
        <table>
            <tr>
                <th>ID</th><th>Pickup</th><th>Drop-off</th><th>Ride Tier</th><th>Vehicle</th>
                <th>Fare</th><th>Status</th><th>Action</th>
            </tr>
            <c:forEach var="ride" items="${rides}">
                <tr>
                    <td>#${ride.id}</td>
                    <td>${ride.pickup}</td>
                    <td>${ride.dropoff}</td>
                    <td>${ride.rideType}</td>
                    <td>${ride.vehicleType}</td>
                    <td><fmt:formatNumber value="${ride.estimatedFare}" type="currency" currencySymbol="Rs. "/></td>
                    <td><span class="badge ${ride.status}">${ride.status}</span></td>
                    <td>
                        <a href="${pageContext.request.contextPath}/rider/trip?id=${ride.id}">View</a>
                        <c:if test="${ride.status == 'COMPLETED'}">
                            &nbsp;|&nbsp;<a href="${pageContext.request.contextPath}/rider/rate?rideId=${ride.id}">Rate</a>
                        </c:if>
                        <c:if test="${ride.status == 'REQUESTED' || ride.status == 'DISPATCHED'}">
                            &nbsp;|&nbsp;
                            <form style="display:inline" method="post" action="${pageContext.request.contextPath}/rider/cancel">
                                <input type="hidden" name="rideId" value="${ride.id}">
                                <button type="submit" class="btn danger" style="padding:2px 8px;font-size:12px;">Cancel</button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty rides}">
                <tr><td colspan="8" class="muted">No rides yet - book your first one above.</td></tr>
            </c:if>
        </table>
    </div>
</div>
</body>
</html>
