package com.bughunters.pos.config;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class TenantSecurityContext {

    public static UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) auth.getPrincipal();
        }
        return null;
    }

    public static Long getCurrentTenantId() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getTenantId() : null;
    }

    public static boolean isSuperAdmin() {
        UserPrincipal user = getCurrentUser();
        return user != null && user.isSuperAdmin();
    }

    public static void validateTenantAccess(Long requestedTenantId) {
        UserPrincipal user = getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("Unauthorized user context");
        }
        if (user.isSuperAdmin()) {
            return; // Super admins can cross tenant boundaries for management
        }
        if (requestedTenantId == null || !requestedTenantId.equals(user.getTenantId())) {
            throw new AccessDeniedException("Access denied: You cannot access or modify resources in another tenant");
        }
    }

    public static void validateBranchAccess(Long requestedBranchId) {
        UserPrincipal user = getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("Unauthorized user context");
        }
        if (user.isSuperAdmin()) {
            return;
        }
        if (requestedBranchId != null && !user.canAccessBranch(requestedBranchId)) {
            throw new AccessDeniedException("Access denied: You are not assigned to branch ID: " + requestedBranchId);
        }
    }
}
