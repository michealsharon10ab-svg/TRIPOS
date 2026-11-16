-- ============================================================
-- V2__hotel_schema.sql
-- Hotel module schema: room_types, rooms, guests,
-- reservations, room_allocations
--
-- Owned by: Member 5 (Kajanika P — Hotel & Reporting)
-- Migration tool: Flyway
-- Target DB: MySQL 8.0
--
-- Naming: V2__ to leave V1__ for Member 1 (Sharon)'s
-- auth/branch tables.  ASS-10.
-- ============================================================

-- ── 1. room_types ────────────────────────────────────────────
CREATE TABLE room_types (
    room_type_id  BIGINT         NOT NULL AUTO_INCREMENT,
    type_name     VARCHAR(100)   NOT NULL,
    base_rate     DECIMAL(12, 2) NOT NULL,
    max_occupancy INT            NOT NULL,
    branch_id     BIGINT         NOT NULL,           -- FK placeholder; Branch owned by another module
    CONSTRAINT pk_room_types PRIMARY KEY (room_type_id),
    CONSTRAINT uq_room_type_name_branch UNIQUE (type_name, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── 2. rooms ─────────────────────────────────────────────────
CREATE TABLE rooms (
    room_id      BIGINT       NOT NULL AUTO_INCREMENT,
    room_no      VARCHAR(20)  NOT NULL,
    floor_no     INT          NOT NULL,
    room_status  VARCHAR(20)  NOT NULL
                     CHECK (room_status IN ('AVAILABLE','BOOKED','CHECKED_IN','MAINTENANCE')),
    amenities    TEXT,
    room_type_id BIGINT       NOT NULL,
    branch_id    BIGINT       NOT NULL,
    version      BIGINT       NOT NULL DEFAULT 0,    -- optimistic lock
    CONSTRAINT pk_rooms         PRIMARY KEY (room_id),
    CONSTRAINT uq_room_no_branch UNIQUE (room_no, branch_id),
    CONSTRAINT fk_room_room_type
        FOREIGN KEY (room_type_id) REFERENCES room_types (room_type_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Index for availability query (JOIN on room_type_id)
CREATE INDEX idx_rooms_room_type ON rooms (room_type_id);
-- Index for branch-scoped list
CREATE INDEX idx_rooms_branch ON rooms (branch_id);

-- ── 3. guests ────────────────────────────────────────────────
-- OQ-3: Separate from Restaurant/CRM CUSTOMER entity.
CREATE TABLE guests (
    guest_id       BIGINT       NOT NULL AUTO_INCREMENT,
    nic_passport_no VARCHAR(50) NOT NULL,
    name           VARCHAR(150) NOT NULL,
    nationality    VARCHAR(100),
    phone          VARCHAR(20),
    CONSTRAINT pk_guests            PRIMARY KEY (guest_id),
    CONSTRAINT uq_guest_nic_passport UNIQUE (nic_passport_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── 4. reservations ──────────────────────────────────────────
CREATE TABLE reservations (
    reservation_id BIGINT      NOT NULL AUTO_INCREMENT,
    check_in_date  DATE        NOT NULL,
    check_out_date DATE        NOT NULL,
    adults         INT         NOT NULL,
    children       INT         NOT NULL DEFAULT 0,
    res_status     VARCHAR(20) NOT NULL
                       CHECK (res_status IN ('CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED')),
    booking_source VARCHAR(20) NOT NULL
                       CHECK (booking_source IN ('WALK_IN','ONLINE','PHONE')),
    guest_id       BIGINT      NOT NULL,
    branch_id      BIGINT      NOT NULL,
    version        BIGINT      NOT NULL DEFAULT 0,  -- optimistic lock
    CONSTRAINT pk_reservations PRIMARY KEY (reservation_id),
    CONSTRAINT chk_dates CHECK (check_out_date > check_in_date),
    CONSTRAINT fk_reservation_guest
        FOREIGN KEY (guest_id) REFERENCES guests (guest_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indexes for common queries
CREATE INDEX idx_res_branch_status ON reservations (branch_id, res_status);
CREATE INDEX idx_res_guest         ON reservations (guest_id);
CREATE INDEX idx_res_dates         ON reservations (check_in_date, check_out_date);

-- ── 5. room_allocations ───────────────────────────────────────
-- Association class: one reservation can span multiple rooms;
-- nightlyRate is a snapshot (Business Rule 4).
CREATE TABLE room_allocations (
    reservation_id BIGINT         NOT NULL,
    room_id        BIGINT         NOT NULL,
    nightly_rate   DECIMAL(12, 2) NOT NULL,
    CONSTRAINT pk_room_allocations PRIMARY KEY (reservation_id, room_id),
    CONSTRAINT fk_alloc_reservation
        FOREIGN KEY (reservation_id) REFERENCES reservations (reservation_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_alloc_room
        FOREIGN KEY (room_id) REFERENCES rooms (room_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Index to speed up overlap-check query (room_id + dates via reservation join)
CREATE INDEX idx_alloc_room ON room_allocations (room_id);
