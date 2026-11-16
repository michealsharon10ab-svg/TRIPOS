package com.bughunters.pos.hotel.repository;

import com.bughunters.pos.hotel.domain.entity.Room;
import com.bughunters.pos.hotel.domain.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /** Rooms for a branch, optionally filtered by status. */
    List<Room> findByBranchId(Long branchId);

    List<Room> findByBranchIdAndRoomStatus(Long branchId, RoomStatus status);

    /**
     * Availability query (Business Rule 3):
     * Returns rooms of a given type whose IDs do NOT appear in any active
     * (non-CANCELLED) allocation that overlaps [checkIn, checkOut).
     *
     * <p>Overlap condition: existing.checkIn < :checkOut AND existing.checkOut > :checkIn
     */
    @Query("""
           SELECT r FROM Room r
           WHERE r.roomType.roomTypeId = :roomTypeId
             AND r.branchId           = :branchId
             AND r.roomStatus        <> 'MAINTENANCE'
             AND r.roomId NOT IN (
                   SELECT ra.id.roomId
                   FROM   RoomAllocation ra
                   WHERE  ra.reservation.resStatus <> 'CANCELLED'
                     AND  ra.reservation.checkInDate  < :checkOut
                     AND  ra.reservation.checkOutDate > :checkIn
             )
           """)
    List<Room> findAvailableRooms(
            @Param("roomTypeId") Long roomTypeId,
            @Param("branchId")   Long branchId,
            @Param("checkIn")    LocalDate checkIn,
            @Param("checkOut")   LocalDate checkOut);
}
