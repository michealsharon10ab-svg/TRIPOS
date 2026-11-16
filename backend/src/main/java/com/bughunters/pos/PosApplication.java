package com.bughunters.pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Bug Hunters Unified POS backend.
 *
 * <p>Package layout:
 * <ul>
 *   <li>{@code com.bughunters.pos.security}  – Auth / RBAC (Member 1: Sharon) [placeholder present]</li>
 *   <li>{@code com.bughunters.pos.hotel}      – Hotel & Reservations (Member 5: Kajanika)</li>
 *   <li>{@code com.bughunters.pos.common}     – Shared DTOs, exception handling</li>
 * </ul>
 */
@SpringBootApplication
public class PosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PosApplication.class, args);
    }
}
