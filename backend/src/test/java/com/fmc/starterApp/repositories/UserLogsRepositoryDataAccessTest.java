package com.fmc.starterApp.repositories;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.fmc.starterApp.models.entity.AppUser;
import com.fmc.starterApp.models.entity.UserLogs;

/**
 * Integration tests for {@link UserLogsRepository}, verifying CRUD operations, constraints, and data retrieval accuracy.
 *
 * <p>This test class employs an in-memory H2 database configured for PostgreSQL compatibility to simulate production-like
 * database behavior. It covers essential scenarios from the core repository testing checklist:
 * <ul>
 *   <li><strong>Basic CRUD Tests:</strong> Create, Read, Update, and Delete operations.</li>
 *   <li><strong>Constraint Validation Tests:</strong> Unique, Non-null, and Length constraints.</li>
 *   <li><strong>Query Method Tests:</strong> Retrieval by ID and (if defined) custom queries.</li>
 *   <li><strong>Bulk Operations Tests:</strong> Testing saveAll and deleteAll, including edge cases with empty lists.</li>
 *   <li><strong>Transactional Tests:</strong> Rollback scenarios and verifying the database state remains unchanged.</li>
 *   <li><strong>Pagination and Sorting Tests:</strong> Using PageRequest and Sort in various directions.</li>
 *   <li><strong>Auditing and Optional Field Tests:</strong> Verifying that fields like loginTime are handled correctly.</li>
 *   <li><strong>Edge Case and Exception Handling Tests:</strong> Testing boundary conditions and unexpected inputs.</li>
 * </ul>
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class UserLogsRepositoryDataAccessTest {

    @Autowired
    private UserLogsRepository userLogsRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    // =========================================================================
    // ✅ 1. Basic CRUD Tests
    // =========================================================================

    // ---------- Create Operation Tests ----------
    /**
     * **Save new entity**
     * Verify that a new UserLogs entity is correctly persisted.
     */
    @Test
    void testCreateUserLog() {
        // Inline: Create and persist a dummy AppUser.
        AppUser user = new AppUser();
        user.setUserName("TestAppUser" + System.nanoTime());
        user.setEmail("testappuser" + System.nanoTime() + "@example.com");
        user = entityManager.persistAndFlush(user);

        UserLogs log = new UserLogs(user, new Date(), null);
        UserLogs savedLog = userLogsRepository.save(log);
        assertNotNull(savedLog.getId(), "The log entry should have a generated ID after save.");
    }

    // ---------- Read Operation Tests ----------
    /**
     * **Find by ID (positive case)**
     * Confirm retrieval by primary key returns the correct UserLogs entity.
     */
    @Test
    void testFindByIdPositive() {
        AppUser user = new AppUser();
        user.setUserName("TestAppUser" + System.nanoTime());
        user.setEmail("testappuser" + System.nanoTime() + "@example.com");
        user = entityManager.persistAndFlush(user);

        UserLogs log = new UserLogs(user, new Date(), null);
        UserLogs savedLog = userLogsRepository.save(log);

        Optional<UserLogs> foundLog = userLogsRepository.findById(savedLog.getId());
        assertTrue(foundLog.isPresent(), "UserLogs should be found by its ID.");
    }

    /**
     * **Find by ID (negative case)**
     * Confirm that querying a non-existent ID returns an empty result.
     */
    @Test
    void testFindByIdNegative() {
        Optional<UserLogs> foundLog = userLogsRepository.findById(-1L);
        assertFalse(foundLog.isPresent(), "Non-existent ID should return an empty Optional.");
    }

    // ---------- Update Operation Tests ----------
    /**
     * **Update existing entity**
     * Verify that changes to a UserLogs entity are correctly persisted.
     */
    @Test
    void testUpdateUserLog() {
        AppUser user = new AppUser();
        user.setUserName("TestAppUser" + System.nanoTime());
        user.setEmail("testappuser" + System.nanoTime() + "@example.com");
        user = entityManager.persistAndFlush(user);

        UserLogs log = new UserLogs(user, new Date(), null);
        UserLogs savedLog = userLogsRepository.save(log);

        Date newLoginTime = new Date(System.currentTimeMillis() + 1000);
        savedLog.setLoginTime(newLoginTime);
        userLogsRepository.saveAndFlush(savedLog);

        UserLogs updatedLog = userLogsRepository.findById(savedLog.getId()).get();
        assertEquals(newLoginTime, updatedLog.getLoginTime(), "Login time should be updated.");
    }

    /**
     * **Update non-existent entity**
     * Verify that attempting to update a UserLogs entity that doesn't exist behaves as expected.
     */
    @Test
    void testUpdateNonExistentUserLog() {
        UserLogs log = new UserLogs();
        log.setId(999999L); // Non-existent ID
        AppUser user = new AppUser();
        user.setUserName("TestAppUser" + System.nanoTime());
        user.setEmail("testappuser" + System.nanoTime() + "@example.com");
        user = entityManager.persistAndFlush(user);

        log.setAppUser(user);
        log.setLoginTime(new Date());

        UserLogs savedLog = userLogsRepository.save(log);
        assertNotNull(savedLog.getId(), "A new record should be created if the entity did not previously exist.");
    }

    // ---------- Delete Operation Tests ----------
    /**
     * **Delete existing entity**
     * Confirm that a UserLogs entity is removed from the repository after deletion.
     */
    @Test
    void testDeleteUserLog() {
        AppUser user = new AppUser();
        user.setUserName("TestAppUser" + System.nanoTime());
        user.setEmail("testappuser" + System.nanoTime() + "@example.com");
        user = entityManager.persistAndFlush(user);

        UserLogs log = new UserLogs(user, new Date(), null);
        userLogsRepository.save(log);

        userLogsRepository.delete(log);
        Optional<UserLogs> deletedLog = userLogsRepository.findById(log.getId());
        assertFalse(deletedLog.isPresent(), "The log entry should be deleted.");
    }

    /**
     * **Delete non-existent entity**
     * Confirm that attempting to delete a non-persisted UserLogs entity does not throw unexpected exceptions.
     */
    @Test
    void testDeleteNonExistentUserLog() {
        UserLogs nonExistentLog = new UserLogs();
        AppUser user = new AppUser();
        user.setUserName("TestAppUser" + System.nanoTime());
        user.setEmail("testappuser" + System.nanoTime() + "@example.com");
        user = entityManager.persistAndFlush(user);

        nonExistentLog.setAppUser(user);
        nonExistentLog.setLoginTime(new Date());
        assertDoesNotThrow(() -> userLogsRepository.delete(nonExistentLog), "Deleting a non-persisted entity should be a no-op.");
    }

    // =========================================================================
    // ✅ 2. Constraint Validation Tests
    // =========================================================================

    /**
     * **Non-null Constraint Violation**
     * Verify that null values for required fields (appUser, loginTime) are rejected.
     */
    @Test
    void testNonNullConstraintViolation() {
        UserLogs log = new UserLogs();
        assertThrows(DataIntegrityViolationException.class, () -> userLogsRepository.saveAndFlush(log));
    }

    // =========================================================================
    // ✅ 3. Query Method Tests (including Custom Queries)
    // =========================================================================
    // (Custom query tests can be added here if custom methods are defined in UserLogsRepository.)

    // =========================================================================
    // ✅ 4. Bulk Operations Tests
    // =========================================================================

    /**
     * **SaveAll and DeleteAll entities**
     * Validate that bulk insertions and deletions function correctly.
     */
    @Test
    void testBulkOperations() {
        AppUser user = new AppUser();
        user.setUserName("TestAppUser" + System.nanoTime());
        user.setEmail("testappuser" + System.nanoTime() + "@example.com");
        user = entityManager.persistAndFlush(user);

        UserLogs log1 = new UserLogs(user, new Date(), null);
        UserLogs log2 = new UserLogs(user, new Date(), null);
        
        userLogsRepository.saveAll(List.of(log1, log2));
        List<UserLogs> logs = userLogsRepository.findAll();
        assertThat(logs.size()).isGreaterThanOrEqualTo(2);
        
        userLogsRepository.deleteAll(List.of(log1, log2));
        Optional<UserLogs> opt1 = userLogsRepository.findById(log1.getId());
        Optional<UserLogs> opt2 = userLogsRepository.findById(log2.getId());
        assertThat(opt1).isNotPresent();
        assertThat(opt2).isNotPresent();
    }

    /**
     * **Bulk Operations Edge Case Test**
     * Verify that calling bulk operations with an empty list behaves as expected.
     */
    @Test
    void testBulkOperationsEmpty() {
        List<UserLogs> emptyList = new ArrayList<>();
        assertDoesNotThrow(() -> userLogsRepository.saveAll(emptyList));
        assertDoesNotThrow(() -> userLogsRepository.deleteAll(emptyList));
    }

    // =========================================================================
    // ✅ 5. Transactional Tests
    // =========================================================================

    /**
     * **Transactional Rollback**
     * Simulate an exception to verify that the repository rolls back the transaction,
     * leaving the database unchanged.
     *
     * In this test, we disable the default transaction so that we can manually manage transactions.
     * We use TransactionTemplate inline to persist an AppUser and update a UserLogs record in a new transaction
     * that we then mark for rollback.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testTransactionalRollback() {
        // Inline: Persist an AppUser in its own transaction.
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        AppUser user = txTemplate.execute(status -> {
            AppUser u = new AppUser();
            u.setUserName("TestAppUser" + System.nanoTime());
            u.setEmail("testappuser" + System.nanoTime() + "@example.com");
            return entityManager.persistAndFlush(u);
        });

        // Persist a UserLogs record.
        UserLogs log = new UserLogs(user, new Date(), null);
        UserLogs savedLog = userLogsRepository.saveAndFlush(log);
        Long logId = savedLog.getId();
        Date originalLoginTime = savedLog.getLoginTime();

        // Execute an update in a new transaction and mark it for rollback.
        txTemplate.execute(status -> {
            UserLogs logToUpdate = userLogsRepository.findById(logId)
                    .orElseThrow(() -> new RuntimeException("Log not found"));
            // Update loginTime by adding 10 seconds.
            logToUpdate.setLoginTime(new Date(System.currentTimeMillis() + 10000));
            userLogsRepository.saveAndFlush(logToUpdate);
            status.setRollbackOnly();
            return null;
        });

        // Retrieve the entity in a new transaction to verify that the update was rolled back.
        UserLogs fetchedLog = txTemplate.execute(status ->
            userLogsRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Log not found"))
        );
        assertEquals(originalLoginTime, fetchedLog.getLoginTime(), "Login time should remain unchanged after rollback");
    }

    // =========================================================================
    // ✅ 6. Pagination and Sorting Tests
    // =========================================================================

    /**
     * **Pagination and Sorting (Ascending)**
     * Validate that pagination and ascending sorting by loginTime work as expected.
     */
    @Test
    void testPaginationAndSortingAscending() {
        AppUser user = new AppUser();
        user.setUserName("TestAppUser" + System.nanoTime());
        user.setEmail("testappuser" + System.nanoTime() + "@example.com");
        user = entityManager.persistAndFlush(user);
        for (int i = 1; i <= 10; i++) {
            UserLogs log = new UserLogs(user, new Date(System.currentTimeMillis() + i * 1000L), null);
            userLogsRepository.save(log);
        }
        Pageable pageable = PageRequest.of(0, 5, Sort.by("loginTime").ascending());
        Page<UserLogs> page = userLogsRepository.findAll(pageable);
        assertThat(page.getContent()).hasSize(5);
        
        List<UserLogs> sortedLogs = page.getContent();
        for (int i = 1; i < sortedLogs.size(); i++) {
            assertTrue(sortedLogs.get(i - 1).getLoginTime().compareTo(sortedLogs.get(i).getLoginTime()) <= 0);
        }
    }

    /**
     * **Sorting (Descending)**
     * Confirm that sorting by loginTime in descending order returns results in the expected order.
     */
    @Test
    void testSortingDescending() {
        AppUser user = new AppUser();
        user.setUserName("TestAppUser" + System.nanoTime());
        user.setEmail("testappuser" + System.nanoTime() + "@example.com");
        user = entityManager.persistAndFlush(user);
        userLogsRepository.deleteAll();
        for (int i = 1; i <= 5; i++) {
            UserLogs log = new UserLogs(user, new Date(System.currentTimeMillis() + i * 1000L), null);
            userLogsRepository.save(log);
        }
        Pageable pageable = PageRequest.of(0, 5, Sort.by("loginTime").descending());
        Page<UserLogs> page = userLogsRepository.findAll(pageable);
        List<UserLogs> sortedLogs = page.getContent();
        for (int i = 1; i < sortedLogs.size(); i++) {
            assertTrue(sortedLogs.get(i - 1).getLoginTime().compareTo(sortedLogs.get(i).getLoginTime()) >= 0);
        }
    }

    // =========================================================================
    // ✅ 7. Auditing and Optional Field Tests
    // =========================================================================

    /**
     * **Login Time Preservation**
     * Verify that the loginTime field is preserved as provided.
     */
    @Test
    void testLoginTimePreservation() {
        AppUser user = new AppUser();
        user.setUserName("TestAppUser" + System.nanoTime());
        user.setEmail("testappuser" + System.nanoTime() + "@example.com");
        user = entityManager.persistAndFlush(user);
        Date now = new Date();
        UserLogs log = new UserLogs(user, now, null);
        UserLogs savedLog = userLogsRepository.save(log);
        assertEquals(now, savedLog.getLoginTime(), "The loginTime should be preserved as provided.");
    }

    // =========================================================================
    // ✅ 8. Edge Case and Exception Handling Tests
    // =========================================================================

    /**
     * **Save Null Entity Exception Test**
     * Verify that passing a null entity to the save method throws an InvalidDataAccessApiUsageException.
     */
    @Test
    void testSaveNullEntity() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> userLogsRepository.save(null));
    }

    /**
     * **Find by ID with Null Exception Test**
     * Verify that passing a null ID to findById throws an InvalidDataAccessApiUsageException.
     */
    @Test
    void testFindByIdWithNull() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> userLogsRepository.findById(null));
    }

    /**
     * **Invalid Pagination Parameters**
     * Verify that negative pagination parameters throw an IllegalArgumentException.
     */
    @Test
    void testInvalidPaginationParameters() {
        assertThrows(IllegalArgumentException.class, () -> {
            Pageable pageable = PageRequest.of(-1, 5, Sort.by("loginTime").ascending());
            userLogsRepository.findAll(pageable);
        });
    }
}
