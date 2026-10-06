-- ============================================================
-- Migration: adds the ride_rejections table, which tracks which
-- drivers have already rejected a given ride, so the Driver
-- Matching & Dispatch module excludes them on re-match instead
-- of re-dispatching the same ride right back to them.
--
-- Run this once against your existing database. It does not
-- touch any existing data in other tables.
-- ============================================================
USE routex;

CREATE TABLE IF NOT EXISTS ride_rejections (
    ride_id     BIGINT NOT NULL,
    driver_id   BIGINT NOT NULL,
    rejected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ride_id, driver_id),
    FOREIGN KEY (ride_id) REFERENCES rides(id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES users(id)
);
