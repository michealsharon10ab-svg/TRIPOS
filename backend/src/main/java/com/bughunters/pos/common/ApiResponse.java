package com.bughunters.pos.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * Uniform API envelope used by all endpoints.
 *
 * <p>Success shape:  {@code { "success": true,  "message": "...", "data": {...} }}
 * <p>Failure shape:  {@code { "success": false, "message": "..." }}  (data omitted)
 *
 * @param <T> type of the payload
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String  message;
    private final T       data;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data    = data;
    }

    /** 200 / 201 helper. */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /** Success with no payload (e.g. PATCH cancel). */
    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(true, message, null);
    }

    /** Error helper (no data). */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
