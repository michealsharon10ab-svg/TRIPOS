package com.bughunters.pos.config;

import com.bughunters.pos.administration.entity.Branch;
import com.bughunters.pos.administration.entity.Permission;
import com.bughunters.pos.administration.entity.Role;
import com.bughunters.pos.administration.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final String fullName;
    private final boolean active;
    private final Long tenantId;
    private final String tenantCode;
    private final String tenantName;
    private final Set<Long> branchIds;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String username, String email, String password, String fullName,
                         boolean active, Long tenantId, String tenantCode, String tenantName,
                         Set<Long> branchIds, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.active = active;
        this.tenantId = tenantId;
        this.tenantCode = tenantCode;
        this.tenantName = tenantName;
        this.branchIds = branchIds;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add Roles with ROLE_ prefix and Permissions directly
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
            if (role.getPermissions() != null) {
                for (Permission perm : role.getPermissions()) {
                    authorities.add(new SimpleGrantedAuthority(perm.getName()));
                }
            }
        }

        Set<Long> branchIds = user.getBranches() != null
                ? user.getBranches().stream().map(Branch::getId).collect(Collectors.toSet())
                : new HashSet<>();

        Long tenantId = user.getTenant() != null ? user.getTenant().getId() : null;
        String tenantCode = user.getTenant() != null ? user.getTenant().getCode() : null;
        String tenantName = user.getTenant() != null ? user.getTenant().getName() : null;

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getFullName(),
                user.isActive(),
                tenantId,
                tenantCode,
                tenantName,
                branchIds,
                authorities
        );
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public Long getTenantId() { return tenantId; }
    public String getTenantCode() { return tenantCode; }
    public String getTenantName() { return tenantName; }
    public Set<Long> getBranchIds() { return branchIds; }

    public boolean canAccessBranch(Long branchId) {
        if (isSuperAdmin()) return true;
        return branchId != null && branchIds.contains(branchId);
    }

    public boolean isSuperAdmin() {
        return authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return active; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return active; }
}
