package com.bughunters.pos.administration.dto;

import com.bughunters.pos.administration.entity.AuditLog;

import java.time.LocalDateTime;

public class AuditLogDTO {
    private Long id;
    private Long tenantId;
    private Long branchId;
    private Long userId;
    private String username;
    private String action;
    private String details;
    private String ipAddress;
    private LocalDateTime timestamp;

    public AuditLogDTO() {}

    public AuditLogDTO(AuditLog log) {
        if (log != null) {
            this.id = log.getId();
            this.tenantId = log.getTenantId();
            this.branchId = log.getBranchId();
            this.userId = log.getUserId();
            this.username = log.getUsername();
            this.action = log.getAction();
            this.details = log.getDetails();
            this.ipAddress = log.getIpAddress();
            this.timestamp = log.getTimestamp();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
