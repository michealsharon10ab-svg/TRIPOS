package com.bughunters.pos.common;

import com.bughunters.pos.hotel.exception.BranchAccessDeniedException;
import com.bughunters.pos.hotel.exception.InvalidDateRangeException;
import com.bughunters.pos.hotel.exception.ResourceNotFoundException;
import com.bughunters.pos.hotel.exception.RoomNotAvailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Centralised exception → HTTP response mapping for the entire application.
 * Business exceptions thrown from service layer are caught here, never in controllers.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* ── 404 Not Found ─────────────────────────────────────────────────── */

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /* ── 400 Bad Request — business rule violations ─────────────────────── */

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDate(InvalidDateRangeException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ex.getMessage()));
    }

    /* ── 409 Conflict — room availability ───────────────────────────────── */

    @ExceptionHandler(RoomNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleNotAvailable(RoomNotAvailableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /* ── 403 Forbidden — cross-branch access ────────────────────────────── */

    @ExceptionHandler(BranchAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleBranchDenied(BranchAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /* ── 400 Bad Request — @Valid / @Validated failures ─────────────────── */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("Validation failed", errors));
    }

    /* ── 500 Internal Server Error — catch-all ───────────────────────────── */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected error occurred. Please contact support."));
    }
}
