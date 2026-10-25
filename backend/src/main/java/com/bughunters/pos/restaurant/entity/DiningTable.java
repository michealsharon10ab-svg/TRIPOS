package com.bughunters.pos.restaurant.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "dining_tables")
public class DiningTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tableNumber;
    private Integer capacity;
    private String status; // VACANT, OCCUPIED, RESERVED
}
