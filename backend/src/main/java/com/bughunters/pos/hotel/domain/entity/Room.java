package com.bughunters.pos.hotel.domain.entity;

import com.bughunters.pos.hotel.domain.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * A physical hotel room.
 *
 * <p>Optimistic locking (ASS-8) is applied via {@code @Version} to prevent
 * two concurrent status-patch requests from clobbering each other.
 *
 * <p>{@code roomNo} is unique within a branch — enforced by both the database
 * unique index and the unique constraint annotation below.
 */
@Entity
@Table(name = "rooms",
       uniqueConstraints = @UniqueConstraint(
               name = "uq_room_no_branch",
               columnNames = {"room_no", "branch_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    /** Room number string, e.g. "101", "B-203". Unique per branch. */
    @Column(name = "room_no", nullable = false, length = 20)
    private String roomNo;

    @Column(name = "floor_no", nullable = false)
    private Integer floorNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_status", nullable = false, length = 20)
    private RoomStatus roomStatus;

    /**
     * Free-text list of amenities (e.g. "WiFi, Mini-bar, Ocean View").
     * Stored as TEXT to avoid join overhead; no need for a separate table
     * in this iteration.
     */
    @Column(name = "amenities", columnDefinition = "TEXT")
    private String amenities;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_type_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_room_room_type"))
    private RoomType roomType;

    /** ASS-6: plain Long FK — Branch entity is owned by another module. */
    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    /** Optimistic lock version — ASS-8. */
    @Version
    @Column(name = "version")
    private Long version;
}
