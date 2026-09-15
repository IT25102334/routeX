<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
    <title>RouteX - Login</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<div class="auth card">
    <div class="brand">RouteX</div>
    <p class="muted">Web-based transport (ride-hailing) system</p>

    <% if (request.getParameter("registered") != null) { %>
        <p class="success-msg">Account created. Please log in.</p>
    <% } %>
    <% if (request.getAttribute("error") != null) { %>
        <p class="error"><%= request.getAttribute("error") %></p>
    <% } %>

    <form method="post" action="${pageContext.request.contextPath}/login">
        <input name="email" type="email" placeholder="Email" required>
        <input name="password" type="password" placeholder="Password" required>
        <button type="submit">Login</button>
    </form>

    <p class="muted">New here? <a href="${pageContext.request.contextPath}/register">Create an account</a></p>
</div>
</body>
</html>
