package com.bughunters.pos.hotel.domain.enums;

/**
 * Lifecycle status of a hotel reservation.
 *
 * <ul>
 *   <li>{@code CONFIRMED}   – Reservation accepted; room allocated but guest not yet arrived.</li>
 *   <li>{@code CHECKED_IN}  – Guest has arrived and checked in.</li>
 *   <li>{@code CHECKED_OUT} – Guest has departed; room released for housekeeping.</li>
 *   <li>{@code CANCELLED}   – Reservation was cancelled; allocations released (Business Rule 5).</li>
 * </ul>
 */
public enum ReservationStatus {
    CONFIRMED,
    CHECKED_IN,
    CHECKED_OUT,
    CANCELLED
}
