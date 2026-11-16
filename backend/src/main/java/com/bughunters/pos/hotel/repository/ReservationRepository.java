package com.bughunters.pos.hotel.repository;

import com.bughunters.pos.hotel.domain.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Find a reservation by ID, scoped to a branch (Business Rule 6).
     * Returns empty if the ID belongs to a different branch.
     */
    Optional<Reservation> findByReservationIdAndBranchId(Long reservationId, Long branchId);
}
