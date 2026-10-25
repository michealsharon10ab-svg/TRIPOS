package com.bughunters.pos.administration.controller;

import com.bughunters.pos.administration.dto.*;
import com.bughunters.pos.administration.service.AuthService;
import com.bughunters.pos.administration.service.BranchService;
import com.bughunters.pos.administration.service.UserService;
import com.bughunters.pos.common.response.ApiResponse;
import com.bughunters.pos.config.TenantSecurityContext;
import com.bughunters.pos.config.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final BranchService branchService;

    public AuthController(AuthService authService, UserService userService, BranchService branchService) {
        this.authService = authService;
        this.userService = userService;
        this.branchService = branchService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        TokenResponse tokenResponse = authService.login(request, httpRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", tokenResponse));
    }

    @PostMapping({"/signup", "/register"})
    public ResponseEntity<ApiResponse<TokenResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        TokenResponse tokenResponse = authService.register(request, httpRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "Business account and admin registered successfully", tokenResponse));
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<ApiResponse<TokenResponse>> verify2Fa(
            @Valid @RequestBody Verify2FARequest request,
            HttpServletRequest httpRequest) {

        TokenResponse tokenResponse = authService.verify2Fa(request, httpRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "Two-factor verification successful", tokenResponse));
    }

    @PostMapping("/pin-login")
    public ResponseEntity<ApiResponse<TokenResponse>> pinLogin(
            @Valid @RequestBody PinLoginRequest request,
            HttpServletRequest httpRequest) {

        TokenResponse tokenResponse = authService.pinLogin(request, httpRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "Terminal PIN login successful", tokenResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        TokenResponse tokenResponse = authService.refreshToken(request, httpRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "Token refreshed successfully", tokenResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {

        String refreshToken = body != null ? body.get("refreshToken") : null;
        authService.logout(refreshToken, currentUser, httpRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "Logged out successfully", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        UserResponseDTO userDTO = userService.getUserById(currentUser.getId(), currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "Current user profile", userDTO));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {

        authService.changePassword(currentUser.getId(), request, httpRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "Password changed successfully. Please sign in with your new password.", null));
    }

    @PostMapping("/2fa/setup")
    public ResponseEntity<ApiResponse<Setup2FAResponse>> setup2Fa(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Setup2FAResponse response = authService.setup2Fa(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "2FA setup initiated", response));
    }

    @PostMapping("/2fa/enable")
    public ResponseEntity<ApiResponse<String>> enable2Fa(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {

        String secret = body.get("secret");
        String code = body.get("code");
        authService.enable2Fa(currentUser.getId(), secret, code, httpRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "Two-factor authentication enabled successfully", null));
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<ApiResponse<String>> disable2Fa(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {

        String password = body.get("password");
        authService.disable2Fa(currentUser.getId(), password, httpRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "Two-factor authentication disabled", null));
    }

    @GetMapping("/tenant/{tenantCode}/branches")
    public ResponseEntity<ApiResponse<List<BranchSummaryDTO>>> getTenantBranches(
            @PathVariable String tenantCode) {

        List<BranchSummaryDTO> branches = branchService.getBranchesByTenantCode(tenantCode);
        return ResponseEntity.ok(new ApiResponse<>(true, "Branches for tenant: " + tenantCode, branches));
    }
}
