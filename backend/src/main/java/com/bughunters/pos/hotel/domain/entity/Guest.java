package com.bughunters.pos.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * A hotel guest identity record.
 *
 * <p>Kept separate from the Restaurant/CRM CUSTOMER entity (OQ-3 resolution).
 * {@code nicPassportNo} is globally unique across all branches — a guest is
 * identified by their ID document, not by branch.
 *
 * <p>If a guest books again, the same record is reused (upsert by
 * {@code nicPassportNo} in the service layer).
 */
@Entity
@Table(name = "guests",
       uniqueConstraints = @UniqueConstraint(
               name = "uq_guest_nic_passport",
               columnNames = {"nic_passport_no"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guest_id")
    private Long guestId;

    /** National ID or Passport number — must be unique across the system. */
    @Column(name = "nic_passport_no", nullable = false, length = 50)
    private String nicPassportNo;

    @Column(name = "nationality", length = 100)
    private String nationality;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;
}
