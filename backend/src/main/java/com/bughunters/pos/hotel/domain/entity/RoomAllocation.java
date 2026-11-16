package com.bughunters.pos.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Association class linking a {@link Reservation} to one or more {@link Room}s.
 *
 * <p>{@code nightlyRate} is a rate snapshot taken from
 * {@link RoomType#getBaseRate()} at the moment of booking (Business Rule 4).
 * Later changes to {@code RoomType.baseRate} do NOT affect this value.
 *
 * <p>An index on {@code (room_id, check_in_date, check_out_date)} (via the
 * reservation join) supports the overlap-check query efficiently.
 */
@Entity
@Table(name = "room_allocations",
       indexes = {
           @Index(name = "idx_alloc_room_dates",
                  columnList = "room_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAllocation {

    @EmbeddedId
    private RoomAllocationId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("reservationId")
    @JoinColumn(name = "reservation_id",
                foreignKey = @ForeignKey(name = "fk_alloc_reservation"))
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("roomId")
    @JoinColumn(name = "room_id",
                foreignKey = @ForeignKey(name = "fk_alloc_room"))
    private Room room;

    /**
     * Rate snapshot at booking time — DECIMAL(12,2).
     * Business Rule 4: immutable after creation.
     */
    @Column(name = "nightly_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal nightlyRate;
}
