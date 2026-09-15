<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
    <title>RouteX - Register</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<div class="auth card">
    <div class="brand">RouteX</div>
    <p class="muted">Create your account</p>

    <% if (request.getAttribute("error") != null) { %>
        <p class="error"><%= request.getAttribute("error") %></p>
    <% } %>

    <form method="post" action="${pageContext.request.contextPath}/register">
        <input name="name" type="text" placeholder="Full name" required>
        <input name="email" type="email" placeholder="Email" required>
        <input name="phone" type="tel" placeholder="Phone number" required>
        <input name="password" type="password" placeholder="Password" required minlength="6">

        <select name="role" id="roleSelect" required onchange="toggleDriverFields()">
            <option value="RIDER">I'm a Rider</option>
            <option value="DRIVER">I'm a Driver</option>
        </select>

        <div id="driverFields" style="display:none;">
            <input name="licenseNo" type="text" placeholder="Driving license number">
            <select name="vehicleType">
                <option value="CAR">Car</option>
                <option value="VAN">Van</option>
                <option value="TUK_TUK">Tuk-tuk</option>
                <option value="BIKE">Motorbike</option>
            </select>
            <input name="vehicleInfo" type="text" placeholder="Vehicle details (e.g. plate number, model)">
            <p class="muted" style="text-align:left;font-size:12px;">
                Your account is created immediately, but you'll need Admin verification
                before you can go online and accept rides.
            </p>
        </div>

        <button type="submit">Create account</button>
    </form>

    <p class="muted">Already have an account? <a href="${pageContext.request.contextPath}/login">Log in</a></p>
</div>

<script>
    function toggleDriverFields() {
        var isDriver = document.getElementById('roleSelect').value === 'DRIVER';
        document.getElementById('driverFields').style.display = isDriver ? 'block' : 'none';
    }
</script>
</body>
</html>
