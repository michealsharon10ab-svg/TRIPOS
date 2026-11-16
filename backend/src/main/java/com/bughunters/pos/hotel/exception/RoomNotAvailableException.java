package com.bughunters.pos.hotel.exception;

/**
 * Thrown when a room is unavailable for the requested date range
 * due to an overlapping active reservation (Business Rule 2).
 *
 * <p>Maps to HTTP 409 Conflict in {@link com.bughunters.pos.common.GlobalExceptionHandler}.
 */
public class RoomNotAvailableException extends RuntimeException {

    public RoomNotAvailableException(Long roomId, String from, String to) {
        super("Room " + roomId + " is not available from " + from + " to " + to + ".");
    }

    public RoomNotAvailableException(String message) {
        super(message);
    }
}
