<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!doctype html>
<html>
<head>
    <title>RouteX - Driver Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<div class="topbar">
    <span class="brand">RouteX</span>
    <div>
        <a href="${pageContext.request.contextPath}/driver/dashboard">Dashboard</a>
        <a href="${pageContext.request.contextPath}/rider/wallet">Wallet</a>
        <a href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
</div>

<div class="container">
    <div class="card">
        <h2>Welcome, ${sessionScope.user.name}</h2>
        <p>
            Status: <span class="badge ${driver.availability}">${driver.availability}</span>
            &nbsp;|&nbsp; Vehicle: <strong>${driver.vehicleType}</strong> (${driver.vehicleInfo})
            &nbsp;|&nbsp; Verified:
            <c:choose>
                <c:when test="${driver.verified}"><span class="badge ACTIVE">YES</span></c:when>
                <c:otherwise><span class="badge SUSPENDED">Pending Admin verification</span></c:otherwise>
            </c:choose>
            &nbsp;|&nbsp; Rating: <span class="stars">★</span> ${driver.rating}
        </p>

        <form method="post" action="${pageContext.request.contextPath}/driver/availability">
            <c:choose>
                <c:when test="${driver.availability == 'ONLINE'}">
                    <input type="hidden" name="availability" value="OFFLINE">
                    <button type="submit" class="btn danger">Go Offline</button>
                </c:when>
                <c:otherwise>
                    <input type="hidden" name="availability" value="ONLINE">
                    <button type="submit" class="btn success" ${driver.verified ? '' : 'disabled'}>Go Online</button>
                </c:otherwise>
            </c:choose>
        </form>
    </div>

    <div class="card">
        <h3>Incoming &amp; Active Requests</h3>
        <c:set var="hasActive" value="false"/>
        <c:forEach var="ride" items="${rides}">
            <c:if test="${ride.status == 'DISPATCHED' || ride.status == 'ACCEPTED' || ride.status == 'ARRIVED' || ride.status == 'IN_PROGRESS'}">
                <c:set var="hasActive" value="true"/>
                <div class="card" style="background:#f8faff;">
                    <p><strong>Ride #${ride.id}</strong> - ${ride.pickup} &rarr; ${ride.dropoff}
                        (<fmt:formatNumber value="${ride.estimatedFare}" type="currency" currencySymbol="Rs. "/>,
                        ${ride.vehicleType})
                        <span class="badge ${ride.status}">${ride.status}</span>
                    </p>

                    <c:if test="${ride.status == 'DISPATCHED'}">
                        <form method="post" action="${pageContext.request.contextPath}/driver/respond" style="display:inline">
                            <input type="hidden" name="rideId" value="${ride.id}">
                            <input type="hidden" name="decision" value="ACCEPT">
                            <button type="submit" class="btn success">Accept</button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/driver/respond" style="display:inline">
                            <input type="hidden" name="rideId" value="${ride.id}">
                            <input type="hidden" name="decision" value="REJECT">
                            <button type="submit" class="btn danger">Reject</button>
                        </form>
                    </c:if>
                    <c:if test="${ride.status != 'DISPATCHED'}">
                        <a class="btn" href="${pageContext.request.contextPath}/driver/trip?id=${ride.id}">Manage Trip</a>
                    </c:if>
                </div>
            </c:if>
        </c:forEach>
        <c:if test="${!hasActive}">
            <p class="muted">No active requests right now. Go online to start receiving ride requests.</p>
        </c:if>
    </div>

    <div class="card">
        <h3>Ride History</h3>
        <table>
            <tr><th>ID</th><th>Pickup</th><th>Drop-off</th><th>Fare</th><th>Status</th></tr>
            <c:forEach var="ride" items="${rides}">
                <tr>
                    <td>#${ride.id}</td>
                    <td>${ride.pickup}</td>
                    <td>${ride.dropoff}</td>
                    <td><fmt:formatNumber value="${ride.estimatedFare}" type="currency" currencySymbol="Rs. "/></td>
                    <td><span class="badge ${ride.status}">${ride.status}</span></td>
                </tr>
            </c:forEach>
            <c:if test="${empty rides}">
                <tr><td colspan="5" class="muted">No rides yet.</td></tr>
            </c:if>
        </table>
    </div>
</div>
</body>
</html>
