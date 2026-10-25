package com.bughunters.pos.restaurant.repository;

import com.bughunters.pos.restaurant.entity.KitchenOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KitchenOrderRepository extends JpaRepository<KitchenOrder, Long> {
}
