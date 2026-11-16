package com.bughunters.pos.hotel.service;

import com.bughunters.pos.hotel.domain.entity.RoomType;
import com.bughunters.pos.hotel.dto.request.RoomTypeRequest;
import com.bughunters.pos.hotel.dto.response.RoomTypeResponse;
import com.bughunters.pos.hotel.exception.ResourceNotFoundException;
import com.bughunters.pos.hotel.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;

    @Override
    public List<RoomTypeResponse> listByBranch(Long branchId) {
        return roomTypeRepository.findByBranchId(branchId)
                .stream()
                .map(RoomTypeResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public RoomTypeResponse create(RoomTypeRequest request, Long branchId) {
        if (roomTypeRepository.existsByTypeNameAndBranchId(request.getTypeName(), branchId)) {
            throw new ResourceNotFoundException(
                "A room type named '" + request.getTypeName() + "' already exists in this branch.");
        }

        RoomType entity = RoomType.builder()
                .typeName(request.getTypeName())
                .baseRate(request.getBaseRate())
                .maxOccupancy(request.getMaxOccupancy())
                .branchId(branchId)
                .build();

        return RoomTypeResponse.from(roomTypeRepository.save(entity));
    }
}
