package com.bughunters.pos.administration.dto;

import com.bughunters.pos.administration.entity.Permission;
import com.bughunters.pos.administration.entity.Role;
import com.bughunters.pos.administration.entity.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private boolean active;
    private boolean twoFactorEnabled;
    private boolean pinConfigured;
    private TenantSummaryDTO tenant;
    private Set<String> roles = new HashSet<>();
    private Set<String> permissions = new HashSet<>();
    private Set<BranchSummaryDTO> branches = new HashSet<>();
    private LocalDateTime createdAt;

    public UserResponseDTO() {}

    public static UserResponseDTO fromEntity(User user) {
        if (user == null) return null;

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setActive(user.isActive());
        dto.setTwoFactorEnabled(user.isTwoFactorEnabled());
        dto.setPinConfigured(user.getPin() != null && !user.getPin().isBlank());
        dto.setCreatedAt(user.getCreatedAt());

        if (user.getTenant() != null) {
            dto.setTenant(new TenantSummaryDTO(user.getTenant()));
        }

        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
            Set<String> perms = new HashSet<>();
            for (Role role : user.getRoles()) {
                if (role.getPermissions() != null) {
                    for (Permission perm : role.getPermissions()) {
                        perms.add(perm.getName());
                    }
                }
            }
            dto.setPermissions(perms);
        }

        if (user.getBranches() != null) {
            dto.setBranches(user.getBranches().stream().map(BranchSummaryDTO::new).collect(Collectors.toSet()));
        }

        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }

    public boolean isPinConfigured() { return pinConfigured; }
    public void setPinConfigured(boolean pinConfigured) { this.pinConfigured = pinConfigured; }

    public TenantSummaryDTO getTenant() { return tenant; }
    public void setTenant(TenantSummaryDTO tenant) { this.tenant = tenant; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> permissions) { this.permissions = permissions; }

    public Set<BranchSummaryDTO> getBranches() { return branches; }
    public void setBranches(Set<BranchSummaryDTO> branches) { this.branches = branches; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
