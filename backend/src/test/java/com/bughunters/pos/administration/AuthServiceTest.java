package com.bughunters.pos.administration;

import com.bughunters.pos.administration.dto.*;
import com.bughunters.pos.administration.entity.*;
import com.bughunters.pos.administration.repository.*;
import com.bughunters.pos.administration.service.AuditLogService;
import com.bughunters.pos.administration.service.AuthService;
import com.bughunters.pos.common.util.TotpUtil;
import com.bughunters.pos.config.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TerminalRepository terminalRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private RoleRepository roleRepository;

    // Use a real JwtTokenProvider instance with test credentials
    // Avoids Java 27 Mockito byte-buddy restrictions on mocking concrete classes
    private JwtTokenProvider tokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private Tenant sampleTenant;
    private Branch sampleBranch;
    private Role cashierRole;

    // Test JWT secret (64-char hex = 256 bits, satisfying HMAC-SHA256 requirement)
    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long ACCESS_EXPIRY_MS  = 3_600_000L;  // 1h
    private static final long REFRESH_EXPIRY_MS = 604_800_000L; // 7d

    @BeforeEach
    void setUp() {
        // Create a real JwtTokenProvider and inject it into AuthService
        tokenProvider = new JwtTokenProvider(TEST_SECRET, ACCESS_EXPIRY_MS, REFRESH_EXPIRY_MS, "bughunters-pos-test");
        ReflectionTestUtils.setField(authService, "tokenProvider", tokenProvider);

        ReflectionTestUtils.setField(authService, "maxPinFailedAttempts", 5);
        ReflectionTestUtils.setField(authService, "pinLockoutMinutes", 15);
        ReflectionTestUtils.setField(authService, "twoFactorIssuer", "TriPOS-BugHunters");

        sampleTenant = new Tenant("LLG", "Lanka Leisure Group", "Hospitality");
        sampleTenant.setId(1L);

        sampleBranch = new Branch(sampleTenant, "COLOMBO-CITY", "Colombo City Hotel", "Galle Rd", "123", BranchType.HOTEL);
        sampleBranch.setId(10L);

        cashierRole = new Role("ROLE_CASHIER", "Countertop Cashier");
        cashierRole.setId(4L);

        sampleUser = new User();
        sampleUser.setId(100L);
        sampleUser.setUsername("cashier01");
        sampleUser.setEmail("cashier01@lankaleisure.com");
        sampleUser.setPassword("$2a$12$hashedPassword");
        sampleUser.setFullName("Cashier One");
        sampleUser.setActive(true);
        sampleUser.setTenant(sampleTenant);
        sampleUser.setRoles(Set.of(cashierRole));
        sampleUser.setBranches(Set.of(sampleBranch));
    }

    @Test
    void testLogin_Success_ReturnsTokensAndUserProfile() {
        LoginRequest request = new LoginRequest("cashier01", "Password@123");
        request.setBranchId(10L);

        when(userRepository.findByUsernameOrEmailWithRolesAndPermissions("cashier01"))
                .thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Password@123", "$2a$12$hashedPassword")).thenReturn(true);

        TokenResponse response = authService.login(request, null);

        assertNotNull(response);
        assertFalse(response.isRequires2FA());
        assertNotNull(response.getAccessToken(), "Access token must not be null");
        assertNotNull(response.getRefreshToken(), "Refresh token must not be null");
        assertEquals("cashier01", response.getUser().getUsername());
        assertEquals("Colombo City Hotel", response.getActiveBranch().getName());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void testLogin_BadCredentials_ThrowsBadCredentialsException() {
        LoginRequest request = new LoginRequest("cashier01", "WrongPassword");

        when(userRepository.findByUsernameOrEmailWithRolesAndPermissions("cashier01"))
                .thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("WrongPassword", "$2a$12$hashedPassword")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request, null));
    }

    @Test
    void testLogin_NonExistentUser_ThrowsBadCredentialsException() {
        LoginRequest request = new LoginRequest("unknownUser", "Password@123");

        when(userRepository.findByUsernameOrEmailWithRolesAndPermissions("unknownUser"))
                .thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(request, null));
    }

    @Test
    void testLogin_DeactivatedAccount_ThrowsBadCredentialsException() {
        sampleUser.setActive(false);
        LoginRequest request = new LoginRequest("cashier01", "Password@123");

        when(userRepository.findByUsernameOrEmailWithRolesAndPermissions("cashier01"))
                .thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Password@123", "$2a$12$hashedPassword")).thenReturn(true);

        assertThrows(BadCredentialsException.class, () -> authService.login(request, null));
    }

    @Test
    void testLogin_TwoFactorEnabled_ReturnsRequires2FAWithTempToken() {
        sampleUser.setTwoFactorEnabled(true);
        sampleUser.setTwoFactorSecret(TotpUtil.generateSecretKey());

        LoginRequest request = new LoginRequest("cashier01", "Password@123");

        when(userRepository.findByUsernameOrEmailWithRolesAndPermissions("cashier01"))
                .thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Password@123", "$2a$12$hashedPassword")).thenReturn(true);

        TokenResponse response = authService.login(request, null);

        assertNotNull(response);
        assertTrue(response.isRequires2FA(), "Response must signal 2FA is required");
        assertNotNull(response.getTempToken(), "2FA temp token must be present");
        assertNull(response.getAccessToken(), "Access token must not be issued before 2FA");
    }

    @Test
    void testRefreshToken_Success_RotatesToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("raw-refresh-token");
        String tokenHash = JwtTokenProvider.hashToken("raw-refresh-token");

        RefreshToken existingToken = new RefreshToken(sampleUser, tokenHash, Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(existingToken));

        TokenResponse response = authService.refreshToken(request, null);

        assertNotNull(response);
        assertNotNull(response.getAccessToken(), "New access token must be generated");
        assertNotNull(response.getRefreshToken(), "New refresh token must be issued");
        assertTrue(existingToken.isRevoked(), "Old refresh token must be revoked (token rotation)");
        assertNotNull(existingToken.getReplacedByTokenHash(), "Replacement hash must be recorded");
    }

    @Test
    void testRefreshToken_ReuseRevokedToken_InvalidatesAllUserSessions() {
        RefreshTokenRequest request = new RefreshTokenRequest("revoked-token");
        String tokenHash = JwtTokenProvider.hashToken("revoked-token");

        RefreshToken revokedToken = new RefreshToken(sampleUser, tokenHash, Instant.now().plusSeconds(3600));
        revokedToken.setRevoked(true);

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(revokedToken));

        assertThrows(BadCredentialsException.class, () -> authService.refreshToken(request, null));
        verify(refreshTokenRepository, times(1)).revokeAllByUser(sampleUser);
    }

    @Test
    void testRegister_Success() {
        RegisterRequest req = new RegisterRequest(
                "John Owner",
                "john@retailmart.lk",
                "johnowner",
                "Password@123",
                "Retail Mart Group",
                BranchType.RETAIL,
                "+94 77 123 4567",
                "Pettah Main Branch",
                "Main Street, Colombo 11"
        );

        when(userRepository.existsByUsername("johnowner")).thenReturn(false);
        when(userRepository.existsByEmail("john@retailmart.lk")).thenReturn(false);
        when(tenantRepository.existsByCode(anyString())).thenReturn(false);

        Tenant createdTenant = new Tenant("RETAIL", "Retail Mart Group", "Self-registered Business Tenant");
        createdTenant.setId(10L);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(createdTenant);

        Branch createdBranch = new Branch(createdTenant, "RETAIL-MAIN", "Pettah Main Branch", "Main Street", "+94", BranchType.RETAIL);
        createdBranch.setId(20L);
        when(branchRepository.save(any(Branch.class))).thenReturn(createdBranch);

        Role tenantAdmin = new Role("ROLE_TENANT_ADMIN", "Business Owner & Tenant Admin");
        when(roleRepository.findByName("ROLE_TENANT_ADMIN")).thenReturn(Optional.of(tenantAdmin));
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword123");

        User savedUser = new User();
        savedUser.setId(30L);
        savedUser.setUsername("johnowner");
        savedUser.setEmail("john@retailmart.lk");
        savedUser.setFullName("John Owner");
        savedUser.setTenant(createdTenant);
        savedUser.getRoles().add(tenantAdmin);
        savedUser.getBranches().add(createdBranch);
        savedUser.setActive(true);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        TokenResponse res = authService.register(req, null);

        assertNotNull(res);
        assertNotNull(res.getAccessToken());
        assertNotNull(res.getRefreshToken());
        assertEquals("johnowner", res.getUser().getUsername());
        assertEquals("Retail Mart Group", res.getUser().getTenant().getName());
    }
}
