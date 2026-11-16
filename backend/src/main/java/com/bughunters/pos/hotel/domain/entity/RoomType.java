package com.bughunters.pos.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a category of hotel rooms (e.g. "Deluxe Twin", "Suite").
 *
 * <p>Each {@link RoomType} is scoped to a branch (ASS-5/6: branchId is a plain
 * Long because the Branch entity is owned by another module).
 *
 * <p>The {@code baseRate} is a snapshot reference; changes here do NOT affect
 * existing {@link RoomAllocation} nightly-rate snapshots (Business Rule 4).
 */
@Entity
@Table(name = "room_types",
       uniqueConstraints = @UniqueConstraint(
               name = "uq_room_type_name_branch",
               columnNames = {"type_name", "branch_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_type_id")
    private Long roomTypeId;

    @Column(name = "type_name", nullable = false, length = 100)
    private String typeName;

    /**
     * Base nightly rate. Stored as DECIMAL(12,2).
     * Business Rule 4: copying this to {@link RoomAllocation#getNightlyRate()} at
     * booking time means later edits here do not affect past allocations.
     */
    @Column(name = "base_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseRate;

    @Column(name = "max_occupancy", nullable = false)
    private Integer maxOccupancy;

    /**
     * ASS-6: plain Long, not a @ManyToOne FK. The Branch entity belongs to
     * another module; hotel only stores the ID.
     */
    @Column(name = "branch_id", nullable = false)
    private Long branchId;
}
