package com.bughunters.pos.administration.dto;

import com.bughunters.pos.administration.entity.BranchType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name cannot exceed 150 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Business name is required")
    @Size(max = 150, message = "Business name cannot exceed 150 characters")
    private String businessName;

    private BranchType businessType = BranchType.RETAIL;

    private String phone;
    private String branchName;
    private String branchAddress;

    public RegisterRequest() {}

    public RegisterRequest(String fullName, String email, String username, String password,
                           String businessName, BranchType businessType, String phone,
                           String branchName, String branchAddress) {
        this.fullName = fullName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.businessName = businessName;
        this.businessType = businessType != null ? businessType : BranchType.RETAIL;
        this.phone = phone;
        this.branchName = branchName;
        this.branchAddress = branchAddress;
    }

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public BranchType getBusinessType() {
        return businessType;
    }

    public void setBusinessType(BranchType businessType) {
        this.businessType = businessType;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchAddress() {
        return branchAddress;
    }

    public void setBranchAddress(String branchAddress) {
        this.branchAddress = branchAddress;
    }
}
