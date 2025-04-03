package com.fmc.starterApp.services;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.fmc.starterApp.models.dto.AdminDTO;
import com.fmc.starterApp.models.entity.AppUser;
import com.fmc.starterApp.models.entity.UserLogs;
import com.fmc.starterApp.repositories.UserLogsRepository;
import com.fmc.starterApp.repositories.UsersRepository;

/**
 * Integration tests for {@link AdminService}, verifying that the service layer:
 * <ul>
 *   <li>Executes its methods correctly and integrates with the underlying repositories.</li>
 *   <li>Validates input parameters and handles errors gracefully.</li>
 *   <li>Manages transactions correctly (e.g., rollback on error).</li>
 * </ul>
 *
 * <p>This test class uses an in-memory H2 database (configured in PostgreSQL mode) and real repository implementations.
 * The tests are organized per service function with subsections for basic functionality, input validation,
 * transactional/integration, and edge case/exception handling.
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class AdminServiceTest {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private UserLogsRepository userLogsRepository;

    @Autowired
    private AdminService adminService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // =========================================================================
    // Tests for insertNewUser Function
    // =========================================================================

    // =========================================================================
    // ✅ 1. insertNewUser's Basic Functionality Tests (Unit Test)
    // =========================================================================
    /**
     * **insertNewUser - Valid Input Test**
     * Verify that insertNewUser executes successfully with valid input and returns the expected AppUser.
     */
    @Test
    @Transactional
    void testInsertNewUser_BasicFunctionality() {
        AppUser user = new AppUser();
        user.setUserName("Test User");
        user.setEmail("testuser@example.com");

        AppUser savedUser = adminService.insertNewUser(user);
        assertNotNull(savedUser.getUserId(), "Expected a generated userId for the persisted user");

        // Verify that a corresponding UserLogs record was created.
        List<UserLogs> logs = userLogsRepository.findAll();
        assertFalse(logs.isEmpty(), "Expected at least one UserLogs record after inserting a new user");
        boolean logFound = logs.stream()
                .anyMatch(log -> log.getAppUser().getUserId().equals(savedUser.getUserId()));
        assertTrue(logFound, "Expected a login log record for the new user");
    }

    // =========================================================================
    // ✅ 2. insertNewUser's Input Validation Tests (Unit Test)
    // =========================================================================
    /**
     * **insertNewUser - Null Input Exception Test**
     * Verify that passing a null AppUser to insertNewUser throws an IllegalArgumentException.
     */
    @Test
    void testInsertNewUser_InvalidInput_Null() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
                () -> adminService.insertNewUser(null),
                "Expected insertNewUser to throw IllegalArgumentException for null input");
        assertThat(ex.getMessage()).contains("AppUser must not be null");
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
        AppUser user = new AppUser();
        user.setUserName("Rollback User");
        user.setEmail("rollback@example.com");
        AppUser savedUser = adminService.insertNewUser(user);
        Long userId = savedUser.getUserId();
        String originalName = savedUser.getUserName();

        // Instantiate a new TransactionTemplate here to ensure transactionManager is available.
        TransactionTemplate localTxTemplate = new TransactionTemplate(transactionManager);
        localTxTemplate.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        localTxTemplate.execute(status -> {
            AppUser userToUpdate = usersRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            userToUpdate.setUserName("Updated Rollback User");
            usersRepository.saveAndFlush(userToUpdate);
            // Force rollback.
            status.setRollbackOnly();
            return null;
        });

        AppUser fetchedUser = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        assertEquals(originalName, fetchedUser.getUserName(), "User name should remain unchanged after rollback");
    }

    /**
     * **insertNewUser - End-to-End Integration Test**
     * Verify that a new AppUser is persisted along with its corresponding login log.
     */
    @Test
    @Transactional
    void testInsertNewUser_EndToEndIntegration() {
        AppUser user = new AppUser(null, "User A", "userA@example.com", List.of());
        AppUser savedUser = adminService.insertNewUser(user);
        assertNotNull(savedUser.getUserId(), "The user should have a generated ID after insertion");

        // Verify that a login log entry is created.
        List<UserLogs> logs = userLogsRepository.findAll();
        assertFalse(logs.isEmpty(), "A login log entry should be created for the new user");
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
            adminService.insertNewUser(null);
            fail("Expected IllegalArgumentException for null input");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).contains("AppUser must not be null");
        }
    }

    // =========================================================================
    // ✅ 5. insertNewUser's Caching and Performance Tests (if applicable)
    // =========================================================================
    // (These tests are not applicable unless caching is implemented in the service layer.)

    // =========================================================================
    // Tests for getAdminInfo Function
    // =========================================================================

    // =========================================================================
    // ✅ 1. getAdminInfo's Basic Functionality Tests (Unit Test)
    // =========================================================================
    /**
     * **getAdminInfo - Basic Retrieval Test**
     * Verify that getAdminInfo returns an AdminDTO with a non-empty user list and a correct total count.
     */
    @Test
    @Transactional
    void testGetAdminInfo_BasicFunctionality() {
        AppUser user = new AppUser();
        user.setUserName("Admin User");
        user.setEmail("adminuser@example.com");
        adminService.insertNewUser(user);

        AdminDTO adminInfo = adminService.getAdminInfo();
        assertNotNull(adminInfo, "AdminDTO should not be null");
        assertTrue(adminInfo.getTotalUsers() > 0, "Total users count should be greater than zero");
        assertThat(adminInfo.getUsers()).isNotEmpty();
    }

    // =========================================================================
    // ✅ 2. getAdminInfo's Input Validation Tests (Unit Test)
    // =========================================================================
    /**
     * **getAdminInfo - Empty Repository Test**
     * Verify that getAdminInfo returns an AdminDTO with an empty user list and a total user count of 0 when no users exist.
     */
    @Test
    @Transactional
    void testGetAdminInfo_InputValidation_EmptyRepository() {
        // First, delete all login logs to avoid foreign key constraint issues.
        userLogsRepository.deleteAll();
        // Then delete all users.
        usersRepository.deleteAll();
        
        AdminDTO adminInfo = adminService.getAdminInfo();
        assertNotNull(adminInfo, "AdminDTO should not be null even if the repository is empty");
        assertEquals(0, adminInfo.getTotalUsers(), "Total users count should be 0 when no users exist");
        assertThat(adminInfo.getUsers()).isEmpty();
    }

    // =========================================================================
    // ✅ 3. getAdminInfo's Integration Tests
    // =========================================================================
    /**
     * **getAdminInfo - End-to-End Integration Test**
     * Verify that the admin info method retrieves all users and returns the correct total count.
     */
    @Test
    @Transactional
    void testGetAdminInfo_EndToEndIntegration() {
        AppUser user1 = new AppUser(null, "User A", "userA@example.com", List.of());
        AppUser user2 = new AppUser(null, "User B", "userB@example.com", List.of());

        adminService.insertNewUser(user1);
        adminService.insertNewUser(user2);

        AdminDTO adminInfo = adminService.getAdminInfo();
        assertNotNull(adminInfo, "AdminDTO should not be null");
        assertTrue(adminInfo.getTotalUsers() >= 2, "Total users count should be at least 2");
        assertThat(adminInfo.getUsers()).extracting(AppUser::getEmail)
                                        .contains("userA@example.com", "userB@example.com");
    }

    // =========================================================================
    // ✅ 4. getAdminInfo's Edge Case and Exception Handling Tests
    // =========================================================================
    /**
     * **getAdminInfo - Error Handling Test**
     * Verify that getAdminInfo gracefully handles errors and does not throw unexpected exceptions.
     */
    @Test
    void testGetAdminInfo_ErrorHandling() {
        try {
            AdminDTO adminInfo = adminService.getAdminInfo();
            assertNotNull(adminInfo, "AdminDTO should not be null when retrieving admin info");
        } catch (Exception e) {
            fail("getAdminInfo should not throw an exception under normal circumstances");
        }
    }

    // =========================================================================
    // ✅ 5. getAdminInfo's Caching and Performance Tests (if applicable)
    // =========================================================================
    // (These tests are not applicable unless caching is implemented in the service layer.)
}
