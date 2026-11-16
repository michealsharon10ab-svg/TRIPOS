package com.bughunters.pos.hotel.dto.response;

import com.bughunters.pos.hotel.domain.entity.RoomType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RoomTypeResponse {

    private Long       roomTypeId;
    private String     typeName;
    private BigDecimal baseRate;
    private Integer    maxOccupancy;
    private Long       branchId;

    public static RoomTypeResponse from(RoomType rt) {
        return RoomTypeResponse.builder()
                .roomTypeId(rt.getRoomTypeId())
                .typeName(rt.getTypeName())
                .baseRate(rt.getBaseRate())
                .maxOccupancy(rt.getMaxOccupancy())
                .branchId(rt.getBranchId())
                .build();
    }
}
