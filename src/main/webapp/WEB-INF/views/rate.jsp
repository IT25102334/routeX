<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
    <title>RouteX - Rate Your Driver</title>
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
    <div class="card" style="max-width:480px;margin:0 auto;">
        <h2>Rate Trip #${ride.id}</h2>
        <p class="muted">${ride.pickup} &rarr; ${ride.dropoff}</p>

        <form method="post" action="${pageContext.request.contextPath}/rider/rate">
            <input type="hidden" name="rideId" value="${ride.id}">

            <label class="muted">Stars (1-5)</label>
            <select name="stars" required>
                <option value="5">★★★★★ Excellent</option>
                <option value="4">★★★★ Good</option>
                <option value="3">★★★ Okay</option>
                <option value="2">★★ Poor</option>
                <option value="1">★ Very Poor</option>
            </select>

            <textarea name="review" rows="4" placeholder="Optional review (e.g. 'Clean car', 'Safe driving')"></textarea>

            <button type="submit">Submit Rating</button>
        </form>
    </div>
</div>
</body>
</html>
