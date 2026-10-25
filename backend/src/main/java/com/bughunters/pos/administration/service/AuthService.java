package com.bughunters.pos.administration.service;

import com.bughunters.pos.administration.dto.*;
import com.bughunters.pos.administration.entity.*;
import com.bughunters.pos.administration.repository.*;
import com.bughunters.pos.common.exception.ApiException;
import com.bughunters.pos.common.exception.ResourceNotFoundException;
import com.bughunters.pos.common.util.TotpUtil;
import com.bughunters.pos.config.JwtTokenProvider;
import com.bughunters.pos.config.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TerminalRepository terminalRepository;
    private final BranchRepository branchRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Value("${app.security.pin.max-failed-attempts:5}")
    private int maxPinFailedAttempts;

    @Value("${app.security.pin.lockout-duration-minutes:15}")
    private int pinLockoutMinutes;

    @Value("${app.security.two-factor.issuer:TriPOS-BugHunters}")
    private String twoFactorIssuer;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            TerminalRepository terminalRepository,
            BranchRepository branchRepository,
            TenantRepository tenantRepository,
            RoleRepository roleRepository,
            JwtTokenProvider tokenProvider,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.terminalRepository = terminalRepository;
        this.branchRepository = branchRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public TokenResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String identifier = request.getUsername().trim();

        User user = userRepository.findByUsernameOrEmailWithRolesAndPermissions(identifier)
                .orElse(null);

        // Prevent username enumeration by running constant-time password check simulation
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            auditLogService.log(null, request.getBranchId(), null, identifier,
                    "LOGIN_FAILED", "Invalid credentials provided", httpRequest);
            throw new BadCredentialsException("Invalid username or password");
        }

        if (!user.isActive()) {
            auditLogService.log(user.getTenant() != null ? user.getTenant().getId() : null,
                    request.getBranchId(), user.getId(), user.getUsername(),
                    "LOGIN_FAILED", "Account deactivated", httpRequest);
            throw new BadCredentialsException("Account is deactivated. Please contact your administrator.");
        }

        if (user.getTenant() != null && !user.getTenant().isActive()) {
            auditLogService.log(user.getTenant().getId(), request.getBranchId(), user.getId(), user.getUsername(),
                    "LOGIN_FAILED", "Tenant inactive", httpRequest);
            throw new BadCredentialsException("Organization account is suspended.");
        }

        // Branch context resolution
        Branch activeBranch = resolveActiveBranch(user, request.getBranchId());

        // Check if 2FA is required
        if (user.isTwoFactorEnabled()) {
            String tempToken = tokenProvider.generate2FaPendingToken(user.getId(), user.getUsername());
            auditLogService.log(user.getTenant() != null ? user.getTenant().getId() : null,
                    activeBranch != null ? activeBranch.getId() : null, user.getId(), user.getUsername(),
                    "2FA_CHALLENGE_ISSUED", "Two-factor authentication prompt required", httpRequest);
            return TokenResponse.for2FA(tempToken);
        }

        return createAuthSession(user, activeBranch, httpRequest, "PASSWORD_LOGIN");
    }

    @Transactional
    public TokenResponse verify2Fa(Verify2FARequest request, HttpServletRequest httpRequest) {
        if (!tokenProvider.validateToken(request.getTempToken())) {
            throw new BadCredentialsException("Invalid or expired two-factor session token. Please log in again.");
        }

        String scope = tokenProvider.getScopeFromToken(request.getTempToken());
        if (!"2FA_PENDING".equals(scope)) {
            throw new BadCredentialsException("Invalid token scope for 2FA verification.");
        }

        String username = tokenProvider.getUsernameFromToken(request.getTempToken());
        User user = userRepository.findByUsernameOrEmailWithRolesAndPermissions(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!user.isTwoFactorEnabled() || user.getTwoFactorSecret() == null) {
            throw new ApiException("Two-factor authentication is not configured for this user");
        }

        boolean valid = TotpUtil.verifyCode(user.getTwoFactorSecret(), request.getCode().trim());
        if (!valid) {
            auditLogService.log(user.getTenant() != null ? user.getTenant().getId() : null,
                    request.getBranchId(), user.getId(), user.getUsername(),
                    "2FA_VERIFICATION_FAILED", "Incorrect TOTP code entered", httpRequest);
            throw new BadCredentialsException("Invalid verification code. Please check your authenticator app.");
        }

        Branch activeBranch = resolveActiveBranch(user, request.getBranchId());
        return createAuthSession(user, activeBranch, httpRequest, "2FA_LOGIN");
    }

    @Transactional
    public TokenResponse pinLogin(PinLoginRequest request, HttpServletRequest httpRequest) {
        // Find registered terminal
        Terminal terminal = terminalRepository.findByTenantCodeAndTerminalCode(
                request.getTenantCode().trim(), request.getTerminalCode().trim())
                .orElseThrow(() -> new BadCredentialsException("POS Terminal not found or not registered"));

        if (!terminal.isActive()) {
            throw new BadCredentialsException("This POS Terminal has been disabled by an administrator");
        }

        if (!terminal.getBranch().getId().equals(request.getBranchId())) {
            throw new BadCredentialsException("Terminal is not registered to the specified branch");
        }

        List<User> candidateUsers = userRepository.findActiveUsersByTenantAndBranch(
                terminal.getTenant().getId(), terminal.getBranch().getId());

        User authenticatedUser = null;
        for (User candidate : candidateUsers) {
            if (candidate.getPin() != null && !candidate.getPin().isBlank()) {
                if (passwordEncoder.matches(request.getPin(), candidate.getPin())) {
                    authenticatedUser = candidate;
                    break;
                }
            }
        }

        if (authenticatedUser == null) {
            auditLogService.log(terminal.getTenant().getId(), terminal.getBranch().getId(), null,
                    "TERMINAL:" + terminal.getTerminalCode(), "PIN_LOGIN_FAILED",
                    "Invalid PIN entered on terminal " + terminal.getTerminalCode(), httpRequest);
            throw new BadCredentialsException("Invalid PIN for this terminal");
        }

        // Check if user PIN is locked
        if (authenticatedUser.isPinLocked()) {
            auditLogService.log(terminal.getTenant().getId(), terminal.getBranch().getId(),
                    authenticatedUser.getId(), authenticatedUser.getUsername(),
                    "PIN_LOCKED_ACCESS_ATTEMPT", "Attempt to use locked PIN", httpRequest);
            throw new BadCredentialsException("This PIN is locked due to consecutive failed attempts. Try again in 15 minutes.");
        }

        // Enforce role security: POS PIN login cannot grant unrestricted SUPER_ADMIN privileges
        boolean hasSuperAdmin = authenticatedUser.getRoles().stream()
                .anyMatch(r -> "ROLE_SUPER_ADMIN".equals(r.getName()));
        if (hasSuperAdmin) {
            throw new AccessDeniedException("Administrative Super Admin accounts require full credential login");
        }

        // Reset pin failed attempts on success
        authenticatedUser.setPinFailedAttempts(0);
        authenticatedUser.setPinLockedUntil(null);
        userRepository.save(authenticatedUser);

        return createAuthSession(authenticatedUser, terminal.getBranch(), httpRequest, "PIN_LOGIN_TERMINAL_" + terminal.getTerminalCode());
    }

    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String rawToken = request.getRefreshToken();
        String tokenHash = JwtTokenProvider.hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            // Compromise detection: Attempt to reuse a revoked token! Revoke all tokens for this user!
            refreshTokenRepository.revokeAllByUser(refreshToken.getUser());
            auditLogService.log(refreshToken.getUser().getTenant() != null ? refreshToken.getUser().getTenant().getId() : null,
                    null, refreshToken.getUser().getId(), refreshToken.getUser().getUsername(),
                    "TOKEN_REUSE_DETECTED", "Revoked token reuse attempted. All user sessions invalidated.", httpRequest);
            throw new BadCredentialsException("Revoked token reuse detected. All sessions terminated.");
        }

        if (refreshToken.isExpired()) {
            throw new BadCredentialsException("Refresh token has expired. Please sign in again.");
        }

        User user = refreshToken.getUser();
        if (!user.isActive()) {
            throw new BadCredentialsException("User account is disabled");
        }

        // Token rotation: Revoke current refresh token and generate replacement
        refreshToken.setRevoked(true);

        String newRawRefreshToken = UUID.randomUUID().toString();
        String newTokenHash = JwtTokenProvider.hashToken(newRawRefreshToken);
        refreshToken.setReplacedByTokenHash(newTokenHash);
        refreshTokenRepository.save(refreshToken);

        Instant newExpiry = Instant.now().plusMillis(tokenProvider.getRefreshTokenExpirationMs());
        RefreshToken newRefreshToken = new RefreshToken(user, newTokenHash, newExpiry);
        refreshTokenRepository.save(newRefreshToken);

        UserPrincipal principal = UserPrincipal.create(user);
        String newAccessToken = tokenProvider.generateAccessToken(principal, null);

        auditLogService.log(user.getTenant() != null ? user.getTenant().getId() : null,
                null, user.getId(), user.getUsername(),
                "TOKEN_ROTATED", "Access token refreshed via refresh token rotation", httpRequest);

        return TokenResponse.success(
                newAccessToken,
                newRawRefreshToken,
                tokenProvider.getAccessTokenExpirationMs() / 1000,
                UserResponseDTO.fromEntity(user),
                null
        );
    }

    @Transactional
    public void logout(String refreshTokenStr, UserPrincipal currentUser, HttpServletRequest httpRequest) {
        if (refreshTokenStr != null && !refreshTokenStr.isBlank()) {
            String tokenHash = JwtTokenProvider.hashToken(refreshTokenStr.trim());
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
        }

        if (currentUser != null) {
            auditLogService.log(currentUser.getTenantId(), null, currentUser.getId(),
                    currentUser.getUsername(), "LOGOUT", "User logged out successfully", httpRequest);
        }
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request, HttpServletRequest httpRequest) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ApiException("New password and confirm password do not match");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ApiException("New password cannot be the same as current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Invalidate active refresh tokens on password change for security
        refreshTokenRepository.revokeAllByUser(user);

        auditLogService.log(user.getTenant() != null ? user.getTenant().getId() : null,
                null, user.getId(), user.getUsername(),
                "PASSWORD_CHANGE", "User successfully changed password", httpRequest);
    }

    public Setup2FAResponse setup2Fa(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String secret = TotpUtil.generateSecretKey();
        String qrCodeUri = TotpUtil.getQrCodeUri(secret, user.getEmail(), twoFactorIssuer);

        return new Setup2FAResponse(secret, qrCodeUri);
    }

    @Transactional
    public void enable2Fa(Long userId, String secret, String code, HttpServletRequest httpRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!TotpUtil.verifyCode(secret, code.trim())) {
            throw new BadCredentialsException("Invalid code. Please re-scan QR code and verify again.");
        }

        user.setTwoFactorSecret(secret);
        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        auditLogService.log(user.getTenant() != null ? user.getTenant().getId() : null,
                null, user.getId(), user.getUsername(),
                "2FA_ENABLED", "Two-factor authentication successfully enabled", httpRequest);
    }

    @Transactional
    public void disable2Fa(Long userId, String password, HttpServletRequest httpRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Password verification failed. Cannot disable 2FA.");
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);

        auditLogService.log(user.getTenant() != null ? user.getTenant().getId() : null,
                null, user.getId(), user.getUsername(),
                "2FA_DISABLED", "Two-factor authentication disabled", httpRequest);
    }

    private TokenResponse createAuthSession(User user, Branch activeBranch, HttpServletRequest httpRequest, String loginMethod) {
        UserPrincipal principal = UserPrincipal.create(user);
        Long branchId = activeBranch != null ? activeBranch.getId() : null;

        String accessToken = tokenProvider.generateAccessToken(principal, branchId);

        // Generate and persist refresh token
        String rawRefreshToken = UUID.randomUUID().toString();
        String tokenHash = JwtTokenProvider.hashToken(rawRefreshToken);
        Instant expiry = Instant.now().plusMillis(tokenProvider.getRefreshTokenExpirationMs());

        RefreshToken refreshToken = new RefreshToken(user, tokenHash, expiry);
        refreshTokenRepository.save(refreshToken);

        auditLogService.log(
                user.getTenant() != null ? user.getTenant().getId() : null,
                branchId,
                user.getId(),
                user.getUsername(),
                "LOGIN_SUCCESS",
                "Authenticated via " + loginMethod + (activeBranch != null ? " on branch " + activeBranch.getName() : ""),
                httpRequest
        );

        return TokenResponse.success(
                accessToken,
                rawRefreshToken,
                tokenProvider.getAccessTokenExpirationMs() / 1000,
                UserResponseDTO.fromEntity(user),
                activeBranch != null ? new BranchSummaryDTO(activeBranch) : null
        );
    }

    private Branch resolveActiveBranch(User user, Long requestedBranchId) {
        if (requestedBranchId != null) {
            if (user.getBranches() != null) {
                return user.getBranches().stream()
                        .filter(b -> b.getId().equals(requestedBranchId))
                        .findFirst()
                        .orElseThrow(() -> new BadCredentialsException("User does not have access to the selected branch"));
            } else {
                throw new BadCredentialsException("User is not assigned to any branches");
            }
        }

        // If no specific branch was requested, select the first assigned active branch
        if (user.getBranches() != null && !user.getBranches().isEmpty()) {
            return user.getBranches().iterator().next();
        }

        return null;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        String username = request.getUsername().trim().toLowerCase();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new ApiException("Username '" + username + "' is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ApiException("Email '" + email + "' is already registered");
        }

        // Generate clean alphanumeric tenant code from business name
        String cleanCode = request.getBusinessName().replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (cleanCode.length() > 6) {
            cleanCode = cleanCode.substring(0, 6);
        } else if (cleanCode.length() < 3) {
            cleanCode = (cleanCode + "POS").substring(0, Math.min(6, (cleanCode + "POS").length()));
        }

        String tenantCode = cleanCode;
        int counter = 1;
        while (tenantRepository.existsByCode(tenantCode)) {
            tenantCode = cleanCode + counter;
            counter++;
        }

        Tenant tenant = new Tenant(tenantCode, request.getBusinessName().trim(), "Self-registered Business Tenant");
        tenant = tenantRepository.save(tenant);

        BranchType bType = request.getBusinessType() != null ? request.getBusinessType() : BranchType.RETAIL;
        String bName = (request.getBranchName() != null && !request.getBranchName().trim().isEmpty())
                ? request.getBranchName().trim()
                : (request.getBusinessName().trim() + " - Main Branch");

        Branch branch = new Branch(
                tenant,
                tenantCode + "-MAIN",
                bName,
                request.getBranchAddress() != null ? request.getBranchAddress().trim() : "Main Office",
                request.getPhone(),
                bType
        );
        branch = branchRepository.save(branch);

        Role tenantAdminRole = roleRepository.findByName("ROLE_TENANT_ADMIN")
                .orElseGet(() -> {
                    Role newRole = new Role("ROLE_TENANT_ADMIN", "Business Owner & Tenant Admin");
                    return roleRepository.save(newRole);
                });

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTenant(tenant);
        user.getRoles().add(tenantAdminRole);
        user.getBranches().add(branch);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        auditLogService.log(tenant.getId(), branch.getId(), savedUser.getId(), savedUser.getUsername(),
                "TENANT_REGISTERED", "Organization registered: " + tenant.getName(), httpRequest);

        return createAuthSession(savedUser, branch, httpRequest, "REGISTRATION_LOGIN");
    }
}
