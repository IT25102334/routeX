<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html>
<head>
    <title>RouteX - Manage Driver Profiles</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<div class="topbar">
    <span class="brand">RouteX</span>
    <div>
        <a href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
</div>

<div class="container">
    <div class="card">
        <h2>Manage Driver Profiles</h2>
        <c:if test="${not empty error}">
            <p class="error">${error}</p>
        </c:if>

        <h3>Create Driver Profile</h3>
        <p class="muted">Enter the user ID of an existing account to give it a driver profile.</p>
        <form method="post" action="${pageContext.request.contextPath}/driver/profile/create">
            <input name="userId" type="number" placeholder="User ID" required>
            <input name="licenseNo" placeholder="License number" required>
            <select name="vehicleType" required>
                <option value="CAR">Car</option>
                <option value="VAN">Van</option>
                <option value="TUK_TUK">Tuk-tuk</option>
                <option value="BIKE">Motorbike</option>
            </select>
            <input name="vehicleInfo" placeholder="Vehicle info (e.g. plate, model)" required>
            <button type="submit">Create Profile</button>
        </form>
    </div>

    <div class="card">
        <h3>All Driver Profiles</h3>
        <table>
            <tr><th>User ID</th><th>Name</th><th>License</th><th>Vehicle</th><th>Status</th><th>Verified</th><th>Rating</th><th>Actions</th></tr>
            <c:forEach var="d" items="${drivers}">
                <tr>
                    <td>${d.id}</td>
                    <td>${d.name}</td>
                    <td>${d.licenseNo}</td>
                    <td>${d.vehicleType} (${d.vehicleInfo})</td>
                    <td>${d.availability}</td>
                    <td>${d.verified}</td>
                    <td>${d.rating}</td>
                    <td>
                        <form method="post" action="${pageContext.request.contextPath}/driver/profile/update" style="display:inline">
                            <input type="hidden" name="userId" value="${d.id}">
                            <input type="hidden" name="vehicleType" value="${d.vehicleType}">
                            <input name="licenseNo" value="${d.licenseNo}" style="width:80px">
                            <input name="vehicleInfo" value="${d.vehicleInfo}" style="width:100px">
                            <button type="submit" class="btn">Save</button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/driver/profile/delete" style="display:inline"
                              onsubmit="return confirm('Delete this driver profile?');">
                            <input type="hidden" name="userId" value="${d.id}">
                            <button type="submit" class="btn danger">Delete</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty drivers}">
                <tr><td colspan="8" class="muted">No driver profiles yet.</td></tr>
            </c:if>
        </table>
    </div>
</div>
</body>
</html>
