package com.bughunters.pos.hotel.service;

import com.bughunters.pos.hotel.dto.request.RoomTypeRequest;
import com.bughunters.pos.hotel.dto.response.RoomTypeResponse;

import java.util.List;

public interface RoomTypeService {

    List<RoomTypeResponse> listByBranch(Long branchId);

    RoomTypeResponse create(RoomTypeRequest request, Long branchId);
}
