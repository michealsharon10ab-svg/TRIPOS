package com.bughunters.pos.hotel.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Guest details embedded inside a reservation request.
 * If a guest with the same NIC/passport already exists, the service
 * will upsert (update existing) rather than create a duplicate.
 */
@Data
public class GuestRequest {

    @NotBlank(message = "NIC or Passport number is required")
    private String nicPassportNo;

    @NotBlank(message = "Guest name is required")
    private String name;

    private String nationality;

    private String phone;
}
