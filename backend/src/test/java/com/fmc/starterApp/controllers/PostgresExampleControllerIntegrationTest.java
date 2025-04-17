package com.fmc.starterApp.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link com.fmc.starterApp.controllers.PostgresExampleController}, verifying that:
 * <ul>
 *   <li>CRUD endpoints under <code>/api/db</code> are mapped correctly.</li>
 *   <li>Input validation returns 400 for malformed JSON.</li>
 *   <li>Authentication is enforced (403 when no JWT).</li>
 *   <li>End‑to‑end flows (POST→GET, PUT→GET, DELETE→GET) work as expected.</li>
 *   <li>Service failures (dropped table) return 400 Bad Request.</li>
 * </ul>
 *
 * <p>Uses MockMvc with JWT simulation and an in‑memory H2 database configured via
 * <code>application-test.properties</code>. The <code>example</code> table is cleared before each test.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class PostgresExampleControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;
    private static final String BASE = "/api/db";

    @BeforeEach
    void cleanup() {
        // Clear the example table before each test
        jdbcTemplate.execute("DELETE FROM example");
    }

    // =========================================================================
    // ✅ 1. Basic Endpoint Tests (Web Test)
    // =========================================================================

    /**
     * **GET /api/db - Existence Test**
     * Verify that GET /api/db with JWT returns 200 OK.
     */
    @Test
    void getDbObjects_list_endpointExists() throws Exception {
        mockMvc.perform(get(BASE).with(jwt()))
               .andExpect(status().isOk());
    }

    /**
     * **POST /api/db - Existence Test**
     * Verify that POST /api/db with JWT returns 200 OK.
     */
    @Test
    void saveDbObject_withJwt_endpointExists() throws Exception {
        mockMvc.perform(post(BASE)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"X\"}"))
               .andExpect(status().isOk());
    }

    /**
     * **PUT /api/db - Existence Test**
     * Verify that PUT /api/db with JWT returns 200 OK.
     */
    @Test
    void updateObject_withJwt_endpointExists() throws Exception {
        // seed record
        jdbcTemplate.update("INSERT INTO example(name) VALUES (?)", "Before");
        Long id = jdbcTemplate.queryForObject("SELECT id FROM example WHERE name='Before'", Long.class);

        mockMvc.perform(put(BASE)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"id\":%d,\"name\":\"New\"}", id)))
               .andExpect(status().isOk());
    }

    /**
     * **DELETE /api/db/{id} - Existence Test**
     * Verify that DELETE /api/db/{id} with JWT returns 200 OK.
     */
    @Test
    void deleteObjectById_withJwt_endpointExists() throws Exception {
        jdbcTemplate.update("INSERT INTO example(name) VALUES (?)", "ToDel");
        Long id = jdbcTemplate.queryForObject("SELECT id FROM example WHERE name='ToDel'", Long.class);

        mockMvc.perform(delete(BASE + "/" + id).with(jwt()))
               .andExpect(status().isOk());
    }

    // =========================================================================
    // ✅ 2. Input Validation and Error Handling Tests
    // =========================================================================

    /**
     * **POST /api/db - Invalid JSON Test**
     * Verify that an empty or malformed JSON body returns 400 Bad Request.
     */
    @Test
    void saveDbObject_invalidJson_returnsBadRequest() throws Exception {
        mockMvc.perform(post(BASE)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
               .andExpect(status().isBadRequest());

        mockMvc.perform(post(BASE)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
               .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // ✅ 3. Security and Authorization Tests
    // =========================================================================

    /**
     * **POST /api/db - No Authentication Test**
     * Verify that POST /api/db without JWT returns 403 Forbidden.
     */
    @Test
    void saveDbObject_noAuth_returnsForbidden() throws Exception {
        mockMvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test1\"}"))
               .andExpect(status().isForbidden());
    }

    /**
     * **GET /api/db - No Authentication Test**
     * Verify that GET /api/db without JWT returns 403 Forbidden.
     */
    @Test
    void getDbObjects_noAuth_returnsForbidden() throws Exception {
        mockMvc.perform(get(BASE))
               .andExpect(status().isForbidden());
    }

    // =========================================================================
    // ✅ 4. Integration and End‑to‑End Tests
    // =========================================================================

    /**
     * **GET /api/db - List Test**
     * Verify that GET /api/db returns a JSON array of all seeded objects.
     */
    @Test
    void getDbObjects_list_returnsOkAndJsonArray() throws Exception {
        jdbcTemplate.update("INSERT INTO example(name) VALUES (?)", "One");
        jdbcTemplate.update("INSERT INTO example(name) VALUES (?)", "Two");

        mockMvc.perform(get(BASE).with(jwt()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)));
    }

    /**
     * **GET /api/db/{id} - Single Object Test**
     * Verify that GET /api/db/{id} returns the correct JSON.
     */
    @Test
    void getDbObjects_byId_returnsOkAndJsonObject() throws Exception {
        jdbcTemplate.update("INSERT INTO example(name) VALUES (?)", "Solo");
        Long id = jdbcTemplate.queryForObject("SELECT id FROM example WHERE name='Solo'", Long.class);

        mockMvc.perform(get(BASE + "/" + id).with(jwt()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", is(id.intValue())))
               .andExpect(jsonPath("$.name", is("Solo")));
    }

    /**
     * **PUT /api/db - Update Test**
     * Verify that PUT /api/db updates an existing object and GET reflects the new name.
     */
    @Test
    void updateObject_withJwt_returnsOkAndUpdatedName() throws Exception {
        jdbcTemplate.update("INSERT INTO example(name) VALUES (?)", "Before");
        Long id = jdbcTemplate.queryForObject("SELECT id FROM example WHERE name='Before'", Long.class);

        mockMvc.perform(put(BASE)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"id\":%d,\"name\":\"After\"}", id)))
               .andExpect(status().isOk())
               .andExpect(content().string("After"));

        mockMvc.perform(get(BASE + "/" + id).with(jwt()))
               .andExpect(jsonPath("$.name", is("After")));
    }

    /**
     * **DELETE /api/db/{id} - Delete Test**
     * Verify that DELETE /api/db/{id} removes the object and returns its ID.
     */
    @Test
    void deleteObjectById_withJwt_returnsOkAndId() throws Exception {
        jdbcTemplate.update("INSERT INTO example(name) VALUES (?)", "DelMe");
        Long id = jdbcTemplate.queryForObject("SELECT id FROM example WHERE name='DelMe'", Long.class);

        mockMvc.perform(delete(BASE + "/" + id).with(jwt()))
               .andExpect(status().isOk())
               .andExpect(content().string(id.toString()));

        mockMvc.perform(get(BASE + "/" + id).with(jwt()))
               .andExpect(content().string(""));
    }

    // =========================================================================
    // ✅ 5. Edge Case and Special Scenario Tests
    // =========================================================================

    /**
     * **Service Error Test**
     * Simulate table drop and verify all endpoints return 400 Bad Request.
     */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void anyDbEndpoint_serviceError_returnsBadRequest() throws Exception {
        jdbcTemplate.execute("DROP TABLE example");

        mockMvc.perform(get(BASE).with(jwt()))
               .andExpect(status().isBadRequest());

        mockMvc.perform(post(BASE)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"X\"}"))
               .andExpect(status().isBadRequest());

        mockMvc.perform(put(BASE)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"name\":\"X\"}"))
               .andExpect(status().isBadRequest());

        mockMvc.perform(delete(BASE + "/1").with(jwt()))
               .andExpect(status().isBadRequest());
    }
}
