package com.bughunters.pos.hotel.controller;

import com.bughunters.pos.common.ApiResponse;
import com.bughunters.pos.hotel.dto.request.ReservationRequest;
import com.bughunters.pos.hotel.dto.response.ReservationResponse;
import com.bughunters.pos.hotel.service.ReservationService;
import com.bughunters.pos.security.SecurityContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Reservation management.
 */
@RestController
@RequestMapping("/api/v1/hotel/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Manage hotel reservations")
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * POST /api/v1/hotel/reservations
     * Creates a new reservation, allocates rooms, snapshots rates.
     */
    @PostMapping
    @Operation(summary = "Create a new reservation with room allocation(s)")
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @Valid @RequestBody ReservationRequest request) {
        Long branchId = SecurityContextHolder.get().getCurrentBranchId();
        ReservationResponse created = reservationService.createReservation(request, branchId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Reservation created successfully", created));
    }

    /**
     * GET /api/v1/hotel/reservations/{id}
     * Retrieves a reservation by ID, scoped to the caller's branch.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get reservation details by ID")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservation(
            @PathVariable Long id) {
        Long branchId = SecurityContextHolder.get().getCurrentBranchId();
        ReservationResponse data = reservationService.getReservation(id, branchId);
        return ResponseEntity.ok(ApiResponse.ok("Reservation retrieved", data));
    }

    /**
     * PATCH /api/v1/hotel/reservations/{id}/cancel
     * Cancels a reservation and releases all room allocations.
     */
    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a reservation and release all allocated rooms")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(
            @PathVariable Long id) {
        Long branchId = SecurityContextHolder.get().getCurrentBranchId();
        ReservationResponse cancelled = reservationService.cancelReservation(id, branchId);
        return ResponseEntity.ok(ApiResponse.ok("Reservation cancelled", cancelled));
    }
}
