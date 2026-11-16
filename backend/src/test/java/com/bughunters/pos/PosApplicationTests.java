package com.bughunters.pos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies the application context loads without errors.
 * This test does NOT require a running database; use the "test" profile
 * with an H2 in-memory DB once schema bootstrap is in place, or exclude
 * via surefire until a Testcontainer MySQL is available in CI.
 */
@SpringBootTest
@ActiveProfiles("test")
class PosApplicationTests {

    @Test
    void contextLoads() {
        // If the context starts without throwing, this test passes.
    }
}
