package com.bughunters.pos.hotel.exception;

/**
 * Thrown when a user attempts to access a resource (room, reservation, room type)
 * that belongs to a different branch from their own (Business Rule 6).
 *
 * <p>Maps to HTTP 403 Forbidden in {@link com.bughunters.pos.common.GlobalExceptionHandler}.
 */
public class BranchAccessDeniedException extends RuntimeException {

    public BranchAccessDeniedException(String resourceType, Long resourceId) {
        super("Access denied: " + resourceType + " " + resourceId
              + " does not belong to your branch.");
    }

    public BranchAccessDeniedException(String message) {
        super(message);
    }
}
