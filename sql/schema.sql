-- ============================================================
-- RouteX Database Schema
-- SE2030 - Software Engineering Group Project (Group 13)
-- ============================================================

CREATE DATABASE IF NOT EXISTS routex CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE routex;

-- ------------------------------------------------------------
-- Core account table. Every Rider, Driver and Admin is a row
-- here; the "role" column decides which extra table (if any)
-- holds their role-specific data. This is the "Single Table"
-- strategy behind the User / Rider / Driver / Admin class
-- hierarchy in the Java code (see UserFactory).
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    name          VARCHAR(120) NOT NULL,
    email         VARCHAR(160) NOT NULL UNIQUE,
    phone         VARCHAR(40),
    password_hash VARCHAR(100) NOT NULL,
    role          ENUM('RIDER','DRIVER','ADMIN') NOT NULL,
    status        ENUM('ACTIVE','SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- Driver-specific data (Driver Matching & Dispatch module)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS drivers (
    user_id       BIGINT PRIMARY KEY,
    license_no    VARCHAR(60),
    vehicle_type  ENUM('CAR','VAN','TUK_TUK','BIKE') NOT NULL DEFAULT 'CAR',
    vehicle_info  VARCHAR(150),
    availability  ENUM('ONLINE','OFFLINE') NOT NULL DEFAULT 'OFFLINE',
    verified      TINYINT(1) NOT NULL DEFAULT 0,
    rating        DECIMAL(3,2) NOT NULL DEFAULT 5.00,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- Rides (Ride Booking & Fare Estimation / Driver Matching /
-- Live Trip Management all operate on this one table, each
-- moving it through a different slice of the status lifecycle)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rides (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    rider_id        BIGINT NOT NULL,
    driver_id       BIGINT NULL,
    pickup          VARCHAR(255) NOT NULL,
    dropoff         VARCHAR(255) NOT NULL,
    ride_type       ENUM('STANDARD','PREMIUM','POOL') NOT NULL DEFAULT 'STANDARD',
    vehicle_type    ENUM('CAR','VAN','TUK_TUK','BIKE') NOT NULL DEFAULT 'CAR',
    estimated_fare  DECIMAL(10,2) NOT NULL,
    final_fare      DECIMAL(10,2),
    status          ENUM('REQUESTED','DISPATCHED','ACCEPTED','ARRIVED','IN_PROGRESS','COMPLETED','CANCELLED')
                    NOT NULL DEFAULT 'REQUESTED',
    current_lat     DECIMAL(10,7),
    current_lng     DECIMAL(10,7),
    sos_triggered   TINYINT(1) NOT NULL DEFAULT 0,
    requested_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at      TIMESTAMP NULL,
    completed_at    TIMESTAMP NULL,
    FOREIGN KEY (rider_id)  REFERENCES users(id),
    FOREIGN KEY (driver_id) REFERENCES users(id)
);

-- ------------------------------------------------------------
-- Wallet & Rewards Management module
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS wallets (
    user_id  BIGINT PRIMARY KEY,
    balance  DECIMAL(12,2) NOT NULL DEFAULT 0,
    points   INT NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS wallet_transactions (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL,
    type        ENUM('TOPUP','REWARD','REDEEM','RIDE_PAYMENT','REFUND') NOT NULL,
    amount      DECIMAL(12,2) NOT NULL,
    note        VARCHAR(255),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ------------------------------------------------------------
-- Ratings, Reviews & Driver Performance module
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ratings (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    ride_id       BIGINT NOT NULL,
    from_user_id  BIGINT NOT NULL,
    to_user_id    BIGINT NOT NULL,
    stars         INT NOT NULL,
    review        VARCHAR(500),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY one_rating_per_user_ride (ride_id, from_user_id),
    FOREIGN KEY (ride_id)      REFERENCES rides(id),
    FOREIGN KEY (from_user_id) REFERENCES users(id),
    FOREIGN KEY (to_user_id)   REFERENCES users(id)
);

-- ------------------------------------------------------------
-- Administration & Reporting module
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS admin_reports (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id    BIGINT NOT NULL,
    title       VARCHAR(180) NOT NULL,
    details     TEXT NOT NULL,
    status      ENUM('OPEN','RESOLVED') NOT NULL DEFAULT 'OPEN',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id)
);

-- ------------------------------------------------------------
-- Notifications - backs the Observer pattern used by the
-- Live Trip Management module (status changes / SOS alerts)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL,
    message     VARCHAR(255) NOT NULL,
    type        VARCHAR(40) NOT NULL,
    is_read     TINYINT(1) NOT NULL DEFAULT 0,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Note: create your first Admin by registering normally, then run:
-- UPDATE users SET role='ADMIN' WHERE email='your@email.com';
-- then log out and log back in.
