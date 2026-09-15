<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!doctype html>
<html>
<head>
    <title>RouteX - Admin Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<div class="topbar">
    <span class="brand">RouteX Admin</span>
    <div>
        <a href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
        <a href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
</div>

<div class="container">
    <div class="metrics-grid">
        <div class="metric-box"><div class="value">${metrics.totalUsers}</div><div class="label">Total Users</div></div>
        <div class="metric-box"><div class="value">${metrics.totalRiders}</div><div class="label">Riders</div></div>
        <div class="metric-box"><div class="value">${metrics.totalDrivers}</div><div class="label">Drivers</div></div>
        <div class="metric-box"><div class="value">${metrics.onlineDrivers}</div><div class="label">Online Drivers</div></div>
        <div class="metric-box"><div class="value">${metrics.totalRides}</div><div class="label">Total Rides</div></div>
        <div class="metric-box"><div class="value">${metrics.completedRides}</div><div class="label">Completed</div></div>
        <div class="metric-box"><div class="value">${metrics.cancelledRides}</div><div class="label">Cancelled</div></div>
        <div class="metric-box"><div class="value">${metrics.openSosAlerts}</div><div class="label">Open SOS Alerts</div></div>
        <div class="metric-box">
            <div class="value"><fmt:formatNumber value="${metrics.totalRevenue}" type="currency" currencySymbol="Rs. "/></div>
            <div class="label">Total Revenue</div>
        </div>
    </div>

    <div class="card">
        <h3>User Accounts</h3>
        <table>
            <tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Action</th></tr>
            <c:forEach var="u" items="${users}">
                <tr>
                    <td>#${u.id}</td>
                    <td>${u.name}</td>
                    <td>${u.email}</td>
                    <td>${u.role}</td>
                    <td><span class="badge ${u.status}">${u.status}</span></td>
                    <td>
                        <form method="post" action="${pageContext.request.contextPath}/admin/user-status" style="display:inline">
                            <input type="hidden" name="userId" value="${u.id}">
                            <c:choose>
                                <c:when test="${u.status == 'ACTIVE'}">
                                    <input type="hidden" name="status" value="SUSPENDED">
                                    <button type="submit" class="btn danger" style="padding:2px 8px;font-size:12px;">Suspend</button>
                                </c:when>
                                <c:otherwise>
                                    <input type="hidden" name="status" value="ACTIVE">
                                    <button type="submit" class="btn success" style="padding:2px 8px;font-size:12px;">Reactivate</button>
                                </c:otherwise>
                            </c:choose>
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </div>

    <div class="card">
        <h3>Driver Verification &amp; Performance</h3>
        <table>
            <tr><th>ID</th><th>Name</th><th>License</th><th>Vehicle</th><th>Availability</th><th>Rating</th><th>Verified</th><th>Action</th></tr>
            <c:forEach var="d" items="${drivers}">
                <tr>
                    <td>#${d.id}</td>
                    <td>${d.name}</td>
                    <td>${d.licenseNo}</td>
                    <td>${d.vehicleType} <span class="muted">(${d.vehicleInfo})</span></td>
                    <td><span class="badge ${d.availability}">${d.availability}</span></td>
                    <td><span class="stars">★</span> ${d.rating}</td>
                    <td>
                        <c:choose>
                            <c:when test="${d.verified}"><span class="badge ACTIVE">Verified</span></c:when>
                            <c:otherwise><span class="badge SUSPENDED">Pending</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <c:if test="${!d.verified}">
                            <form method="post" action="${pageContext.request.contextPath}/admin/verify-driver">
                                <input type="hidden" name="userId" value="${d.id}">
                                <button type="submit" class="btn success" style="padding:2px 8px;font-size:12px;">Verify</button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </div>

    <div class="card">
        <h3>All Rides</h3>
        <table>
            <tr><th>ID</th><th>Rider</th><th>Driver</th><th>Vehicle</th><th>Fare</th><th>Status</th><th>Action</th></tr>
            <c:forEach var="r" items="${rides}">
                <tr>
                    <td>#${r.id}</td>
                    <td>#${r.riderId}</td>
                    <td>${r.driverId != null ? r.driverId : '-'}</td>
                    <td>${r.vehicleType}</td>
                    <td><fmt:formatNumber value="${r.estimatedFare}" type="currency" currencySymbol="Rs. "/></td>
                    <td><span class="badge ${r.status}">${r.status}</span> <c:if test="${r.sosTriggered}"><span class="badge SUSPENDED">SOS</span></c:if></td>
                    <td>
                        <c:if test="${r.status == 'REQUESTED'}">
                            <form method="post" action="${pageContext.request.contextPath}/admin/dispatch" style="display:inline">
                                <input type="hidden" name="rideId" value="${r.id}">
                                <button type="submit" class="btn" style="padding:2px 8px;font-size:12px;">Dispatch Now</button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </div>

    <div class="card">
        <h3>Operational / Dispute Reports</h3>
        <form method="post" action="${pageContext.request.contextPath}/admin/report" style="max-width:480px;">
            <input name="title" type="text" placeholder="Report title" required>
            <textarea name="details" rows="3" placeholder="Details" required></textarea>
            <button type="submit">Log Report</button>
        </form>
        <table>
            <tr><th>ID</th><th>Title</th><th>Status</th><th>Created</th><th>Action</th></tr>
            <c:forEach var="rep" items="${reports}">
                <tr>
                    <td>#${rep.id}</td>
                    <td>${rep.title}</td>
                    <td><span class="badge ${rep.status}">${rep.status}</span></td>
                    <td>${rep.createdAt}</td>
                    <td>
                        <c:if test="${rep.status == 'OPEN'}">
                            <form method="post" action="${pageContext.request.contextPath}/admin/report-resolve" style="display:inline">
                                <input type="hidden" name="reportId" value="${rep.id}">
                                <button type="submit" class="btn success" style="padding:2px 8px;font-size:12px;">Resolve</button>
                            </form>
                        </c:if>
                        <form method="post" action="${pageContext.request.contextPath}/admin/report-delete" style="display:inline"
                              onsubmit="return confirm('Delete this report permanently?');">
                            <input type="hidden" name="reportId" value="${rep.id}">
                            <button type="submit" class="btn danger" style="padding:2px 8px;font-size:12px;">Delete</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty reports}">
                <tr><td colspan="5" class="muted">No reports logged yet.</td></tr>
            </c:if>
        </table>
    </div>
</div>
</body>
</html>
