package com.bughunters.pos.hotel.dto.response;

import com.bughunters.pos.hotel.domain.entity.Guest;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GuestResponse {

    private Long   guestId;
    private String nicPassportNo;
    private String name;
    private String nationality;
    private String phone;

    public static GuestResponse from(Guest guest) {
        return GuestResponse.builder()
                .guestId(guest.getGuestId())
                .nicPassportNo(guest.getNicPassportNo())
                .name(guest.getName())
                .nationality(guest.getNationality())
                .phone(guest.getPhone())
                .build();
    }
}
