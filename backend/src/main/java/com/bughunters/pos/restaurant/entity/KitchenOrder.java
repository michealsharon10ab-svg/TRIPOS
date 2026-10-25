package com.bughunters.pos.restaurant.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "kitchen_orders")
public class KitchenOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private String status; // PENDING, COOKING, READY, SERVED
    private LocalDateTime createdAt;
}
