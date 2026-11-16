package com.bughunters.pos.hotel.repository;

import com.bughunters.pos.hotel.domain.entity.RoomAllocation;
import com.bughunters.pos.hotel.domain.entity.RoomAllocationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomAllocationRepository extends JpaRepository<RoomAllocation, RoomAllocationId> {

    /** All allocations for a given reservation — used in cancellation logic. */
    List<RoomAllocation> findByReservationReservationId(Long reservationId);

    /**
     * Overlap check for a specific room (Business Rule 2).
     * Uses a half-open interval: existing.checkIn < :checkOut AND existing.checkOut > :checkIn.
     * Excludes CANCELLED reservations.
     */
    @Query("""
           SELECT ra FROM RoomAllocation ra
           WHERE ra.id.roomId = :roomId
             AND ra.reservation.resStatus <> 'CANCELLED'
             AND ra.reservation.checkInDate  < :checkOut
             AND ra.reservation.checkOutDate > :checkIn
           """)
    List<RoomAllocation> findOverlappingAllocations(
            @Param("roomId")   Long roomId,
            @Param("checkIn")  LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    /**
     * Delete all allocations for a reservation — called during cancellation
     * (Business Rule 5). Uses a bulk delete for performance.
     */
    @Modifying
    @Query("DELETE FROM RoomAllocation ra WHERE ra.id.reservationId = :reservationId")
    void deleteByReservationId(@Param("reservationId") Long reservationId);
}
