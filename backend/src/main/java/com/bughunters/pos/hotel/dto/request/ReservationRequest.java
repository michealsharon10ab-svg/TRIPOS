package com.bughunters.pos.hotel.dto.request;

import com.bughunters.pos.hotel.domain.enums.BookingSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ReservationRequest {

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date must be today or in the future")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    private LocalDate checkOutDate;

    @NotNull(message = "Number of adults is required")
    @Min(value = 1, message = "At least one adult is required")
    private Integer adults;

    @NotNull(message = "Number of children is required")
    @Min(value = 0, message = "Children count cannot be negative")
    private Integer children;

    @NotNull(message = "Booking source is required")
    private BookingSource bookingSource;

    @Valid
    @NotNull(message = "Guest details are required")
    private GuestRequest guest;

    /** Room IDs to allocate to this reservation. At least one required. */
    @NotEmpty(message = "At least one room must be allocated")
    private List<Long> roomIds;
}
