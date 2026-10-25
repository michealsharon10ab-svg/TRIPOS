package com.bughunters.pos.restaurant.repository;

import com.bughunters.pos.restaurant.entity.DiningTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {
}
