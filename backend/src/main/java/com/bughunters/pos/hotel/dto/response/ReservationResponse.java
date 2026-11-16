package com.bughunters.pos.hotel.dto.response;

import com.bughunters.pos.hotel.domain.entity.Reservation;
import com.bughunters.pos.hotel.domain.enums.BookingSource;
import com.bughunters.pos.hotel.domain.enums.ReservationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ReservationResponse {

    private Long              reservationId;
    private LocalDate         checkInDate;
    private LocalDate         checkOutDate;
    /** Derived — computed from checkInDate and checkOutDate. Not stored. */
    private long              noOfNights;
    private Integer           adults;
    private Integer           children;
    private ReservationStatus resStatus;
    private BookingSource     bookingSource;
    private GuestResponse     guest;
    private Long              branchId;
    private Long              version;
    private List<RoomAllocationResponse> allocations;

    public static ReservationResponse from(Reservation res,
                                           List<RoomAllocationResponse> allocations) {
        return ReservationResponse.builder()
                .reservationId(res.getReservationId())
                .checkInDate(res.getCheckInDate())
                .checkOutDate(res.getCheckOutDate())
                .noOfNights(res.getNoOfNights())
                .adults(res.getAdults())
                .children(res.getChildren())
                .resStatus(res.getResStatus())
                .bookingSource(res.getBookingSource())
                .guest(GuestResponse.from(res.getGuest()))
                .branchId(res.getBranchId())
                .version(res.getVersion())
                .allocations(allocations)
                .build();
    }
}
