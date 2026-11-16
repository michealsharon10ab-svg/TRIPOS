package com.bughunters.pos.hotel.domain.entity;

import com.bughunters.pos.hotel.domain.enums.BookingSource;
import com.bughunters.pos.hotel.domain.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * A hotel reservation.
 *
 * <p>{@code noOfNights} is intentionally NOT stored (Business Rule — derived
 * field). Use {@link #getNoOfNights()} to compute it at runtime.
 *
 * <p>Optimistic locking (ASS-8) prevents concurrent status updates from
 * silently overwriting each other.
 *
 * <p>branchId (ASS-6) scopes every query — a user must never see another
 * branch's reservations (Business Rule 6).
 */
@Entity
@Table(name = "reservations",
       indexes = {
           @Index(name = "idx_res_branch_status",
                  columnList = "branch_id, res_status"),
           @Index(name = "idx_res_guest",
                  columnList = "guest_id"),
           @Index(name = "idx_res_dates",
                  columnList = "check_in_date, check_out_date")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "adults", nullable = false)
    private Integer adults;

    @Column(name = "children", nullable = false)
    private Integer children;

    @Enumerated(EnumType.STRING)
    @Column(name = "res_status", nullable = false, length = 20)
    private ReservationStatus resStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_source", nullable = false, length = 20)
    private BookingSource bookingSource;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_reservation_guest"))
    private Guest guest;

    /** ASS-6: plain Long FK — Branch entity owned by another module. */
    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    /** Optimistic lock version — ASS-8. */
    @Version
    @Column(name = "version")
    private Long version;

    // ── Derived field (Business Rule — NOT stored) ────────────────────────

    /**
     * Computes the number of nights between check-in and check-out.
     * ASS-11: never persisted.
     *
     * @return number of nights; 0 if dates are equal (shouldn't happen if
     *         validation passed)
     */
    @Transient
    public long getNoOfNights() {
        if (checkInDate == null || checkOutDate == null) return 0;
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }
}
