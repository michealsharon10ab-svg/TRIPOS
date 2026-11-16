package com.bughunters.pos.hotel.service;

import com.bughunters.pos.hotel.domain.entity.Room;
import com.bughunters.pos.hotel.domain.entity.RoomType;
import com.bughunters.pos.hotel.domain.enums.RoomStatus;
import com.bughunters.pos.hotel.dto.request.RoomRequest;
import com.bughunters.pos.hotel.dto.response.AvailabilityResponse;
import com.bughunters.pos.hotel.dto.response.RoomResponse;
import com.bughunters.pos.hotel.exception.BranchAccessDeniedException;
import com.bughunters.pos.hotel.exception.InvalidDateRangeException;
import com.bughunters.pos.hotel.exception.ResourceNotFoundException;
import com.bughunters.pos.hotel.repository.RoomRepository;
import com.bughunters.pos.hotel.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

    private final RoomRepository     roomRepository;
    private final RoomTypeRepository roomTypeRepository;

    @Override
    public List<RoomResponse> listRooms(Long branchId, RoomStatus status) {
        List<Room> rooms = (status == null)
                ? roomRepository.findByBranchId(branchId)
                : roomRepository.findByBranchIdAndRoomStatus(branchId, status);
        return rooms.stream().map(RoomResponse::from).toList();
    }

    @Override
    @Transactional
    public RoomResponse createRoom(RoomRequest request, Long branchId) {
        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("RoomType", request.getRoomTypeId()));

        // Ensure the room type belongs to this branch (Business Rule 6)
        if (!roomType.getBranchId().equals(branchId)) {
            throw new BranchAccessDeniedException("RoomType", roomType.getRoomTypeId());
        }

        Room room = Room.builder()
                .roomNo(request.getRoomNo())
                .floorNo(request.getFloorNo())
                .roomStatus(RoomStatus.AVAILABLE)
                .amenities(request.getAmenities())
                .roomType(roomType)
                .branchId(branchId)
                .build();

        return RoomResponse.from(roomRepository.save(room));
    }

    @Override
    @Transactional
    public RoomResponse patchStatus(Long roomId, RoomStatus newStatus, Long branchId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        // Business Rule 6 — branch isolation
        if (!room.getBranchId().equals(branchId)) {
            throw new BranchAccessDeniedException("Room", roomId);
        }

        room.setRoomStatus(newStatus);
        return RoomResponse.from(roomRepository.save(room));
    }

    @Override
    public AvailabilityResponse checkAvailability(LocalDate from, LocalDate to,
                                                   Long roomTypeId, Long branchId) {
        // Business Rule 1 — dates
        if (!to.isAfter(from)) {
            throw new InvalidDateRangeException("Check-out date must be strictly after check-in date.");
        }

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("RoomType", roomTypeId));

        // Business Rule 6 — scope to caller's branch
        if (!roomType.getBranchId().equals(branchId)) {
            throw new BranchAccessDeniedException("RoomType", roomTypeId);
        }

        List<Room> available = roomRepository.findAvailableRooms(roomTypeId, branchId, from, to);

        return AvailabilityResponse.builder()
                .roomTypeId(roomTypeId)
                .roomTypeName(roomType.getTypeName())
                .checkInDate(from)
                .checkOutDate(to)
                .noOfNights(ChronoUnit.DAYS.between(from, to))
                .availableRooms(available.stream().map(RoomResponse::from).toList())
                .build();
    }
}
