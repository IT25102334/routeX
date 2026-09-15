<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!doctype html>
<html>
<head>
    <title>RouteX - Wallet &amp; Rewards</title>
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
    <c:if test="${not empty sessionScope.walletError}">
        <p class="error">${sessionScope.walletError}</p>
        <c:remove var="walletError" scope="session"/>
    </c:if>

    <div class="metrics-grid">
        <div class="metric-box">
            <div class="value"><fmt:formatNumber value="${wallet.balance}" type="currency" currencySymbol="Rs. "/></div>
            <div class="label">Wallet Balance</div>
        </div>
        <div class="metric-box">
            <div class="value">${wallet.points}</div>
            <div class="label">Loyalty Points</div>
        </div>
    </div>

    <div class="card" style="max-width:420px;">
        <h3>Top Up Wallet</h3>
        <form method="post" action="${pageContext.request.contextPath}/rider/wallet">
            <input type="hidden" name="action" value="TOPUP">
            <input type="number" name="amount" min="1" step="0.01" placeholder="Amount (Rs.)" required>
            <button type="submit">Top Up</button>
        </form>
    </div>

    <div class="card" style="max-width:420px;">
        <h3>Redeem Loyalty Points</h3>
        <p class="muted">100 points = Rs. 10.00 wallet credit. You have ${wallet.points} points.</p>
        <form method="post" action="${pageContext.request.contextPath}/rider/wallet">
            <input type="hidden" name="action" value="REDEEM">
            <input type="number" name="points" min="100" step="1" placeholder="Points to redeem" required>
            <button type="submit">Redeem</button>
        </form>
    </div>

    <div class="card">
        <h3>Transaction History</h3>
        <table>
            <tr><th>Type</th><th>Amount</th><th>Note</th><th>Date</th></tr>
            <c:forEach var="tx" items="${transactions}">
                <tr>
                    <td><span class="badge">${tx.type}</span></td>
                    <td><fmt:formatNumber value="${tx.amount}" type="currency" currencySymbol="Rs. "/></td>
                    <td>${tx.note}</td>
                    <td>${tx.createdAt}</td>
                </tr>
            </c:forEach>
            <c:if test="${empty transactions}">
                <tr><td colspan="4" class="muted">No transactions yet.</td></tr>
            </c:if>
        </table>
    </div>
</div>
</body>
</html>
