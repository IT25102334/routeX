package com.routex.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** One row of a wallet's transaction history (top-up, redemption, ride payment, refund). */
public class WalletTransaction {

    private long id;
    private long userId;
    private String type;      // TOPUP | REWARD | REDEEM | RIDE_PAYMENT | REFUND
    private BigDecimal amount;
    private String note;
    private Timestamp createdAt;

    public WalletTransaction(long id, long userId, String type, BigDecimal amount, String note, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.note = note;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public long getUserId() { return userId; }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getNote() { return note; }
    public Timestamp getCreatedAt() { return createdAt; }
}
