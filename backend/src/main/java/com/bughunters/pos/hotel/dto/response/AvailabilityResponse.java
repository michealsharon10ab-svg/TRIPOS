package com.bughunters.pos.hotel.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/** Response payload for GET /api/v1/hotel/availability */
@Data
@Builder
public class AvailabilityResponse {

    private Long            roomTypeId;
    private String          roomTypeName;
    private LocalDate       checkInDate;
    private LocalDate       checkOutDate;
    private long            noOfNights;
    private List<RoomResponse> availableRooms;
}
