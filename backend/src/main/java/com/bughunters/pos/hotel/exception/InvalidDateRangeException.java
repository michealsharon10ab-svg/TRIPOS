package com.bughunters.pos.hotel.exception;

/**
 * Thrown when {@code checkOutDate} is not strictly after {@code checkInDate}
 * (Business Rule 1), or when any other date constraint is violated.
 *
 * <p>Maps to HTTP 400 Bad Request in {@link com.bughunters.pos.common.GlobalExceptionHandler}.
 */
public class InvalidDateRangeException extends RuntimeException {

    public InvalidDateRangeException(String message) {
        super(message);
    }
}
