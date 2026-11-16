package com.bughunters.pos.hotel.service;

import com.bughunters.pos.hotel.domain.enums.RoomStatus;
import com.bughunters.pos.hotel.dto.request.RoomRequest;
import com.bughunters.pos.hotel.dto.response.AvailabilityResponse;
import com.bughunters.pos.hotel.dto.response.RoomResponse;

import java.time.LocalDate;
import java.util.List;

public interface RoomService {

    List<RoomResponse> listRooms(Long branchId, RoomStatus status);

    RoomResponse createRoom(RoomRequest request, Long branchId);

    RoomResponse patchStatus(Long roomId, RoomStatus newStatus, Long branchId);

    AvailabilityResponse checkAvailability(LocalDate from, LocalDate to,
                                           Long roomTypeId, Long branchId);
}
