<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html>
<head>
    <title>RouteX - My Reviews</title>
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
    <div class="card">
        <h2>My Reviews</h2>
        <p class="muted">Reviews you've submitted about drivers. You can delete one if it was submitted by mistake.</p>

        <table>
            <tr><th>Ride</th><th>Stars</th><th>Review</th><th>Date</th><th>Action</th></tr>
            <c:forEach var="rev" items="${reviews}">
                <tr>
                    <td>#${rev.rideId}</td>
                    <td><span class="stars">★</span> ${rev.stars}</td>
                    <td>${rev.review}</td>
                    <td>${rev.createdAt}</td>
                    <td>
                        <form method="post" action="${pageContext.request.contextPath}/rider/rate-delete"
                              onsubmit="return confirm('Delete this review?');">
                            <input type="hidden" name="ratingId" value="${rev.id}">
                            <button type="submit" class="btn danger" style="padding:2px 8px;font-size:12px;">Delete</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty reviews}">
                <tr><td colspan="5" class="muted">You haven't submitted any reviews yet.</td></tr>
            </c:if>
        </table>
    </div>
</div>
</body>
</html>
