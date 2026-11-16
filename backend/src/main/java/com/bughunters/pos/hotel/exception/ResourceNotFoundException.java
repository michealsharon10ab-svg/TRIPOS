package com.bughunters.pos.hotel.exception;

/**
 * Thrown when a requested entity (room, reservation, room type, guest) is not found,
 * or when the caller does not have access to it.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String entityName, Long id) {
        super(entityName + " with ID " + id + " was not found.");
    }
}
