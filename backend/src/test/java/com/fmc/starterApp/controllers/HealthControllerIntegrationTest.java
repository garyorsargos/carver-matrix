package com.fmc.starterApp.controllers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link com.fmc.starterApp.controllers.Health}, verifying that the controller layer:
 * <ul>
 *   <li>Exposes GET <code>/api/health</code> publicly.</li>
 *   <li>Returns HTTP 200 OK with the exact body <code>"app is healthy"</code>.</li>
 *   <li>Permits access without authentication, and accepts a valid JWT without error.</li>
 *   <li>Returns HTTP 405 for unsupported methods (e.g. POST).</li>
 *   <li>Returns HTTP 404 for unknown subpaths under <code>/api/health</code>.</li>
 * </ul>
 *
 * <p>This test class uses MockMvc (with Spring Security filters), an in‑memory H2 database configured via
 * <code>application-test.properties</code>.</p>
 */
@Disabled("Excluded due to Spring Boot dep conflict. Omitting spring-webmvc from dependency mangement allows testing but breaks production")
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class HealthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // =========================================================================
    // ✅ 1. Basic Endpoint Tests (Web Test)
    // =========================================================================

    /**
     * **healthcheck - No Authentication Test**
     * Verify that GET /api/health without any token returns 200 OK and the correct body.
     */
    @Test
    void healthcheck_noAuth_returnsOkAndMessage() throws Exception {
        mockMvc.perform(get("/api/health"))
               .andExpect(status().isOk())
               .andExpect(content().string("app is healthy"));
    }

    // =========================================================================
    // ✅ 2. Input Validation and Error Handling Tests
    // =========================================================================

    /**
     * **healthcheck - Unsupported Method Test**
     * Verify that POST /api/health returns 405 Method Not Allowed.
     */
    @Test
    void healthcheck_postMethodNotAllowed_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/api/health"))
               .andExpect(status().isMethodNotAllowed());
    }

    // =========================================================================
    // ✅ 3. Security and Authorization Tests
    // =========================================================================

    /**
     * **healthcheck - With Valid JWT Test**
     * Verify that GET /api/health with a valid JWT still returns 200 OK and the correct body.
     */
    @Test
    void healthcheck_withJwt_returnsOkAndMessage() throws Exception {
        mockMvc.perform(get("/api/health").with(jwt()))
               .andExpect(status().isOk())
               .andExpect(content().string("app is healthy"));
    }

    // =========================================================================
    // ✅ 4. Integration and End‑to‑End Tests
    // =========================================================================

    // No integration tests beyond the basic GET cycle are required for a simple health endpoint.

    // =========================================================================
    // ✅ 5. Edge Case and Special Scenario Tests
    // =========================================================================

    /**
     * **unknownEndpoint - Not Found Test**
     * Verify that GET /api/health/does-not-exist returns 404 Not Found.
     */
    @Test
    void unknownHealthSubpath_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/health/does-not-exist"))
               .andExpect(status().isNotFound());
    }
}
