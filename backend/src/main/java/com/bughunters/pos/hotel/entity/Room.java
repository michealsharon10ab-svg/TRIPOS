package com.bughunters.pos.hotel.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "hotel_rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String roomNumber;
    private String status; // AVAILABLE, OCCUPIED, CLEANING, MAINTENANCE
}
