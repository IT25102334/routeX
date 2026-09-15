-- ============================================================
-- Migration: adds vehicle_type support (run this ONLY if you
-- already created your database from an earlier version of
-- schema.sql and don't want to lose your test data).
--
-- If you don't care about existing test data, it's simpler to
-- just drop and recreate: DROP DATABASE routex; then re-run
-- the full sql/schema.sql instead of this file.
-- ============================================================
USE routex;

ALTER TABLE drivers
    ADD COLUMN vehicle_type ENUM('CAR','VAN','TUK_TUK','BIKE') NOT NULL DEFAULT 'CAR' AFTER license_no;

ALTER TABLE rides
    ADD COLUMN vehicle_type ENUM('CAR','VAN','TUK_TUK','BIKE') NOT NULL DEFAULT 'CAR' AFTER ride_type;
