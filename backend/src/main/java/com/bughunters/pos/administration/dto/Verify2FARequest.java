package com.bughunters.pos.administration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class Verify2FARequest {

    @NotBlank(message = "Temporary token is required")
    private String tempToken;

    @NotBlank(message = "Verification code is required")
    @Pattern(regexp = "^\\d{6}$", message = "Code must be 6 digits")
    private String code;

    private Long branchId;

    public Verify2FARequest() {}

    public Verify2FARequest(String tempToken, String code) {
        this.tempToken = tempToken;
        this.code = code;
    }

    public String getTempToken() { return tempToken; }
    public void setTempToken(String tempToken) { this.tempToken = tempToken; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
}
