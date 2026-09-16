<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>RouteX - Admin & Reporting</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 30px; background: #f5f6f8; }
        h1 { color: #222; }
        nav a { margin-right: 15px; text-decoration: none; color: #2a5bd7; font-weight: bold; }
        table { border-collapse: collapse; width: 100%; margin-top: 15px; background: #fff; }
        th, td { border: 1px solid #ddd; padding: 8px 12px; text-align: left; }
        th { background: #2a5bd7; color: #fff; }
        .summary-box { display: inline-block; background: #fff; padding: 15px 25px;
                       border-radius: 8px; margin-right: 15px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
        .summary-box .value { font-size: 24px; font-weight: bold; color: #2a5bd7; }
        form.inline { display: inline; }
        select, button { padding: 4px 8px; }
    </style>
</head>
<body>

<h1>Admin & Reporting Dashboard</h1>

<nav>
    <a href="${pageContext.request.contextPath}/admin/report?type=summary">Summary</a>
    <a href="${pageContext.request.contextPath}/admin/report?type=users">Users</a>
    <a href="${pageContext.request.contextPath}/admin/report?type=rides">Rides</a>
    <a href="${pageContext.request.contextPath}/admin/report?type=topDrivers">Top Drivers</a>
</nav>

<hr/>

<%-- ================= SUMMARY ================= --%>
<c:if test="${reportType == 'summary'}">
    <div class="summary-box">
        <div>Total Revenue</div>
        <div class="value">$<fmt:formatNumber value="${totalRevenue}" pattern="#,##0.00" xmlns:fmt="jakarta.tags.fmt"/></div>
    </div>
    <div class="summary-box">
        <div>Total Rides</div>
        <div class="value">${totalRides}</div>
    </div>

    <h2>Top Drivers</h2>
    <table>
        <tr><th>Driver ID</th><th>Trips</th><th>Earnings</th></tr>
        <c:forEach var="d" items="${topDrivers}">
            <tr>
                <td>${d.driverId}</td>
                <td>${d.trips}</td>
                <td>$${d.earnings}</td>
            </tr>
        </c:forEach>
    </table>
</c:if>

<%-- ================= USERS ================= --%>
<c:if test="${reportType == 'users'}">
    <h2>All Users</h2>
    <table>
        <tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Action</th></tr>
        <c:forEach var="u" items="${users}">
            <tr>
                <td>${u.id}</td>
                <td>${u.name}</td>
                <td>${u.email}</td>
                <td>${u.role}</td>
                <td>${u.status}</td>
                <td>
                    <form class="inline" method="post"
                          action="${pageContext.request.contextPath}/admin/report">
                        <input type="hidden" name="action" value="updateStatus"/>
                        <input type="hidden" name="userId" value="${u.id}"/>
                        <select name="status">
                            <option value="ACTIVE" ${u.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                            <option value="SUSPENDED" ${u.status == 'SUSPENDED' ? 'selected' : ''}>SUSPENDED</option>
                        </select>
                        <button type="submit">Update</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
    </table>
</c:if>

<%-- ================= RIDES ================= --%>
<c:if test="${reportType == 'rides'}">
    <h2>All Rides</h2>
    <table>
        <tr><th>ID</th><th>Rider ID</th><th>Driver ID</th><th>Status</th><th>Fare</th><th>Created At</th></tr>
        <c:forEach var="r" items="${rides}">
            <tr>
                <td>${r.id}</td>
                <td>${r.riderId}</td>
                <td>${r.driverId}</td>
                <td>${r.status}</td>
                <td>$${r.fare}</td>
                <td>${r.createdAt}</td>
            </tr>
        </c:forEach>
    </table>
</c:if>

<%-- ================= TOP DRIVERS (standalone view) ================= --%>
<c:if test="${reportType == 'topDrivers'}">
    <h2>Top Drivers</h2>
    <table>
        <tr><th>Driver ID</th><th>Trips</th><th>Earnings</th></tr>
        <c:forEach var="d" items="${topDrivers}">
            <tr>
                <td>${d.driverId}</td>
                <td>${d.trips}</td>
                <td>$${d.earnings}</td>
            </tr>
        </c:forEach>
    </table>
</c:if>

</body>
</html>
