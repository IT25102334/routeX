package com.routex.model;

import java.sql.Timestamp;

/**
 * An Admin account. Admins manage users/drivers and view platform-wide
 * analytics (Administration & Reporting module).
 */
public class Admin extends User {

    public Admin(long id, String name, String email, String phone, String status, Timestamp createdAt) {
        super(id, name, email, phone, status, createdAt);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}
