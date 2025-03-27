package com.fmc.starterApp.repositories;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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

import com.fmc.starterApp.models.entity.User2;

/**
 * Integration tests for {@link User2Repository}, verifying CRUD operations, constraints, and data retrieval accuracy.
 *
 * <p>This test class employs an in-memory H2 database configured for PostgreSQL compatibility to simulate production-like
 * database behavior. It covers essential scenarios from the core repository testing checklist:
 * <ul>
 *   <li><strong>Basic CRUD Tests:</strong> Create, Read, Update, and Delete operations.</li>
 *   <li><strong>Constraint Validation Tests:</strong> Unique, Non-null, and Length constraints.</li>
 *   <li><strong>Query Method Tests:</strong> Derived queries via {@code findByKeycloakId} (custom query tests are included).</li>
 *   <li><strong>Bulk Operations Tests:</strong> Testing saveAll and deleteAll, including edge cases with empty lists.</li>
 *   <li><strong>Transactional Tests:</strong> Rollback scenarios and verifying the database state remains unchanged.</li>
 *   <li><strong>Pagination and Sorting Tests:</strong> Using PageRequest and Sort in various directions.</li>
 *   <li><strong>Auditing and Optional Field Tests:</strong> Verifying {@code createdAt} and handling of optional fields.</li>
 *   <li><strong>Edge Case and Exception Handling Tests:</strong> Testing boundary conditions and unexpected inputs.</li>
 * </ul>
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class User2RepositoryDataAccessTest {

    @Autowired
    private User2Repository user2Repository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    // =========================================================================
    // ✅ 1. Basic CRUD Tests
    // =========================================================================

    // ---------- Create Operation Tests ----------
    /**
     * **Save new entity**  
     * Verify that a new entity is correctly persisted.
     */
    @Test
    void testCreateUser() {
        User2 user = new User2();
        user.setKeycloakId("kc-create");
        user.setUsername("createUser");
        user.setEmail("create@example.com");
        User2 savedUser = user2Repository.save(user);
        assertNotNull(savedUser.getUserId(), "The user should have a generated ID after save.");
        assertThat(savedUser.getKeycloakId()).isEqualTo("kc-create");
    }

    // ---------- Read Operation Tests ----------
    /**
     * **Find by ID (positive case)**  
     * Confirm retrieval by primary key returns the correct entity.
     */
    @Test
    void testFindByIdPositive() {
        User2 user = new User2();
        user.setKeycloakId("kc-find-id");
        user.setUsername("findByIdUser");
        user.setEmail("findbyid@example.com");
        User2 savedUser = user2Repository.save(user);
        Optional<User2> foundUser = user2Repository.findById(savedUser.getUserId());
        assertTrue(foundUser.isPresent(), "User should be found by its ID.");
        assertThat(foundUser.get().getUsername()).isEqualTo("findByIdUser");
    }

    /**
     * **Find by ID (negative case)**  
     * Confirm that querying a non-existent ID returns an empty result.
     */
    @Test
    void testFindByIdNegative() {
        Optional<User2> foundUser = user2Repository.findById(-1L);
        assertThat(foundUser).isNotPresent();
    }

    /**
     * **Find by unique field (negative case)**  
     * Verify that querying with a non-existent keycloakId returns empty.
     */
    @Test
    void testFindByKeycloakIdNotFound() {
        Optional<User2> userOpt = user2Repository.findByKeycloakId("nonexistent");
        assertThat(userOpt).isNotPresent();
    }

    /**
     * **Find by unique field (negative case with null)**  
     * Ensure that querying with a null keycloakId yields an empty result.
     */
    @Test
    void testFindByKeycloakIdWithNull() {
        Optional<User2> userOpt = user2Repository.findByKeycloakId(null);
        assertThat(userOpt).isNotPresent();
    }

    // ---------- Update Operation Tests ----------
    /**
     * **Update existing entity**  
     * Verify that changes to an entity are correctly persisted.
     */
    @Test
    void testUpdateUser() {
        User2 user = new User2();
        user.setKeycloakId("kc-update");
        user.setUsername("initialUser");
        user.setEmail("initial@example.com");
        user2Repository.save(user);
        user.setUsername("updatedUser");
        user.setEmail("updated@example.com");
        user2Repository.saveAndFlush(user);
        User2 updatedUser = user2Repository.findByKeycloakId("kc-update").get();
        assertThat(updatedUser.getUsername()).isEqualTo("updatedUser");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }

    /**
     * **Update non-existent entity**  
     * Verify that attempting to update an entity that doesn't exist behaves as expected.
     */
    @Test
    void testUpdateNonExistentUser() {
        User2 user = new User2();
        user.setUserId(999999L);
        user.setKeycloakId("kc-nonexistent");
        user.setUsername("nonexistentUser");
        user.setEmail("nonexistent@example.com");
        User2 savedUser = user2Repository.save(user);
        assertNotNull(savedUser.getUserId(), "A new record should be created if the entity did not previously exist.");
    }

    // ---------- Delete Operation Tests ----------
    /**
     * **Delete existing entity**  
     * Confirm that an entity is removed from the repository after deletion.
     */
    @Test
    void testDeleteUser() {
        User2 user = new User2();
        user.setKeycloakId("kc-delete");
        user.setUsername("deleteUser");
        user.setEmail("delete@example.com");
        user2Repository.save(user);
        user2Repository.delete(user);
        Optional<User2> deletedUserOpt = user2Repository.findByKeycloakId("kc-delete");
        assertThat(deletedUserOpt).isNotPresent();
    }

    /**
     * **Delete non-existent entity**  
     * Confirm that attempting to delete a non-persisted entity does not throw unexpected exceptions.
     */
    @Test
    void testDeleteNonExistentUser() {
        User2 nonExistentUser = new User2();
        nonExistentUser.setKeycloakId("kc-nonexistent");
        nonExistentUser.setUsername("nonexistentUser");
        nonExistentUser.setEmail("nonexistent@example.com");
        user2Repository.delete(nonExistentUser);
    }

    // =========================================================================
    // ✅ 2. Constraint Validation Tests
    // =========================================================================

    /**
     * **Unique Constraint Violation**  
     * Ensure that inserting duplicate values for keycloakId throws a DataIntegrityViolationException.
     */
    @Test
    void testUniqueConstraintOnKeycloakId() {
        User2 user1 = new User2();
        user1.setKeycloakId("duplicate");
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user2Repository.save(user1);
        User2 user2 = new User2();
        user2.setKeycloakId("duplicate");
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        assertThrows(DataIntegrityViolationException.class, () -> user2Repository.saveAndFlush(user2));
    }

    /**
     * **Non-null Constraint Violation**  
     * Verify that null values for required fields are rejected.
     */
    @Test
    void testNonNullConstraintViolation() {
        User2 user = new User2();
        assertThrows(DataIntegrityViolationException.class, () -> user2Repository.saveAndFlush(user));
    }

    /**
     * **Length Constraint Violation**  
     * Confirm that values exceeding defined length limits are not persisted.
     */
    @Test
    void testLengthConstraintViolation() {
        User2 user = new User2();
        user.setKeycloakId("kc-length");
        user.setUsername("a".repeat(51)); // 51 characters (limit is 50)
        user.setEmail("length@example.com");
        assertThrows(DataIntegrityViolationException.class, () -> user2Repository.saveAndFlush(user));
    }

    // =========================================================================
    // ✅ 3. Query Method Tests
    // =========================================================================

    /**
     * **Find by unique field (positive case)**  
     * Explicitly verify that the derived query method returns the correct entity.
     */
    @Test
    void testFindByKeycloakIdPositive() {
        User2 user = new User2();
        user.setKeycloakId("kc-positive");
        user.setUsername("positiveUser");
        user.setEmail("positive@example.com");
        user2Repository.save(user);
        Optional<User2> retrievedUser = user2Repository.findByKeycloakId("kc-positive");
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getUsername()).isEqualTo("positiveUser");
    }
    // (Custom query tests can be added here if additional query methods are defined.)

    // =========================================================================
    // ✅ 4. Bulk Operations Tests
    // =========================================================================

    /**
     * **SaveAll and DeleteAll entities**  
     * Validate that bulk insertions and deletions function correctly.
     */
    @Test
    void testBulkOperations() {
        User2 user1 = new User2(null, "kc-bulk1", "Bulk", "User1", "Bulk User1", "bulk1", "bulk1@example.com", LocalDateTime.now());
        User2 user2 = new User2(null, "kc-bulk2", "Bulk", "User2", "Bulk User2", "bulk2", "bulk2@example.com", LocalDateTime.now());
        user2Repository.saveAll(List.of(user1, user2));
        List<User2> users = user2Repository.findAll();
        assertThat(users.size()).isGreaterThanOrEqualTo(2);
        user2Repository.deleteAll(List.of(user1, user2));
        Optional<User2> userOpt1 = user2Repository.findByKeycloakId("kc-bulk1");
        Optional<User2> userOpt2 = user2Repository.findByKeycloakId("kc-bulk2");
        assertThat(userOpt1).isNotPresent();
        assertThat(userOpt2).isNotPresent();
    }

    /**
     * **Bulk Operations Edge Case Test**  
     * Verify that calling bulk operations with an empty list behaves as expected.
     */
    @Test
    void testBulkOperationsEmpty() {
        List<User2> emptyList = new ArrayList<>();
        assertDoesNotThrow(() -> user2Repository.saveAll(emptyList));
        assertDoesNotThrow(() -> user2Repository.deleteAll(emptyList));
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
     * We use TransactionTemplate inline to update a User2 entity in a new transaction, mark it for rollback,
     * and then verify that the changes were not committed.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testTransactionalRollback() {
        // Persist a User2 entity.
        User2 user = new User2();
        user.setKeycloakId("kc-tx");
        user.setUsername("txUser");
        user.setEmail("tx@example.com");
        User2 savedUser = user2Repository.save(user);
        Long userId = savedUser.getUserId();
        String originalUsername = savedUser.getUsername();

        // Use TransactionTemplate to run an update in a new transaction and mark it for rollback.
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.execute(status -> {
            User2 userToUpdate = user2Repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            // Update the username.
            userToUpdate.setUsername("updatedTxUser");
            user2Repository.saveAndFlush(userToUpdate);
            // Mark this transaction for rollback.
            status.setRollbackOnly();
            return null;
        });

        // After the rollback, verify that the state in the database remains unchanged.
        User2 fetchedUser = user2Repository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        assertThat(fetchedUser.getUsername()).isEqualTo(originalUsername);
    }

    // =========================================================================
    // ✅ 6. Pagination and Sorting Tests
    // =========================================================================

    /**
     * **Pagination and Sorting (Ascending)**
     * Validate that pagination and ascending sorting by username work as expected.
     */
    @Test
    void testPaginationAndSortingAscending() {
        for (int i = 1; i <= 10; i++) {
            User2 user = new User2();
            user.setKeycloakId("kc-page-" + i);
            user.setUsername("user" + i);
            user.setEmail("user" + i + "@example.com");
            user2Repository.save(user);
        }
        Pageable pageable = PageRequest.of(0, 5, Sort.by("username").ascending());
        Page<User2> page = user2Repository.findAll(pageable);
        assertThat(page.getContent()).hasSize(5);
        List<User2> sortedUsers = page.getContent();
        for (int i = 1; i < sortedUsers.size(); i++) {
            assertThat(sortedUsers.get(i - 1).getUsername().compareTo(sortedUsers.get(i).getUsername()))
                .isLessThanOrEqualTo(0);
        }
    }

    /**
     * **Sorting (Descending)**
     * Confirm that sorting by username in descending order returns results in the expected order.
     */
    @Test
    void testSortingDescending() {
        user2Repository.deleteAll();
        for (int i = 1; i <= 5; i++) {
            User2 user = new User2();
            user.setKeycloakId("kc-sort-" + i);
            user.setUsername("user" + i);
            user.setEmail("user" + i + "@example.com");
            user2Repository.save(user);
        }
        Pageable pageable = PageRequest.of(0, 5, Sort.by("username").descending());
        Page<User2> page = user2Repository.findAll(pageable);
        List<User2> sortedUsers = page.getContent();
        for (int i = 1; i < sortedUsers.size(); i++) {
            assertThat(sortedUsers.get(i - 1).getUsername().compareTo(sortedUsers.get(i).getUsername()))
                .isGreaterThanOrEqualTo(0);
        }
    }

    // =========================================================================
    // ✅ 7. Auditing and Optional Field Tests
    // =========================================================================

    /**
     * **Creation Timestamp**
     * Verify that the creation timestamp (createdAt) is automatically populated.
     */
    @Test
    void testCreationTimestamp() {
        User2 user = new User2();
        user.setKeycloakId("kc-timestamp");
        user.setUsername("timestampUser");
        user.setEmail("timestamp@example.com");
        User2 savedUser = user2Repository.save(user);
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    /**
     * **Optional Field Handling**
     * Verify that entities with optional fields set to null are handled correctly.
     */
    @Test
    void testSavingUserWithOptionalFieldsAsNull() {
        User2 user = new User2();
        user.setKeycloakId("kc-optional");
        user.setUsername("optionalUser");
        user.setEmail("optional@example.com");
        user.setFirstName(null);
        user.setLastName(null);
        user.setFullName(null);
        User2 savedUser = user2Repository.save(user);
        assertThat(savedUser.getFirstName()).isNull();
        assertThat(savedUser.getLastName()).isNull();
        assertThat(savedUser.getFullName()).isNull();
    }

    // =========================================================================
    // ✅ 8. Edge Case and Exception Handling Tests
    // =========================================================================

    /**
     * **Max-length Strings**
     * Confirm that fields respect maximum allowed lengths.
     */
    @Test
    void testMaxLengthConstraints() {
        String longUsername = "a".repeat(50);
        User2 user = new User2();
        user.setKeycloakId("kc-long");
        user.setUsername(longUsername);
        user.setEmail("long@example.com");
        User2 savedUser = user2Repository.save(user);
        assertThat(savedUser.getUsername().length()).isEqualTo(50);
    }

    /**
     * **Empty Strings**
     * Validate that empty string values are handled correctly.
     */
    @Test
    void testEmptyStringHandling() {
        User2 user = new User2();
        user.setKeycloakId("kc-empty");
        user.setUsername("");
        user.setEmail("empty@example.com");
        User2 savedUser = user2Repository.save(user);
        assertThat(savedUser.getUsername()).isEqualTo("");
    }

    /**
     * **Special Characters and Unicode**
     * Confirm that special and Unicode characters are persisted correctly.
     */
    @Test
    void testSpecialCharacters() {
        User2 user = new User2();
        user.setKeycloakId("kc-special");
        user.setUsername("特殊字符");
        user.setEmail("special@example.com");
        User2 savedUser = user2Repository.save(user);
        assertThat(savedUser.getUsername()).isEqualTo("特殊字符");
    }

    /**
     * **Save Null Entity Exception Test**
     * Verify that passing a null entity to the save method throws an InvalidDataAccessApiUsageException.
     */
    @Test
    void testSaveNullEntity() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> user2Repository.save(null));
    }

    /**
     * **Find by ID with Null Exception Test**
     * Verify that passing a null ID to findById throws an InvalidDataAccessApiUsageException.
     */
    @Test
    void testFindByIdWithNull() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> user2Repository.findById(null));
    }

    /**
     * **Invalid Pagination Parameters**
     * Verify that negative pagination parameters throw an IllegalArgumentException.
     */
    @Test
    void testInvalidPaginationParameters() {
        assertThrows(IllegalArgumentException.class, () -> {
            Pageable pageable = PageRequest.of(-1, 5, Sort.by("username").ascending());
            user2Repository.findAll(pageable);
        });
    }
}
