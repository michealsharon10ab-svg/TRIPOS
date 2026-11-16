package com.bughunters.pos.hotel.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomTypeRequest {

    @NotBlank(message = "Type name is required")
    private String typeName;

    @NotNull(message = "Base rate is required")
    @DecimalMin(value = "0.01", message = "Base rate must be greater than zero")
    private BigDecimal baseRate;

    @NotNull(message = "Max occupancy is required")
    @Min(value = 1, message = "Max occupancy must be at least 1")
    private Integer maxOccupancy;
}
