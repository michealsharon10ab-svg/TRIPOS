package com.bughunters.pos.hotel.domain.enums;

/**
 * Channel through which a hotel reservation was made.
 *
 * <ul>
 *   <li>{@code WALK_IN} – Guest arrived without a prior booking.</li>
 *   <li>{@code ONLINE}  – Booked via the online portal or OTA.</li>
 *   <li>{@code PHONE}   – Booked over the phone by a receptionist.</li>
 * </ul>
 */
public enum BookingSource {
    WALK_IN,
    ONLINE,
    PHONE
}
