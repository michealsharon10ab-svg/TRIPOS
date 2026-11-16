package com.bughunters.pos.hotel.dto.request;

import com.bughunters.pos.hotel.domain.enums.RoomStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomStatusPatchRequest {

    @NotNull(message = "Status is required")
    private RoomStatus status;
}
