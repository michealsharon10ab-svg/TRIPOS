package com.bughunters.pos.hotel.dto.response;

import com.bughunters.pos.hotel.domain.entity.RoomAllocation;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RoomAllocationResponse {

    private Long       roomId;
    private String     roomNo;
    private BigDecimal nightlyRate;

    public static RoomAllocationResponse from(RoomAllocation ra) {
        return RoomAllocationResponse.builder()
                .roomId(ra.getRoom().getRoomId())
                .roomNo(ra.getRoom().getRoomNo())
                .nightlyRate(ra.getNightlyRate())
                .build();
    }
}
