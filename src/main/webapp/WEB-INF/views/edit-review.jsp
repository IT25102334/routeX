<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html>
<head>
    <title>RouteX - Edit Your Review</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<div class="topbar">
    <span class="brand">RouteX</span>
    <div>
        <a href="${pageContext.request.contextPath}/rider/reviews">My Reviews</a>
        <a href="${pageContext.request.contextPath}/rider/dashboard">My Rides</a>
        <a href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
</div>

<div class="container">
    <div class="card" style="max-width:480px;margin:0 auto;">
        <h2>Edit Your Review</h2>
        <p class="muted">Ride #${rating.rideId}</p>

        <c:if test="${not empty error}">
            <p style="color:#c0392b;background:#fdecea;border:1px solid #c0392b;padding:8px 12px;border-radius:4px;">${error}</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/rider/rate-edit">
            <input type="hidden" name="ratingId" value="${rating.id}">

            <label class="muted">Stars (1-5)</label>
            <select name="stars" required>
                <option value="5" ${rating.stars == 5 ? 'selected' : ''}>★★★★★ Excellent</option>
                <option value="4" ${rating.stars == 4 ? 'selected' : ''}>★★★★ Good</option>
                <option value="3" ${rating.stars == 3 ? 'selected' : ''}>★★★ Okay</option>
                <option value="2" ${rating.stars == 2 ? 'selected' : ''}>★★ Poor</option>
                <option value="1" ${rating.stars == 1 ? 'selected' : ''}>★ Very Poor</option>
            </select>

            <textarea name="review" rows="4" placeholder="Optional review (e.g. 'Clean car', 'Safe driving')">${rating.review}</textarea>

            <button type="submit">Save Changes</button>
        </form>
    </div>
</div>
</body>
</html>
