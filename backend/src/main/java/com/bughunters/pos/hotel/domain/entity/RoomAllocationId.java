package com.bughunters.pos.hotel.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for {@link RoomAllocation}.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomAllocationId implements Serializable {

    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "room_id")
    private Long roomId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomAllocationId that)) return false;
        return Objects.equals(reservationId, that.reservationId)
            && Objects.equals(roomId, that.roomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId, roomId);
    }
}
