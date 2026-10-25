package com.bughunters.pos.hotel.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "hotel_reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long roomId;
    private String guestName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}
