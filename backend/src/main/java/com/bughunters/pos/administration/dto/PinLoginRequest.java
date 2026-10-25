package com.bughunters.pos.administration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class PinLoginRequest {

    @NotBlank(message = "Terminal code is required")
    private String terminalCode;

    @NotBlank(message = "Tenant code is required")
    private String tenantCode;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotBlank(message = "PIN is required")
    @Pattern(regexp = "^\\d{4,6}$", message = "PIN must be between 4 and 6 digits")
    private String pin;

    public PinLoginRequest() {}

    public PinLoginRequest(String tenantCode, Long branchId, String terminalCode, String pin) {
        this.tenantCode = tenantCode;
        this.branchId = branchId;
        this.terminalCode = terminalCode;
        this.pin = pin;
    }

    public String getTerminalCode() { return terminalCode; }
    public void setTerminalCode(String terminalCode) { this.terminalCode = terminalCode; }

    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(String tenantCode) { this.tenantCode = tenantCode; }

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
}
