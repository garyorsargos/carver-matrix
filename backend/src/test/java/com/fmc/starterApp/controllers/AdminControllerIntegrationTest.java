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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;  // only for the destructive test
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link com.fmc.starterApp.controllers.AdminController}, verifying that the controller layer:
 * <ul>
 *   <li>Exposes its endpoints correctly under <code>/api/admin</code>.</li>
 *   <li>Performs input validation and error handling.</li>
 *   <li>Enforces security rules: authenticated access required, and only users with STARTER_ADMIN role allowed.</li>
 *   <li>Integrates end‑to‑end with service and persistence for POST→GET cycles.</li>
 *   <li>Handles edge cases such as backend errors and Unicode input.</li>
 * </ul>
 *
 * <p>This test class uses MockMvc with Spring Security filters, an in‑memory H2 database configured
 * via <code>application-test.properties</code>, and JdbcTemplate for setup/teardown and one destructive
 * test annotated with {@link DirtiesContext}.</p>
 */
@Disabled("Excluded due to Spring Boot dep conflict. Omitting spring-webmvc from dependency mangement allows testing but breaks production")
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class AdminControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;

    // helper authorities
    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_STARTER_ADMIN");
    private static final SimpleGrantedAuthority READ  = new SimpleGrantedAuthority("ROLE_STARTER_READ");

    @BeforeEach
    void cleanup() {
        // Reset tables before each test
        jdbcTemplate.execute("DELETE FROM USER_LOGS");
        jdbcTemplate.execute("DELETE FROM USERS");
    }

    // =========================================================================
    // ✅ 1. Basic Endpoint Tests (Web Test)
    // =========================================================================

    /**
     * **usersAdminData - Endpoint Exists**
     * Verify that GET /api/admin/users with ADMIN role returns 200 OK.
     */
    @Test
    void usersAdminData_withAdmin_returnsOk() throws Exception {
        // seed one user via POST
        String payload = """
            { "userName":"Alice", "email":"alice@example.com" }
            """;
        mockMvc.perform(post("/api/admin/users")
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
               .andExpect(status().isOk());

        // existence check
        mockMvc.perform(get("/api/admin/users")
                .with(jwt().authorities(ADMIN)))
               .andExpect(status().isOk());
    }

    /**
     * **addKeyToRoles - Endpoint Exists**
     * Verify that POST /api/admin/users with ADMIN role returns 200 OK.
     */
    @Test
    void addKeyToRoles_withAdmin_returnsOk() throws Exception {
        String payload = """
            { "userName":"Bob", "email":"bob@example.com" }
            """;
        mockMvc.perform(post("/api/admin/users")
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
               .andExpect(status().isOk());
    }

    // =========================================================================
    // ✅ 2. Input Validation and Error Handling Tests
    // =========================================================================

    /**
     * **addKeyToRoles - Empty or Malformed JSON Test**
     * Verify that POST /api/admin/users with empty or invalid JSON returns 400 Bad Request.
     */
    @Test
    void addKeyToRoles_invalidJson_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
               .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/admin/users")
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
               .andExpect(status().isBadRequest());
    }

    /**
     * **addKeyToRoles - Missing Email Field Test**
     * Verify that POST /api/admin/users without the email field returns 400 Bad Request.
     */
    @Test
    void addKeyToRoles_missingEmailField_returnsBadRequest() throws Exception {
        String payload = """
            { "userName":"NoEmail" }
            """;
        mockMvc.perform(post("/api/admin/users")
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
               .andExpect(status().isBadRequest());
    }

    /**
     * **addKeyToRoles - Invalid Email Format Test**
     * Verify that POST /api/admin/users with malformed email returns 400 Bad Request.
     */
    @Test
    void addKeyToRoles_invalidEmailFormat_returnsBadRequest() throws Exception {
        String payload = """
            { "userName":"BadEmail", "email":"not-an-email" }
            """;
        mockMvc.perform(post("/api/admin/users")
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
               .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // ✅ 3. Security and Authorization Tests
    // =========================================================================

    /**
     * **usersAdminData - No Authentication Test**
     * Verify that GET /api/admin/users without JWT returns 403 Forbidden.
     */
    @Test
    void usersAdminData_noAuth_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
               .andExpect(status().isForbidden());
    }

    /**
     * **usersAdminData - Insufficient Role Test**
     * Verify that GET /api/admin/users with READ role returns 401 Unauthorized.
     */
    @Test
    void usersAdminData_insufficientRole_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .with(jwt().authorities(READ)))
               .andExpect(status().isUnauthorized());
    }

    /**
     * **addKeyToRoles - No Authentication Test**
     * Verify that POST /api/admin/users without JWT returns 403 Forbidden.
     */
    @Test
    void addKeyToRoles_noAuth_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"Bob\",\"email\":\"bob@example.com\"}"))
               .andExpect(status().isForbidden());
    }

    /**
     * **addKeyToRoles - Insufficient Role Test**
     * Verify that POST /api/admin/users with READ role returns 401 Unauthorized.
     */
    @Test
    void addKeyToRoles_readRole_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                .with(jwt().authorities(READ))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"Bob\",\"email\":\"bob@example.com\"}"))
               .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // ✅ 4. Integration and End‑to‑End Tests
    // =========================================================================

    /**
     * **usersAdminData - Basic Retrieval Test**
     * Verify full POST → GET cycle returns correct totalUsers and user data.
     */
    @Test
    void usersAdminData_returnsOkAndDto() throws Exception {
        String payload = """
            { "userName":"Alice", "email":"alice@example.com" }
            """;
        mockMvc.perform(post("/api/admin/users")
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
               .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/users")
                .with(jwt().authorities(ADMIN)))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.totalUsers").value(1))
               .andExpect(jsonPath("$.users", hasSize(1)))
               .andExpect(jsonPath("$.users[0].email").value("alice@example.com"));
    }

    /**
     * **addKeyToRoles - Basic Creation Test**
     * Verify that POST /api/admin/users with valid body returns 200 OK and the created user JSON.
     */
    @Test
    void addKeyToRoles_returnsOkAndCreatedUser() throws Exception {
        String payload = """
            { "userName":"Bob", "email":"bob@example.com" }
            """;
        mockMvc.perform(post("/api/admin/users")
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.userId", notNullValue()))
               .andExpect(jsonPath("$.email").value("bob@example.com"));
    }

    /**
     * **addKeyToRoles_thenGetUsers - End‑to‑End Cycle Test**
     * Verify full POST → GET cycle returns correct totalUsers and user data for multiple calls.
     */
    @Test
    void addKeyToRoles_thenGetUsers_endToEnd() throws Exception {
        String payload = """
            { "userName":"Dana","email":"dana@example.com" }
            """;
        mockMvc.perform(post("/api/admin/users")
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
               .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/users")
                .with(jwt().authorities(ADMIN)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalUsers").value(1))
               .andExpect(jsonPath("$.users[0].email").value("dana@example.com"));
    }

    // =========================================================================
    // ✅ 5. Edge Case and Special Scenario Tests
    // =========================================================================

    /**
     * **usersAdminData - Service Error Test**
     * Simulate a backend failure by dropping tables, expecting 400 Bad Request.
     */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void usersAdminData_serviceError_returnsBadRequest() throws Exception {
        jdbcTemplate.execute("DROP TABLE USER_LOGS");
        jdbcTemplate.execute("DROP TABLE USERS");

        mockMvc.perform(get("/api/admin/users")
                .with(jwt().authorities(ADMIN)))
               .andExpect(status().isBadRequest());
    }

    /**
     * **addKeyToRoles - Unicode Support Test**
     * Verify that POST /api/admin/users accepts and returns Unicode characters in userName.
     */
    @Test
    void addKeyToRoles_specialCharactersInName_returnsOk() throws Exception {
        String payload = """
            { "userName":"Ťëßt Üšêr","email":"testuser@example.com" }
            """;
        mockMvc.perform(post("/api/admin/users")
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.userName").value("Ťëßt Üšêr"));
    }
}
