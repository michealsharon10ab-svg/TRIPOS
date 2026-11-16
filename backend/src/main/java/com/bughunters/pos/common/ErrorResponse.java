package com.bughunters.pos.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.List;

/**
 * Structured error response body.
 *
 * <p>Shape:
 * <pre>{@code
 * {
 *   "success": false,
 *   "message": "Validation failed",
 *   "errors":  ["checkOutDate must be after checkInDate", ...]
 * }
 * }</pre>
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final boolean      success = false;
    private final String       message;
    private final List<String> errors;

    public ErrorResponse(String message) {
        this.message = message;
        this.errors  = null;
    }

    public ErrorResponse(String message, List<String> errors) {
        this.message = message;
        this.errors  = errors;
    }
}
