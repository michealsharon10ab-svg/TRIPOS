package com.bughunters.pos.hotel.repository;

import com.bughunters.pos.hotel.domain.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    /** All room types for a given branch. */
    List<RoomType> findByBranchId(Long branchId);

    /** Existence check for duplicate name within the same branch. */
    boolean existsByTypeNameAndBranchId(String typeName, Long branchId);
}
