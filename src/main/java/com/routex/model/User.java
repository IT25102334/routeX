package com.routex.model;

import java.sql.Timestamp;

/**
 * Base class for every account in RouteX.
 * Rider, Driver and Admin all extend this class - this is the
 * class hierarchy that backs the "role" column in the users table.
 * Instances are built by {@link com.routex.factory.UserFactory}
 * (Factory Method design pattern) so that calling code never has
 * to know which concrete subtype it is dealing with until it needs to.
 */
public abstract class User {

    protected long id;
    protected String name;
    protected String email;
    protected String phone;
    protected String passwordHash;
    protected String status;      // ACTIVE | SUSPENDED
    protected Timestamp createdAt;

    public User(long id, String name, String email, String phone, String status, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.createdAt = createdAt;
    }

    /** Each subclass reports which role it represents (matches the DB enum). */
    public abstract String getRole();

    public long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getStatus() { return status; }
    public Timestamp getCreatedAt() { return createdAt; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public boolean isActive() { return "ACTIVE".equals(status); }
}
