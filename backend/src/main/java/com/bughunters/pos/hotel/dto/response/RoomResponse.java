package com.bughunters.pos.hotel.dto.response;

import com.bughunters.pos.hotel.domain.entity.Room;
import com.bughunters.pos.hotel.domain.enums.RoomStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomResponse {

    private Long           roomId;
    private String         roomNo;
    private Integer        floorNo;
    private RoomStatus     roomStatus;
    private String         amenities;
    private RoomTypeResponse roomType;
    private Long           branchId;
    private Long           version;

    public static RoomResponse from(Room room) {
        return RoomResponse.builder()
                .roomId(room.getRoomId())
                .roomNo(room.getRoomNo())
                .floorNo(room.getFloorNo())
                .roomStatus(room.getRoomStatus())
                .amenities(room.getAmenities())
                .roomType(RoomTypeResponse.from(room.getRoomType()))
                .branchId(room.getBranchId())
                .version(room.getVersion())
                .build();
    }
}
