package com.bughunters.pos.administration.controller;

import com.bughunters.pos.administration.dto.*;
import com.bughunters.pos.administration.service.UserService;
import com.bughunters.pos.common.response.ApiResponse;
import com.bughunters.pos.common.response.PagedResponse;
import com.bughunters.pos.config.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponseDTO>>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        String[] sortParts = sort.split(",");
        Sort sortObj = Sort.by(sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1]) ? Sort.Direction.ASC : Sort.Direction.DESC, sortParts[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<UserResponseDTO> result = userService.listUsers(search, pageable, currentUser);

        PagedResponse<UserResponseDTO> pagedResponse = new PagedResponse<>();
        pagedResponse.setContent(result.getContent());
        pagedResponse.setPageNumber(result.getNumber());
        pagedResponse.setPageSize(result.getSize());
        pagedResponse.setTotalElements(result.getTotalElements());
        pagedResponse.setTotalPages(result.getTotalPages());
        pagedResponse.setLast(result.isLast());

        return ResponseEntity.ok(new ApiResponse<>(true, "Staff members retrieved successfully", pagedResponse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        UserResponseDTO user = userService.getUserById(id, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "User details retrieved", user));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {

        UserResponseDTO user = userService.createUser(request, currentUser, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Staff account created successfully", user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {

        UserResponseDTO updated = userService.updateUser(id, request, currentUser, httpRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "Staff account updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USER_DISABLE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {

        userService.toggleUserStatus(id, active, currentUser, httpRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "Account status changed to: " + (active ? "ACTIVE" : "DEACTIVATED"), null));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('USER_UPDATE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> resetUserPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {

        userService.resetPassword(id, request, currentUser, httpRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "Staff password reset successfully", null));
    }

    @PostMapping("/{id}/set-pin")
    @PreAuthorize("hasAuthority('USER_UPDATE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> setUserPin(
            @PathVariable Long id,
            @Valid @RequestBody SetPinRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {

        userService.setUserPin(id, request, currentUser, httpRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "POS terminal PIN configured successfully", null));
    }
}
