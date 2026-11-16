package com.bughunters.pos.hotel.repository;

import com.bughunters.pos.hotel.domain.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {

    /** Find by unique ID document — used for upsert in reservation flow. */
    Optional<Guest> findByNicPassportNo(String nicPassportNo);
}
