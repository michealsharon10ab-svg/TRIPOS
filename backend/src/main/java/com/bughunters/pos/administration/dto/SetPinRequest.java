package com.bughunters.pos.administration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SetPinRequest {

    @NotBlank(message = "PIN is required")
    @Pattern(regexp = "^\\d{4,6}$", message = "PIN must be between 4 and 6 digits")
    private String pin;

    @NotBlank(message = "PIN confirmation is required")
    private String confirmPin;

    public SetPinRequest() {}

    public SetPinRequest(String pin, String confirmPin) {
        this.pin = pin;
        this.confirmPin = confirmPin;
    }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public String getConfirmPin() { return confirmPin; }
    public void setConfirmPin(String confirmPin) { this.confirmPin = confirmPin; }
}
