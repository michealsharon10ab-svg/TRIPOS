package com.bughunters.pos.hotel.service;

import com.bughunters.pos.hotel.dto.request.ReservationRequest;
import com.bughunters.pos.hotel.dto.response.ReservationResponse;

public interface ReservationService {

    ReservationResponse createReservation(ReservationRequest request, Long branchId);

    ReservationResponse getReservation(Long reservationId, Long branchId);

    ReservationResponse cancelReservation(Long reservationId, Long branchId);
}
