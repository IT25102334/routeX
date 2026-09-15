package com.routex.model;

import java.math.BigDecimal;

/**
 * A rider's or driver's in-app wallet: cash balance plus loyalty points.
 * Owned by the Wallet & Rewards Management module.
 */
public class Wallet {

    private long userId;
    private BigDecimal balance = BigDecimal.ZERO;
    private int points = 0;

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}
