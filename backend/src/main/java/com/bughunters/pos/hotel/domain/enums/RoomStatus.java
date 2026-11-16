package com.bughunters.pos.hotel.domain.enums;

/**
 * Physical housekeeping/operational status of a hotel room.
 *
 * <p>Status values (resolved from OQ-1):
 * <ul>
 *   <li>{@code AVAILABLE}   – Room is clean and ready for allocation.</li>
 *   <li>{@code BOOKED}      – Room has an active (CONFIRMED) reservation.</li>
 *   <li>{@code CHECKED_IN}  – A guest is currently in the room.</li>
 *   <li>{@code MAINTENANCE} – Room is out of service (maintenance/repairs).</li>
 * </ul>
 */
public enum RoomStatus {
    AVAILABLE,
    BOOKED,
    CHECKED_IN,
    MAINTENANCE
}
