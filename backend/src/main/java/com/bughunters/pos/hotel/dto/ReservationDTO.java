package com.bughunters.pos.hotel.dto;

import java.time.LocalDate;

public class ReservationDTO {
    private Long id;
    private Long roomId;
    private String guestName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}
