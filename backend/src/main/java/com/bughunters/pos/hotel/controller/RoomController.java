package com.bughunters.pos.hotel.controller;

import com.bughunters.pos.common.ApiResponse;
import com.bughunters.pos.hotel.domain.enums.RoomStatus;
import com.bughunters.pos.hotel.dto.request.RoomRequest;
import com.bughunters.pos.hotel.dto.request.RoomStatusPatchRequest;
import com.bughunters.pos.hotel.dto.response.AvailabilityResponse;
import com.bughunters.pos.hotel.dto.response.RoomResponse;
import com.bughunters.pos.hotel.service.RoomService;
import com.bughunters.pos.security.SecurityContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for Room and Availability management.
 */
@RestController
@RequestMapping("/api/v1/hotel")
@RequiredArgsConstructor
@Tag(name = "Rooms", description = "Manage hotel rooms and availability")
public class RoomController {

    private final RoomService roomService;

    /**
     * GET /api/v1/hotel/rooms?status=&branchId=
     * Lists rooms; optionally filters by status. branchId param is validated
     * against the caller's own branchId (callers cannot query other branches).
     */
    @GetMapping("/rooms")
    @Operation(summary = "List rooms for the current branch, optionally filtered by status")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> listRooms(
            @Parameter(description = "Filter by room status (optional)")
            @RequestParam(required = false) RoomStatus status) {
        Long branchId = SecurityContextHolder.get().getCurrentBranchId();
        List<RoomResponse> data = roomService.listRooms(branchId, status);
        return ResponseEntity.ok(ApiResponse.ok("Rooms retrieved successfully", data));
    }

    /**
     * POST /api/v1/hotel/rooms
     * Creates a new room in the caller's branch.
     */
    @PostMapping("/rooms")
    @Operation(summary = "Create a new room")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(
            @Valid @RequestBody RoomRequest request) {
        Long branchId = SecurityContextHolder.get().getCurrentBranchId();
        RoomResponse created = roomService.createRoom(request, branchId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Room created successfully", created));
    }

    /**
     * PATCH /api/v1/hotel/rooms/{id}/status
     * Updates the housekeeping/operational status of a room.
     */
    @PatchMapping("/rooms/{id}/status")
    @Operation(summary = "Update room status (e.g., AVAILABLE → MAINTENANCE)")
    public ResponseEntity<ApiResponse<RoomResponse>> patchRoomStatus(
            @PathVariable Long id,
            @Valid @RequestBody RoomStatusPatchRequest request) {
        Long branchId = SecurityContextHolder.get().getCurrentBranchId();
        RoomResponse updated = roomService.patchStatus(id, request.getStatus(), branchId);
        return ResponseEntity.ok(ApiResponse.ok("Room status updated", updated));
    }

    /**
     * GET /api/v1/hotel/availability?from=&to=&roomTypeId=
     * Returns rooms of a type that are free for the entire date range.
     */
    @GetMapping("/availability")
    @Operation(summary = "Check room availability for a date range and room type")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkAvailability(
            @Parameter(description = "Check-in date (ISO: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "Check-out date (ISO: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @Parameter(description = "Room type ID to search", required = true)
            @RequestParam Long roomTypeId) {
        Long branchId = SecurityContextHolder.get().getCurrentBranchId();
        AvailabilityResponse data = roomService.checkAvailability(from, to, roomTypeId, branchId);
        return ResponseEntity.ok(ApiResponse.ok("Availability retrieved", data));
    }
}
