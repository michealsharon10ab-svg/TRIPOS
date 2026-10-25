package com.bughunters.pos.administration.service;

import com.bughunters.pos.administration.dto.*;
import com.bughunters.pos.administration.entity.Branch;
import com.bughunters.pos.administration.entity.Role;
import com.bughunters.pos.administration.entity.Tenant;
import com.bughunters.pos.administration.entity.User;
import com.bughunters.pos.administration.repository.BranchRepository;
import com.bughunters.pos.administration.repository.RefreshTokenRepository;
import com.bughunters.pos.administration.repository.RoleRepository;
import com.bughunters.pos.administration.repository.TenantRepository;
import com.bughunters.pos.administration.repository.UserRepository;
import com.bughunters.pos.common.exception.ApiException;
import com.bughunters.pos.common.exception.ResourceNotFoundException;
import com.bughunters.pos.config.TenantSecurityContext;
import com.bughunters.pos.config.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final TenantRepository tenantRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            BranchRepository branchRepository,
            TenantRepository tenantRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.branchRepository = branchRepository;
        this.tenantRepository = tenantRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> listUsers(String query, Pageable pageable, UserPrincipal currentUser) {
        if (currentUser.isSuperAdmin() && currentUser.getTenantId() == null) {
            if (query != null && !query.isBlank()) {
                return userRepository.searchByTenantId(1L, query, pageable).map(UserResponseDTO::fromEntity);
            }
            return userRepository.findAll(pageable).map(UserResponseDTO::fromEntity);
        }

        Long tenantId = currentUser.getTenantId();
        if (query != null && !query.isBlank()) {
            return userRepository.searchByTenantId(tenantId, query, pageable).map(UserResponseDTO::fromEntity);
        }
        return userRepository.findByTenantId(tenantId, pageable).map(UserResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long userId, UserPrincipal currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!currentUser.isSuperAdmin() && (user.getTenant() == null || !user.getTenant().getId().equals(currentUser.getTenantId()))) {
            throw new AccessDeniedException("Access denied: You cannot view users outside your tenant organization");
        }

        return UserResponseDTO.fromEntity(user);
    }

    @Transactional
    public UserResponseDTO createUser(CreateUserRequest request, UserPrincipal currentUser, HttpServletRequest httpRequest) {
        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new ApiException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new ApiException("Email is already registered");
        }

        // Determine tenant context
        Tenant tenant;
        if (currentUser.isSuperAdmin() && request.getTenantId() != null) {
            tenant = tenantRepository.findById(request.getTenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with ID: " + request.getTenantId()));
        } else {
            if (currentUser.getTenantId() == null) {
                throw new ApiException("Tenant context is required to create a user");
            }
            tenant = tenantRepository.findById(currentUser.getTenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Current user tenant not found"));
        }

        // Validate Roles & Prevent Privilege Escalation
        Set<Role> roles = new HashSet<>();
        for (Long roleId : request.getRoleIds()) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

            if ("ROLE_SUPER_ADMIN".equals(role.getName()) && !currentUser.isSuperAdmin()) {
                throw new AccessDeniedException("Privilege escalation denied: Only Super Administrators can assign the SUPER_ADMIN role");
            }
            roles.add(role);
        }

        // Validate Branches (Ensure assigned branches belong to the user's tenant)
        Set<Branch> branches = new HashSet<>();
        if (request.getBranchIds() != null && !request.getBranchIds().isEmpty()) {
            for (Long branchId : request.getBranchIds()) {
                Branch branch = branchRepository.findByIdAndTenantId(branchId, tenant.getId())
                        .orElseThrow(() -> new ApiException("Branch ID " + branchId + " does not belong to tenant " + tenant.getName()));
                branches.add(branch);
            }
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTenant(tenant);
        user.setRoles(roles);
        user.setBranches(branches);
        user.setActive(true);

        if (request.getPin() != null && !request.getPin().isBlank()) {
            user.setPin(passwordEncoder.encode(request.getPin().trim()));
        }

        User savedUser = userRepository.save(user);

        auditLogService.log(tenant.getId(), null, savedUser.getId(), savedUser.getUsername(),
                "USER_CREATED", "Staff account created by " + currentUser.getUsername(), httpRequest);

        return UserResponseDTO.fromEntity(savedUser);
    }

    @Transactional
    public UserResponseDTO updateUser(Long userId, UpdateUserRequest request, UserPrincipal currentUser, HttpServletRequest httpRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        TenantSecurityContext.validateTenantAccess(user.getTenant() != null ? user.getTenant().getId() : null);

        // Check if updating email to another existing user
        if (!user.getEmail().equalsIgnoreCase(request.getEmail().trim()) &&
                userRepository.existsByEmail(request.getEmail().trim())) {
            throw new ApiException("Email is already used by another account");
        }

        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPhone(request.getPhone());

        // Validate Roles & Privilege Escalation
        Set<Role> roles = new HashSet<>();
        for (Long roleId : request.getRoleIds()) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

            if ("ROLE_SUPER_ADMIN".equals(role.getName()) && !currentUser.isSuperAdmin()) {
                throw new AccessDeniedException("Privilege escalation denied: Only Super Administrators can assign the SUPER_ADMIN role");
            }
            roles.add(role);
        }
        user.setRoles(roles);

        // Validate Branches
        if (request.getBranchIds() != null && user.getTenant() != null) {
            Set<Branch> branches = new HashSet<>();
            for (Long branchId : request.getBranchIds()) {
                Branch branch = branchRepository.findByIdAndTenantId(branchId, user.getTenant().getId())
                        .orElseThrow(() -> new ApiException("Branch ID " + branchId + " does not belong to tenant"));
                branches.add(branch);
            }
            user.setBranches(branches);
        }

        User updatedUser = userRepository.save(user);

        auditLogService.log(user.getTenant() != null ? user.getTenant().getId() : null,
                null, updatedUser.getId(), updatedUser.getUsername(),
                "USER_UPDATED", "Staff profile updated by " + currentUser.getUsername(), httpRequest);

        return UserResponseDTO.fromEntity(updatedUser);
    }

    @Transactional
    public void toggleUserStatus(Long userId, boolean active, UserPrincipal currentUser, HttpServletRequest httpRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        TenantSecurityContext.validateTenantAccess(user.getTenant() != null ? user.getTenant().getId() : null);

        if (user.getId().equals(currentUser.getId())) {
            throw new ApiException("You cannot deactivate your own account");
        }

        user.setActive(active);
        userRepository.save(user);

        if (!active) {
            // Revoke all refresh tokens on deactivation
            refreshTokenRepository.revokeAllByUser(user);
        }

        auditLogService.log(user.getTenant() != null ? user.getTenant().getId() : null,
                null, user.getId(), user.getUsername(),
                "USER_STATUS_CHANGE", "Account status changed to: " + (active ? "ACTIVE" : "DEACTIVATED"), httpRequest);
    }

    @Transactional
    public void resetPassword(Long userId, ResetPasswordRequest request, UserPrincipal currentUser, HttpServletRequest httpRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        TenantSecurityContext.validateTenantAccess(user.getTenant() != null ? user.getTenant().getId() : null);

        boolean isTargetSuperAdmin = user.getRoles().stream().anyMatch(r -> "ROLE_SUPER_ADMIN".equals(r.getName()));
        if (isTargetSuperAdmin && !currentUser.isSuperAdmin()) {
            throw new AccessDeniedException("Only a Super Administrator can reset another Super Administrator's password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke active sessions for security
        refreshTokenRepository.revokeAllByUser(user);

        auditLogService.log(user.getTenant() != null ? user.getTenant().getId() : null,
                null, user.getId(), user.getUsername(),
                "ADMIN_PASSWORD_RESET", "Password was reset by administrator " + currentUser.getUsername(), httpRequest);
    }

    @Transactional
    public void setUserPin(Long userId, SetPinRequest request, UserPrincipal currentUser, HttpServletRequest httpRequest) {
        if (!request.getPin().equals(request.getConfirmPin())) {
            throw new ApiException("PIN and confirm PIN do not match");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        TenantSecurityContext.validateTenantAccess(user.getTenant() != null ? user.getTenant().getId() : null);

        user.setPin(passwordEncoder.encode(request.getPin().trim()));
        user.setPinFailedAttempts(0);
        user.setPinLockedUntil(null);
        userRepository.save(user);

        auditLogService.log(user.getTenant() != null ? user.getTenant().getId() : null,
                null, user.getId(), user.getUsername(),
                "PIN_CONFIGURED", "Terminal PIN configured for user by " + currentUser.getUsername(), httpRequest);
    }
}
