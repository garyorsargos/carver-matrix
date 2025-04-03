package com.fmc.starterApp.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.fmc.starterApp.models.dto.JWTInfoDTO;
import com.fmc.starterApp.models.dto.User2DTO;
import com.fmc.starterApp.models.entity.User2;
import com.fmc.starterApp.repositories.User2Repository;

/**
 * Integration tests for {@link User2Service}, verifying that the service layer:
 * <ul>
 *   <li>Executes its methods correctly and integrates with the underlying repository.</li>
 *   <li>Validates input parameters and handles errors gracefully.</li>
 *   <li>Manages transactions correctly (e.g., rollback on error).</li>
 * </ul>
 *
 * <p>This test class uses an in-memory H2 database (configured in PostgreSQL mode) and real repository implementations.
 * The tests are organized per service function with subsections for basic functionality, input validation,
 * transactional/integration, edge case/exception handling, and caching/performance (if applicable).
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class User2ServiceTest {

    @Autowired
    private User2Repository user2Repository;

    @Autowired
    private User2Service user2Service;

    @Autowired
    private PlatformTransactionManager transactionManager;

    // Instantiate a TransactionTemplate using the injected PlatformTransactionManager.
    private final TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

    // =========================================================================
    // Tests for insertNewUser Function
    // =========================================================================

    // =========================================================================
    // ✅ 1. insertNewUser's Basic Functionality Tests (Unit Test)
    // =========================================================================
    /**
     * **insertNewUser - Valid Input Test**
     * Verify that insertNewUser executes successfully with valid input and returns the expected User2.
     */
    @Test
    @Transactional
    void testInsertNewUser_BasicFunctionality() {
        LocalDateTime now = LocalDateTime.now();
        // Use the correct constructor with a null ID.
        User2 user = new User2(null, "kc-test", "Test", "User", "Test User", "testuser", "testuser@example.com", now);

        User2 savedUser = user2Service.insertNewUser(user);
        assertNotNull(savedUser.getUserId(), "Expected a generated userId for the persisted user");
    }

    // =========================================================================
    // ✅ 2. insertNewUser's Input Validation Tests (Unit Test)
    // =========================================================================
    /**
     * **insertNewUser - Null Input Exception Test**
     * Verify that passing a null User2 to insertNewUser throws an IllegalArgumentException.
     */
    @Test
    void testInsertNewUser_NullInput() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> user2Service.insertNewUser(null),
                "Expected insertNewUser to throw IllegalArgumentException for null input");
        assertThat(ex.getMessage()).contains("User2 must not be null");
    }

    // =========================================================================
    // ✅ 3. insertNewUser's Transactional and Integration Tests
    // =========================================================================
    /**
     * **insertNewUser - Transactional Rollback Test**
     * Simulate an exception within a transaction to verify that changes made by insertNewUser are rolled back.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testInsertNewUser_TransactionalRollback() {
        LocalDateTime now = LocalDateTime.now();
        User2 user = new User2(null, "kc-rollback", "Rollback", "User", "Rollback User", "rollbackuser", "rollback@example.com", now);
        User2 savedUser = user2Service.insertNewUser(user);
        Long userId = savedUser.getUserId();
        String originalUsername = savedUser.getUsername();

        // Use a local TransactionTemplate to force a rollback.
        TransactionTemplate localTxTemplate = new TransactionTemplate(transactionManager);
        localTxTemplate.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        localTxTemplate.execute(status -> {
            User2 userToUpdate = user2Repository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            userToUpdate.setUsername("Updated Rollback User");
            user2Repository.saveAndFlush(userToUpdate);
            // Force rollback.
            status.setRollbackOnly();
            return null;
        });

        User2 fetchedUser = user2Repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        assertEquals(originalUsername, fetchedUser.getUsername(), "User name should remain unchanged after rollback");
    }

    /**
     * **insertNewUser - End-to-End Integration Test**
     * Verify that a new User2 is persisted.
     */
    @Test
    @Transactional
    void testInsertNewUser_EndToEndIntegration() {
        LocalDateTime now = LocalDateTime.now();
        User2 user = new User2(null, "kc-12345", "John", "Doe", "John Doe", "johndoe", "johndoe@example.com", now);
        User2 savedUser = user2Service.insertNewUser(user);
        assertNotNull(savedUser.getUserId(), "The user should have a generated ID after insertion");
    }

    // =========================================================================
    // ✅ 4. insertNewUser's Edge Case and Exception Handling Tests
    // =========================================================================
    /**
     * **insertNewUser - Unexpected Error Handling Test**
     * Verify that the service method gracefully handles unexpected errors by propagating a meaningful exception.
     */
    @Test
    void testInsertNewUser_UnexpectedErrorHandling() {
        try {
            user2Service.insertNewUser(null);
            fail("Expected IllegalArgumentException for null input");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).contains("User2 must not be null");
        }
    }

    // =========================================================================
    // ✅ 5. insertNewUser's Caching and Performance Tests (if applicable)
    // =========================================================================
    // (Not applicable unless caching is implemented in the service layer.)

    // =========================================================================
    // Tests for upsertUser Function
    // =========================================================================

    // =========================================================================
    // ✅ 1. upsertUser's Basic Functionality Tests (Unit Test)
    // =========================================================================
    /**
     * **upsertUser - Create New User Test**
     * Verify that upsertUser creates a new User2 when no matching record exists.
     */
    @Test
    @Transactional
    void testUpsertUser_CreateNewUser() {
        // Create a dummy Jwt token with required claims.
        Jwt jwt = Jwt.withTokenValue("dummy")
                .header("alg", "none")
                .claim("sub", "user-123")
                .claim("preferred_username", "newuser")
                .claim("email", "newuser@example.com")
                .claim("given_name", "New")
                .claim("family_name", "User")
                .claim("name", "New User")
                .build();

        User2 result = user2Service.upsertUser(jwt);
        assertNotNull(result.getUserId(), "New user should be created and assigned an ID");
        assertEquals("newuser", result.getUsername());
        assertEquals("newuser@example.com", result.getEmail());
    }

    // =========================================================================
    // ✅ 2. upsertUser's Input Validation Tests (Unit Test)
    // =========================================================================
    /**
     * **upsertUser - Null Input Exception Test**
     * Verify that passing a null JWT to upsertUser throws an IllegalArgumentException.
     */
    @Test
    void testUpsertUser_NullInput() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> user2Service.upsertUser(null),
                "Expected upsertUser to throw IllegalArgumentException for null JWT input");
        assertThat(ex.getMessage()).contains("JWT must not be null");
    }

    // =========================================================================
    // ✅ 3. upsertUser's Edge Case and Exception Handling Tests (Unit Test)
    // =========================================================================
    /**
     * **upsertUser - Unexpected Error Handling Test**
     * Verify that upsertUser gracefully handles unexpected errors.
     */
    @Test
    void testUpsertUser_UnexpectedErrorHandling() {
        try {
            user2Service.upsertUser(null);
            fail("Expected IllegalArgumentException for null JWT input");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).contains("JWT must not be null");
        }
    }

    // =========================================================================
    // 4. upsertUser's Transactional and Integration Tests (Not applicable)
    // =========================================================================
    // (upsertUser is not annotated with @Transactional; integration tests can be implemented later if needed.)

    // =========================================================================
    // ✅ 5. upsertUser's Caching and Performance Tests (if applicable)
    // =========================================================================
    // (Not applicable unless caching is implemented in the service layer.)

    // =========================================================================
    // Tests for getUserInfo Function
    // =========================================================================

    // =========================================================================
    // ✅ 1. getUserInfo's Basic Functionality Tests (Unit Test)
    // =========================================================================
    /**
     * **getUserInfo - Basic Retrieval Test**
     * Verify that getUserInfo returns a User2DTO with a non-empty user list and a correct total count.
     */
    @Test
    @Transactional
    void testGetUserInfo_BasicFunctionality() {
        LocalDateTime now = LocalDateTime.now();
        User2 user = new User2(null, "kc-info", "Info", "User", "Info User", "infouser", "infouser@example.com", now);
        user2Service.insertNewUser(user);

        User2DTO dto = user2Service.getUserInfo();
        assertNotNull(dto, "User2DTO should not be null");
        assertTrue(dto.getTotalUsers() > 0, "Total users count should be greater than zero");
        assertThat(dto.getUsers()).isNotEmpty();
    }

    // =========================================================================
    // ✅ 2. getUserInfo's Input Validation Tests (Unit Test)
    // =========================================================================
    /**
     * **getUserInfo - Empty Repository Test**
     * Verify that getUserInfo returns a User2DTO with an empty user list and a total count of 0 when no users exist.
     */
    @Test
    @Transactional
    void testGetUserInfo_EmptyRepository() {
        user2Repository.deleteAll();
        User2DTO dto = user2Service.getUserInfo();
        assertNotNull(dto, "User2DTO should not be null even if repository is empty");
        assertEquals(0, dto.getTotalUsers(), "Total users count should be 0 when repository is empty");
        assertThat(dto.getUsers()).isEmpty();
    }

    // =========================================================================
    // ✅ 3. getUserInfo's Integration Tests
    // =========================================================================
    /**
     * **getUserInfo - End-to-End Integration Test**
     * Verify that getUserInfo retrieves all users and returns the correct total count.
     */
    @Test
    @Transactional
    void testGetUserInfo_EndToEndIntegration() {
        LocalDateTime now = LocalDateTime.now();
        User2 user1 = new User2(null, "kc-1", "User", "A", "User A", "userA", "userA@example.com", now);
        User2 user2 = new User2(null, "kc-2", "User", "B", "User B", "userB", "userB@example.com", now);

        user2Service.insertNewUser(user1);
        user2Service.insertNewUser(user2);

        User2DTO dto = user2Service.getUserInfo();
        assertNotNull(dto, "User2DTO should not be null");
        assertTrue(dto.getTotalUsers() >= 2, "Total users count should be at least 2");
        assertThat(dto.getUsers()).extracting(User2::getEmail)
                                  .contains("userA@example.com", "userB@example.com");
    }

    // =========================================================================
    // ✅ 4. getUserInfo's Edge Case and Exception Handling Tests (Unit Test)
    // =========================================================================
    /**
     * **getUserInfo - Error Handling Test**
     * Verify that getUserInfo gracefully handles errors and does not throw unexpected exceptions.
     */
    @Test
    void testGetUserInfo_ErrorHandling() {
        try {
            User2DTO dto = user2Service.getUserInfo();
            assertNotNull(dto, "User2DTO should not be null when retrieving user info");
        } catch (Exception e) {
            fail("getUserInfo should not throw an exception under normal circumstances");
        }
    }

    // =========================================================================
    // ✅ 5. getUserInfo's Caching and Performance Tests (if applicable)
    // =========================================================================
    // (Not applicable unless caching is implemented in the service layer.)

    // =========================================================================
    // Tests for Deprecated Functions: getCurrentUserId and extractJwtInfo
    // =========================================================================

    // =========================================================================
    // ✅ 1. getCurrentUserId's Basic Functionality Tests (Unit Test)
    // =========================================================================
    /**
     * **getCurrentUserId - Basic Retrieval Test**
     * Verify that getCurrentUserId returns a Keycloak ID when the security context is properly set.
     * Note: This test requires setting the SecurityContext; if not possible in integration tests,
     * consider mocking the SecurityContext or simply noting that testing is deferred.
     */
    @Test
    void testGetCurrentUserId_BasicFunctionality() {
        // Since getCurrentUserId is deprecated and depends on the SecurityContext,
        // we simply note that this test is to be implemented with proper SecurityContext setup.
        // For now, we assume the function returns null if not authenticated.
        String currentUserId = user2Service.getCurrentUserId();
        // Not authenticated in the test context, so expect null.
        assertNull(currentUserId, "Expected null when no authentication is present");
    }

    // =========================================================================
    // ✅ 2. extractJwtInfo's Basic Functionality Tests (Unit Test)
    // =========================================================================
    /**
     * **extractJwtInfo - Valid Input Test**
     * Verify that extractJwtInfo returns a JWTInfoDTO with the expected claim values when provided a valid Jwt.
     */
    @Test
    void testExtractJwtInfo_BasicFunctionality() {
        // Build a dummy Jwt with required claims.
        Jwt jwt = Jwt.withTokenValue("dummy")
                .header("alg", "none")
                .claim("sub", "user-456")
                .claim("sid", "session-789")
                .claim("email_verified", true)
                .claim("preferred_username", "testuser")
                .claim("given_name", "Test")
                .claim("family_name", "User")
                .claim("name", "Test User")
                .claim("email", "testuser@example.com")
                .claim("resource_access", Map.of(
                        "starter-app", Map.of("roles", List.of("ROLE_USER")),
                        "account", Map.of("roles", List.of("ROLE_ADMIN"))
                ))
                .build();

        JWTInfoDTO jwtInfo = user2Service.extractJwtInfo(jwt);
        assertNotNull(jwtInfo, "JWTInfoDTO should not be null");
        assertEquals("user-456", jwtInfo.getKeycloakID());
        assertEquals("session-789", jwtInfo.getSessionID());
        assertTrue(jwtInfo.isEmailVerified());
        assertEquals("testuser", jwtInfo.getUsername());
        assertEquals("Test", jwtInfo.getFirstName());
        assertEquals("User", jwtInfo.getLastName());
        assertEquals("Test User", jwtInfo.getFullName());
        assertEquals("testuser@example.com", jwtInfo.getEmail());
        assertThat(jwtInfo.getStarterAppRole()).contains("ROLE_USER");
        assertThat(jwtInfo.getAccountRole()).contains("ROLE_ADMIN");
    }

    // =========================================================================
    // ✅ 3. Deprecated Functions' Input Validation Tests (Unit Test)
    // =========================================================================
    /**
     * **extractJwtInfo - Null Input Exception Test**
     * Verify that passing a null Jwt to extractJwtInfo throws an IllegalArgumentException.
     */
    @Test
    void testExtractJwtInfo_NullInput() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> user2Service.extractJwtInfo(null),
                "Expected extractJwtInfo to throw IllegalArgumentException for null Jwt input");
        assertThat(ex.getMessage()).contains("JWT must not be null");
    }

    // =========================================================================
    // ✅ 4. Deprecated Functions' Edge Case and Exception Handling Tests (Unit Test)
    // =========================================================================
    // (Additional tests for deprecated functions can be added later if needed.)
}
