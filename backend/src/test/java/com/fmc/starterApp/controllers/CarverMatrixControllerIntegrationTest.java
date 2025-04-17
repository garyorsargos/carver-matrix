package com.fmc.starterApp.controllers;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmc.starterApp.models.entity.User2;
import com.fmc.starterApp.repositories.User2Repository;

/**
 * Integration tests for {@link com.fmc.starterApp.controllers.CarverMatrixController}, verifying that:
 * <ul>
 *   <li>All endpoints under <code>/api/carvermatrices</code> enforce authentication.</li>
 *   <li>CRUD and search endpoints behave correctly with valid input.</li>
 *   <li>Invalid JSON yields HTTP 400 Bad Request.</li>
 *   <li>Backend failures yield HTTP 500 Internal Server Error.</li>
 * </ul>
 *
 * <p>This test class uses MockMvc with JWT simulation (providing an <code>email</code> claim)
 * and an in‑memory H2 database configured via <code>application-test.properties</code>.
 * Tables are cleared before each test.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class CarverMatrixControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private User2Repository user2Repository;

    private static final String BASE = "/api/carvermatrices";
    private static final JwtRequestPostProcessor VALID_JWT =
        jwt().jwt(j -> j.claim("email", "u@x.com"));
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeEach
    void cleanup() {
        // Clear tables in uppercase to match H2's default quoting
        jdbcTemplate.execute("DELETE FROM CARVER_ITEMS");
        jdbcTemplate.execute("DELETE FROM CARVER_MATRICES");
        user2Repository.deleteAll();
    }

    // =========================================================================
    // ✅ 1. Basic Endpoint Tests (Web Test)
    // =========================================================================

    /**
     * **createCarverMatrix - Creation Test**
     * Verify that POST /api/carvermatrices/create with valid JWT and body
     * returns 201 Created, with a non-null matrixId, correct name, and items list size.
     */
    @Test
    void createCarverMatrix_withJwt_returnsCreated() throws Exception {
        User2 user = new User2(
            null, "kc1", "First", "Last", "First Last",
            "user1", "u@x.com", LocalDateTime.now()
        );
        user = user2Repository.save(user);

        String json = """
            {
              "name":"Matrix1",
              "description":"Desc",
              "hosts":["u@x.com"],
              "participants":["u@x.com"],
              "items":[{"itemName":"Item1"}]
            }
            """;

        mockMvc.perform(post(BASE + "/create")
                .with(VALID_JWT)
                .queryParam("userId", user.getUserId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.matrixId", notNullValue()))
               .andExpect(jsonPath("$.name").value("Matrix1"))
               .andExpect(jsonPath("$.items", hasSize(1)));
    }

    /**
     * **getCarverMatrixByCarverId - Retrieval Test**
     * Verify that GET /api/carvermatrices/{matrixId} with valid JWT
     * returns 200 OK and the correct JSON structure (matrixId, hosts, items).
     */
    @Test
    void getCarverMatrixByCarverId_withJwt_returnsOkAndJson() throws Exception {
        User2 user = user2Repository.save(new User2(
            null, "kc2", "F", "L", "F L", "user2", "u2@x.com", LocalDateTime.now()
        ));

        String create = """
            {
              "name":"M2","description":"D2",
              "hosts":["u@x.com"],"participants":["u@x.com"],
              "items":[{"itemName":"I2"}]
            }
            """;

        mockMvc.perform(post(BASE + "/create")
                .with(VALID_JWT)
                .queryParam("userId", user.getUserId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(create))
               .andExpect(status().isCreated());

        Long matrixId = jdbcTemplate.queryForObject(
            "SELECT MATRIX_ID FROM CARVER_MATRICES WHERE NAME = 'M2'", Long.class);

        mockMvc.perform(get(BASE + "/" + matrixId).with(VALID_JWT))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.matrixId").value(matrixId.intValue()))
               .andExpect(jsonPath("$.hosts", hasSize(1)))
               .andExpect(jsonPath("$.items", hasSize(1)));
    }

    // =========================================================================
    // ✅ 2. Input Validation and Error Handling Tests
    // =========================================================================

    /**
     * **createCarverMatrix - Invalid JSON Test**
     * Verify that POST /api/carvermatrices/create with malformed or empty JSON
     * returns 400 Bad Request.
     */
    @Test
    void createCarverMatrix_invalidJson_returnsBadRequest() throws Exception {
        mockMvc.perform(post(BASE + "/create")
                .with(VALID_JWT)
                .queryParam("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
               .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // ✅ 3. Security and Authorization Tests
    // =========================================================================

    /**
     * **anyEndpoint - No Authentication Test**
     * Verify that GET /api/carvermatrices/{id} without JWT returns 403 Forbidden.
     */
    @Test
    void anyEndpoint_noAuth_returnsForbidden() throws Exception {
        mockMvc.perform(get(BASE + "/1"))
               .andExpect(status().isForbidden());
    }

    // =========================================================================
    // ✅ 4. Integration and End‑to‑End Tests
    // =========================================================================

    /**
     * **searchCarverMatrices - Search Test**
     * Verify that GET /api/carvermatrices/search with a name filter
     * returns 200 OK and a list containing only matching matrices.
     */
    @Test
    void searchCarverMatrices_withJwt_returnsFilteredList() throws Exception {
        User2 user = user2Repository.save(new User2(
            null, "kc5", "F","L","F L","user5","u5@x.com",LocalDateTime.now()
        ));
        String a = """
            {"name":"FindMe","hosts":["u@x.com"],"items":[{"itemName":"A"}]}
            """;
        String b = """
            {"name":"Other","hosts":["u@x.com"],"items":[{"itemName":"B"}]}
            """;

        mockMvc.perform(post(BASE + "/create")
                .with(VALID_JWT)
                .queryParam("userId", user.getUserId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(a))
               .andExpect(status().isCreated());
        mockMvc.perform(post(BASE + "/create")
                .with(VALID_JWT)
                .queryParam("userId", user.getUserId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(b))
               .andExpect(status().isCreated());

        mockMvc.perform(get(BASE + "/search")
                .with(VALID_JWT)
                .queryParam("name","FindMe"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].name").value("FindMe"));
    }

    /**
     * **updateCarverMatrix - Update Test**
     * Verify that PUT /api/carvermatrices/{matrixId}/update changes the matrix name
     * and GET reflects the new value.
     */
    @Test
    void updateCarverMatrix_withJwt_returnsOkAndUpdatedName() throws Exception {
        User2 user = user2Repository.save(new User2(
            null, "kc6","F","L","F L","user6","u6@x.com",LocalDateTime.now()
        ));
        String create = """
            {"name":"Old","hosts":["u@x.com"],"items":[{"itemName":"I"}]}
            """;

        mockMvc.perform(post(BASE + "/create")
                .with(VALID_JWT)
                .queryParam("userId", user.getUserId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(create))
               .andExpect(status().isCreated());

        Long id = jdbcTemplate.queryForObject(
            "SELECT MATRIX_ID FROM CARVER_MATRICES WHERE NAME='Old'", Long.class);

        mockMvc.perform(put(BASE + "/" + id + "/update")
                .with(VALID_JWT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New\"}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("New"));

        mockMvc.perform(get(BASE + "/" + id).with(VALID_JWT))
               .andExpect(jsonPath("$.name").value("New"));
    }

    /**
     * **updateCarverItems - CarverItems Update Test**
     * Verify that PUT /api/carvermatrices/{matrixId}/carveritems/update
     * updates the criticality metric for the given user email.
     */
    @Test
    void updateCarverItems_withJwt_returnsOkAndUpdatedMetrics() throws Exception {
        User2 user = user2Repository.save(new User2(
            null, "kc7","F","L","F L","user7","u7@x.com",LocalDateTime.now()
        ));
        String create = """
            {"name":"ITest","hosts":["u@x.com"],"items":[{"itemName":"X"}]}
            """;

        mockMvc.perform(post(BASE + "/create")
                .with(VALID_JWT)
                .queryParam("userId", user.getUserId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(create))
               .andExpect(status().isCreated());

        Long matrixId = jdbcTemplate.queryForObject(
            "SELECT MATRIX_ID FROM CARVER_MATRICES WHERE NAME='ITest'", Long.class);
        Long itemId = jdbcTemplate.queryForObject(
            "SELECT ITEM_ID FROM CARVER_ITEMS WHERE MATRIX_ID = ?", Long.class, matrixId);

        String updateList = MAPPER.writeValueAsString(new Map[]{
            Map.of("itemId", itemId, "criticality", 7)
        });

        mockMvc.perform(put(BASE + "/" + matrixId + "/carveritems/update")
                .with(VALID_JWT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateList))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].itemId").value(itemId.intValue()))
               .andExpect(jsonPath("$[0].criticality['u@x.com']").value(7));
    }

    /**
     * **deleteCarverMatrix - Delete Test**
     * Verify that DELETE /api/carvermatrices/{matrixId} removes the matrix
     * and a subsequent GET returns 500 Internal Server Error.
     */
    @Test
    void deleteCarverMatrix_withJwt_returnsOkAndMessage() throws Exception {
        User2 user = user2Repository.save(new User2(
            null, "kc8","F","L","F L","user8","u8@x.com",LocalDateTime.now()
        ));
        String create = """
            {"name":"DTest","hosts":["u@x.com"],"items":[{"itemName":"Y"}]}
            """;

        mockMvc.perform(post(BASE + "/create")
                .with(VALID_JWT)
                .queryParam("userId", user.getUserId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(create))
               .andExpect(status().isCreated());

        Long id = jdbcTemplate.queryForObject(
            "SELECT MATRIX_ID FROM CARVER_MATRICES WHERE NAME='DTest'", Long.class);

        mockMvc.perform(delete(BASE + "/" + id).with(VALID_JWT))
               .andExpect(status().isOk())
               .andExpect(content().string("CarverMatrix deleted successfully"));

        mockMvc.perform(get(BASE + "/" + id).with(VALID_JWT))
               .andExpect(status().isInternalServerError());
    }

    // =========================================================================
    // ✅ 5. Edge Case and Special Scenario Tests
    // =========================================================================

    /**
     * **anyEndpoint - Service Error Test**
     * Simulate backend failure by dropping the <code>CARVER_MATRICES</code> table,
     * then hitting GET /api/carvermatrices/{id} to expect 500 Internal Server Error.
     */
    @Test
    void anyEndpoint_serviceError_returnsServerError() throws Exception {
        try {
            jdbcTemplate.execute("DROP TABLE CARVER_MATRICES");
        } catch (Exception ignored) {}

        mockMvc.perform(get(BASE + "/1").with(VALID_JWT))
               .andExpect(status().isInternalServerError());
    }
}
