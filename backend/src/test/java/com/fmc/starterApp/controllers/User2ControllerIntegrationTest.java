package com.fmc.starterApp.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fmc.starterApp.repositories.User2Repository;

import jakarta.servlet.http.Cookie;

/**
 * Integration tests for {@link com.fmc.starterApp.controllers.User2Controller}, verifying that:
 * <ul>
 *   <li>All endpoints under <code>/api/user2</code> work with the required JWT claims.</li>
 *   <li>Input validation returns HTTP 400 for malformed or missing JSON fields.</li>
 *   <li>Authentication is enforced (403 Forbidden when no JWT is provided).</li>
 *   <li>End‑to‑end flows (POST→GET users, GET whoami, GET whoami‑upsert, GET logout) succeed.</li>
 *   <li>Unknown endpoints return 404 Not Found.</li>
 * </ul>
 *
 * <p>This test class uses MockMvc with JWT simulation (providing all required claims) and an
 * in‑memory H2 database configured via <code>application-test.properties</code>. The
 * <code>users2</code> table is cleared before each test.</p>
 */
@Disabled("Excluded due to Spring Boot dep conflict. Omitting spring-webmvc from dependency mangement allows testing but breaks production")
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class User2ControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private User2Repository user2Repository;

    private static final String BASE = "/api/user2";

    private static final JwtRequestPostProcessor VALID_JWT = jwt().jwt(jwt -> {
        jwt.claim("sub", "kc-id")
           .claim("sid", "sess-id")
           .claim("email_verified", true)
           .claim("preferred_username", "uname")
           .claim("email", "u@x.com")
           .claim("given_name", "First")
           .claim("family_name", "Last")
           .claim("name", "First Last");
    });

    @BeforeEach
    void cleanup() {
        // Clear the users2 table before each test
        user2Repository.deleteAll();
    }

    // =========================================================================
    // ✅ 1. Basic Endpoint Tests (Web Test)
    // =========================================================================

    /**
     * **user2Data - Existence Test**
     * Verify that GET /api/user2/users with a valid JWT returns HTTP 200 OK.
     */
    @Test
    void user2Data_withJwt_endpointExists() throws Exception {
        mockMvc.perform(get(BASE + "/users").with(VALID_JWT))
               .andExpect(status().isOk());
    }

    /**
     * **addUser2 - Existence Test**
     * Verify that POST /api/user2/users with a valid JWT returns HTTP 200 OK.
     */
    @Test
    void addUser2_withJwt_endpointExists() throws Exception {
        String payload = """
            {"keycloakId":"kc1","username":"user1","email":"u1@example.com"}
            """;
        mockMvc.perform(post(BASE + "/users")
                .with(VALID_JWT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
               .andExpect(status().isOk());
    }

    /**
     * **whoAmI - Existence Test**
     * Verify that GET /api/user2/whoami with a valid JWT returns HTTP 200 OK.
     */
    @Test
    void whoAmI_withJwt_endpointExists() throws Exception {
        mockMvc.perform(get(BASE + "/whoami").with(VALID_JWT))
               .andExpect(status().isOk());
    }

    /**
     * **whoAmIUpsert - Existence Test**
     * Verify that GET /api/user2/whoami-upsert with a valid JWT returns HTTP 200 OK.
     */
    @Test
    void whoAmIUpsert_withJwt_endpointExists() throws Exception {
        mockMvc.perform(get(BASE + "/whoami-upsert").with(VALID_JWT))
               .andExpect(status().isOk());
    }

    /**
     * **logout - Existence Test**
     * Verify that GET /api/user2/logout with a valid JWT returns HTTP 200 OK.
     */
    @Test
    void logout_withJwt_endpointExists() throws Exception {
        Cookie cookie = new Cookie("test", "val");
        mockMvc.perform(get(BASE + "/logout")
                .with(VALID_JWT)
                .cookie(cookie))
               .andExpect(status().isOk());
    }

    // =========================================================================
    // ✅ 2. Input Validation and Error Handling Tests
    // =========================================================================

    /**
     * **addUser2 - Invalid JSON Test**
     * Verify that POST /api/user2/users with malformed JSON returns HTTP 400 Bad Request.
     */
    @Test
    void addUser2_invalidJson_returnsBadRequest() throws Exception {
        mockMvc.perform(post(BASE + "/users")
                .with(VALID_JWT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid }"))
               .andExpect(status().isBadRequest());
    }

    /**
     * **addUser2 - Missing Fields Test**
     * Verify that POST /api/user2/users with missing required fields returns HTTP 400 Bad Request.
     */
    @Test
    void addUser2_missingFields_returnsBadRequest() throws Exception {
        String payload = "{\"username\":\"user1\"}";
        mockMvc.perform(post(BASE + "/users")
                .with(VALID_JWT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
               .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // ✅ 3. Security and Authorization Tests
    // =========================================================================

    /**
     * **user2Data - No Authentication Test**
     * Verify that GET /api/user2/users without a JWT returns HTTP 403 Forbidden.
     */
    @Test
    void user2Data_noAuth_returnsForbidden() throws Exception {
        mockMvc.perform(get(BASE + "/users"))
               .andExpect(status().isForbidden());
    }

    /**
     * **addUser2 - No Authentication Test**
     * Verify that POST /api/user2/users without a JWT returns HTTP 403 Forbidden.
     */
    @Test
    void addUser2_noAuth_returnsForbidden() throws Exception {
        String payload = """
            {"keycloakId":"kc1","username":"user1","email":"u1@example.com"}
            """;
        mockMvc.perform(post(BASE + "/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
               .andExpect(status().isForbidden());
    }

    // =========================================================================
    // ✅ 4. Integration and End‑to‑End Tests
    // =========================================================================

    /**
     * **addUser2_thenGetUsers - End‑to‑End Cycle Test**
     * Verify POST→GET cycle returns the newly inserted user in the list.
     */
    @Test
    void addUser2_thenGetUsers_endToEnd() throws Exception {
        String payload = """
            {"keycloakId":"kc2","username":"user2","email":"u2@example.com"}
            """;
        mockMvc.perform(post(BASE + "/users")
                .with(VALID_JWT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.userId", notNullValue()))
               .andExpect(jsonPath("$.username").value("user2"));

        mockMvc.perform(get(BASE + "/users").with(VALID_JWT))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalUsers").value(1))
               .andExpect(jsonPath("$.users", hasSize(1)))
               .andExpect(jsonPath("$.users[0].email").value("u2@example.com"));
    }

    /**
     * **whoAmI - JWT Info Extraction Test**
     * Verify that GET /api/user2/whoami returns a JWTInfoDTO populated from the JWT claims.
     */
    @Test
    void whoAmI_returnsJwtInfo() throws Exception {
        mockMvc.perform(get(BASE + "/whoami").with(VALID_JWT))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.keycloakID").value("kc-id"))
               .andExpect(jsonPath("$.sessionID").value("sess-id"))
               .andExpect(jsonPath("$.emailVerified").value(true))
               .andExpect(jsonPath("$.username").value("uname"))
               .andExpect(jsonPath("$.email").value("u@x.com"));
    }

    /**
     * **whoAmIUpsert - Upsert Test**
     * Verify that GET /api/user2/whoami-upsert creates or returns a User2 entity based on JWT claims.
     */
    @Test
    void whoAmIUpsert_createsAndReturnsUser() throws Exception {
        mockMvc.perform(get(BASE + "/whoami-upsert").with(VALID_JWT))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.keycloakId").value("kc-id"))
               .andExpect(jsonPath("$.username").value("uname"))
               .andExpect(jsonPath("$.email").value("u@x.com"));
    }

    /**
     * **logout - Cookie Clearing Test**
     * Verify that GET /api/user2/logout clears cookies by setting maxAge=0.
     */
    @Test
    void logout_clearsCookies() throws Exception {
        Cookie cookie = new Cookie("t", "v");
        mockMvc.perform(get(BASE + "/logout")
                .with(VALID_JWT)
                .cookie(cookie))
               .andExpect(status().isOk())
               .andExpect(cookie().maxAge("t", 0));
    }

    // =========================================================================
    // ✅ 5. Edge Case and Special Scenario Tests
    // =========================================================================

    /**
     * **unknownEndpoint - Not Found Test**
     * Verify that accessing an undefined endpoint under /api/user2 returns HTTP 404 Not Found.
     */
    @Test
    void unknownEndpoint_returnsNotFound() throws Exception {
        mockMvc.perform(get(BASE + "/nope").with(VALID_JWT))
               .andExpect(status().isNotFound());
    }
}
