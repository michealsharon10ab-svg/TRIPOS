package com.bughunters.pos.hotel.service;

import com.bughunters.pos.hotel.domain.entity.*;
import com.bughunters.pos.hotel.domain.enums.ReservationStatus;
import com.bughunters.pos.hotel.domain.enums.RoomStatus;
import com.bughunters.pos.hotel.dto.request.GuestRequest;
import com.bughunters.pos.hotel.dto.request.ReservationRequest;
import com.bughunters.pos.hotel.dto.response.ReservationResponse;
import com.bughunters.pos.hotel.dto.response.RoomAllocationResponse;
import com.bughunters.pos.hotel.exception.BranchAccessDeniedException;
import com.bughunters.pos.hotel.exception.InvalidDateRangeException;
import com.bughunters.pos.hotel.exception.ResourceNotFoundException;
import com.bughunters.pos.hotel.exception.RoomNotAvailableException;
import com.bughunters.pos.hotel.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository    reservationRepository;
    private final RoomRepository           roomRepository;
    private final GuestRepository          guestRepository;
    private final RoomAllocationRepository allocationRepository;

    // ── Create ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, Long branchId) {

        // Business Rule 1 — date validation
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new InvalidDateRangeException(
                    "Check-out date must be strictly after check-in date.");
        }

        // Upsert guest (find by NIC/passport, or create)
        Guest guest = resolveGuest(request.getGuest());

        // Build the reservation (status starts at CONFIRMED)
        Reservation reservation = Reservation.builder()
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .adults(request.getAdults())
                .children(request.getChildren())
                .resStatus(ReservationStatus.CONFIRMED)
                .bookingSource(request.getBookingSource())
                .guest(guest)
                .branchId(branchId)
                .build();

        reservation = reservationRepository.save(reservation);

        // Allocate each requested room
        List<RoomAllocation> allocations = new ArrayList<>();
        for (Long roomId : request.getRoomIds()) {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

            // Business Rule 6 — branch isolation
            if (!room.getBranchId().equals(branchId)) {
                throw new BranchAccessDeniedException("Room", roomId);
            }

            // Business Rule 2 — overlap check
            List<RoomAllocation> conflicts = allocationRepository.findOverlappingAllocations(
                    roomId,
                    request.getCheckInDate(),
                    request.getCheckOutDate());

            if (!conflicts.isEmpty()) {
                throw new RoomNotAvailableException(
                        roomId,
                        request.getCheckInDate().toString(),
                        request.getCheckOutDate().toString());
            }

            // Business Rule 4 — snapshot the rate at booking time
            RoomAllocationId allocationId = new RoomAllocationId(
                    reservation.getReservationId(), roomId);

            RoomAllocation allocation = RoomAllocation.builder()
                    .id(allocationId)
                    .reservation(reservation)
                    .room(room)
                    .nightlyRate(room.getRoomType().getBaseRate()) // snapshot
                    .build();

            allocations.add(allocationRepository.save(allocation));

            // Update room status to BOOKED
            room.setRoomStatus(RoomStatus.BOOKED);
            roomRepository.save(room);
        }

        List<RoomAllocationResponse> allocationResponses = allocations.stream()
                .map(RoomAllocationResponse::from)
                .toList();

        return ReservationResponse.from(reservation, allocationResponses);
    }

    // ── Get ───────────────────────────────────────────────────────────────

    @Override
    public ReservationResponse getReservation(Long reservationId, Long branchId) {
        Reservation reservation = reservationRepository
                .findByReservationIdAndBranchId(reservationId, branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));

        List<RoomAllocationResponse> allocationResponses =
                allocationRepository.findByReservationReservationId(reservationId)
                        .stream()
                        .map(RoomAllocationResponse::from)
                        .toList();

        return ReservationResponse.from(reservation, allocationResponses);
    }

    // ── Cancel ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ReservationResponse cancelReservation(Long reservationId, Long branchId) {
        Reservation reservation = reservationRepository
                .findByReservationIdAndBranchId(reservationId, branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));

        if (reservation.getResStatus() == ReservationStatus.CANCELLED) {
            throw new InvalidDateRangeException("Reservation is already cancelled.");
        }
        if (reservation.getResStatus() == ReservationStatus.CHECKED_OUT) {
            throw new InvalidDateRangeException("Cannot cancel a checked-out reservation.");
        }

        // Business Rule 5 — cancellation releases allocations
        List<RoomAllocation> allocations =
                allocationRepository.findByReservationReservationId(reservationId);

        // Release rooms back to AVAILABLE
        for (RoomAllocation alloc : allocations) {
            Room room = alloc.getRoom();
            room.setRoomStatus(RoomStatus.AVAILABLE);
            roomRepository.save(room);
        }

        // Delete all allocations (Business Rule 5)
        allocationRepository.deleteByReservationId(reservationId);

        reservation.setResStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        return ReservationResponse.from(reservation, List.of());
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /**
     * Finds an existing guest by NIC/passport, or creates a new record.
     * Prevents duplicate guest rows for returning guests.
     */
    private Guest resolveGuest(GuestRequest req) {
        return guestRepository.findByNicPassportNo(req.getNicPassportNo())
                .map(existing -> {
                    // Update contact details in case they changed
                    existing.setName(req.getName());
                    existing.setNationality(req.getNationality());
                    existing.setPhone(req.getPhone());
                    return guestRepository.save(existing);
                })
                .orElseGet(() -> guestRepository.save(
                        Guest.builder()
                             .nicPassportNo(req.getNicPassportNo())
                             .name(req.getName())
                             .nationality(req.getNationality())
                             .phone(req.getPhone())
                             .build()));
    }
}
