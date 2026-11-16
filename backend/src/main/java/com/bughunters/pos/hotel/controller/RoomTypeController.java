package com.bughunters.pos.hotel.controller;

import com.bughunters.pos.common.ApiResponse;
import com.bughunters.pos.hotel.dto.request.RoomTypeRequest;
import com.bughunters.pos.hotel.dto.response.RoomTypeResponse;
import com.bughunters.pos.hotel.service.RoomTypeService;
import com.bughunters.pos.security.SecurityContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Room Type management.
 *
 * <p>
 * All endpoints are scoped to the caller's {@code branchId} retrieved from
 * {@link SecurityContextHolder} (placeholder — see ASS-7 / Sharon's JWT
 * module).
 */
@RestController
@RequestMapping("/api/v1/hotel/room-types")
@RequiredArgsConstructor
@Tag(name = "Room Types", description = "Manage hotel room type categories")
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    /**
     * GET /api/v1/hotel/room-types
     * Returns all room types for the caller's branch.
     */
    @GetMapping
    @Operation(summary = "List room types for the current branch")
    public ResponseEntity<ApiResponse<List<RoomTypeResponse>>> listRoomTypes() {
        Long branchId = SecurityContextHolder.get().getCurrentBranchId();
        List<RoomTypeResponse> data = roomTypeService.listByBranch(branchId);
        return ResponseEntity.ok(ApiResponse.ok("Room types retrieved successfully", data));
    }

    /**
     * POST /api/v1/hotel/room-types
     * Creates a new room type scoped to the caller's branch.
     */
    @PostMapping
    @Operation(summary = "Create a new room type for the current branch")
    public ResponseEntity<ApiResponse<RoomTypeResponse>> createRoomType(
            @Valid @RequestBody RoomTypeRequest request) {
        Long branchId = SecurityContextHolder.get().getCurrentBranchId();
        RoomTypeResponse created = roomTypeService.create(request, branchId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Room type created successfully", created));
    }
}
