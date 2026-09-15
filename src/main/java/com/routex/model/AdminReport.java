package com.routex.model;

import java.sql.Timestamp;

/** An operational/dispute report logged by an Admin (Administration & Reporting module). */
public class AdminReport {

    private long id;
    private long adminId;
    private String title;
    private String details;
    private String status; // OPEN | RESOLVED
    private Timestamp createdAt;

    public AdminReport(long id, long adminId, String title, String details, String status, Timestamp createdAt) {
        this.id = id;
        this.adminId = adminId;
        this.title = title;
        this.details = details;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public long getAdminId() { return adminId; }
    public String getTitle() { return title; }
    public String getDetails() { return details; }
    public String getStatus() { return status; }
    public Timestamp getCreatedAt() { return createdAt; }
}
