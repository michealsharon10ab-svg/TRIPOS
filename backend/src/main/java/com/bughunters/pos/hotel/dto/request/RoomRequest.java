package com.bughunters.pos.hotel.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomRequest {

    @NotBlank(message = "Room number is required")
    private String roomNo;

    @NotNull(message = "Floor number is required")
    @Min(value = 0, message = "Floor number must be zero or greater")
    private Integer floorNo;

    /** Free-text amenities list — optional. */
    private String amenities;

    @NotNull(message = "Room type ID is required")
    private Long roomTypeId;
}
