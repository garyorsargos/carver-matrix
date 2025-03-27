package com.fmc.starterApp.repositories;

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

import com.fmc.starterApp.models.entity.AppUser;

/**
 * Integration tests for {@link UsersRepository}, verifying CRUD operations, constraints, relationship behavior,
 * and data retrieval accuracy for the AppUser entity.
 *
 * <p>This test class employs an in-memory H2 database configured for PostgreSQL compatibility to simulate production-like
 * behavior. It follows the Repository Testing Checklist:
 * <ul>
 *   <li><strong>Basic CRUD Tests:</strong> Create, Read, Update, and Delete operations.</li>
 *   <li><strong>Constraint Validation Tests:</strong> Unique, Non-null, and Length constraints.</li>
 *   <li><strong>Query Method Tests:</strong> Retrieval by ID (and by unique fields if custom methods are added).</li>
 *   <li><strong>Bulk Operations Tests:</strong> Testing saveAll and deleteAll, including edge cases with empty lists.</li>
 *   <li><strong>Transactional Tests:</strong> Rollback scenarios using TransactionTemplate to verify that the database remains unchanged.</li>
 *   <li><strong>Pagination and Sorting Tests:</strong> Using PageRequest and Sort in various directions.</li>
 *   <li><strong>Auditing and Optional Field Tests:</strong> Verifying that optional fields (e.g., the userTimes collection) behave correctly.</li>
 *   <li><strong>Edge Case and Exception Handling Tests:</strong> Testing boundary conditions (max lengths, empty strings, special characters) and invalid inputs.</li>
 * </ul>
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class UserRepositoryDataAccessTest {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    // =========================================================================
    // ✅ 1. Basic CRUD Tests
    // =========================================================================

    // ---------- Create Operation Tests ----------
    /**
     * **Save new entity**
     * Verify that a new AppUser entity is correctly persisted.
     */
    @Test
    void testCreateUser() {
        AppUser user = new AppUser();
        user.setUserName("John Doe");
        user.setEmail("johndoe@example.com");
        AppUser savedUser = usersRepository.save(user);
        assertNotNull(savedUser.getUserId(), "The user should have a generated ID after save.");
        assertThat(savedUser.getUserName()).isEqualTo("John Doe");
    }

    // ---------- Read Operation Tests ----------
    /**
     * **Find by ID (positive case)**
     * Confirm that retrieval by primary key returns the correct AppUser entity.
     */
    @Test
    void testFindByIdPositive() {
        AppUser user = new AppUser();
        user.setUserName("Jane Doe");
        user.setEmail("janedoe@example.com");
        AppUser savedUser = usersRepository.save(user);
        Optional<AppUser> foundUser = usersRepository.findById(savedUser.getUserId());
        assertTrue(foundUser.isPresent(), "User should be found by its ID.");
        assertThat(foundUser.get().getUserName()).isEqualTo("Jane Doe");
    }

    /**
     * **Find by ID (negative case)**
     * Confirm that querying a non-existent ID returns an empty result.
     */
    @Test
    void testFindByIdNegative() {
        Optional<AppUser> foundUser = usersRepository.findById(-1L);
        assertThat(foundUser).isNotPresent();
    }

    // ---------- Update Operation Tests ----------
    /**
     * **Update existing entity**
     * Verify that changes to an AppUser entity are correctly persisted.
     */
    @Test
    void testUpdateUser() {
        AppUser user = new AppUser();
        user.setUserName("Initial Name");
        user.setEmail("initial@example.com");
        AppUser savedUser = usersRepository.save(user);
        savedUser.setUserName("Updated Name");
        AppUser updatedUser = usersRepository.saveAndFlush(savedUser);
        assertThat(updatedUser.getUserName()).isEqualTo("Updated Name");
    }

    /**
     * **Update non-existent entity**
     * Verify that attempting to update an entity that doesn't exist behaves as expected.
     */
    @Test
    void testUpdateNonExistentUser() {
        AppUser user = new AppUser();
        user.setUserId(999999L);
        user.setUserName("Nonexistent User");
        user.setEmail("nonexistent@example.com");
        AppUser savedUser = usersRepository.save(user);
        assertNotNull(savedUser.getUserId(), "A new record should be created if the entity did not previously exist.");
    }

    // ---------- Delete Operation Tests ----------
    /**
     * **Delete existing entity**
     * Confirm that an AppUser entity is removed from the repository after deletion.
     */
    @Test
    void testDeleteUser() {
        AppUser user = new AppUser();
        user.setUserName("Delete User");
        user.setEmail("delete@example.com");
        AppUser savedUser = usersRepository.save(user);
        usersRepository.delete(savedUser);
        Optional<AppUser> deletedUser = usersRepository.findById(savedUser.getUserId());
        assertThat(deletedUser).isNotPresent();
    }

    /**
     * **Delete non-existent entity**
     * Confirm that attempting to delete a non-persisted entity does not throw unexpected exceptions.
     */
    @Test
    void testDeleteNonExistentUser() {
        AppUser nonExistentUser = new AppUser();
        nonExistentUser.setUserName("Ghost User");
        nonExistentUser.setEmail("ghost@example.com");
        assertDoesNotThrow(() -> usersRepository.delete(nonExistentUser));
    }

    // =========================================================================
    // ✅ 2. Constraint Validation Tests
    // =========================================================================

    /**
     * **Unique Constraint Violation**
     * Ensure that inserting duplicate values for email throws a DataIntegrityViolationException.
     */
    @Test
    void testUniqueConstraintOnEmail() {
        AppUser user1 = new AppUser();
        user1.setUserName("User One");
        user1.setEmail("duplicate@example.com");
        usersRepository.save(user1);
        AppUser user2 = new AppUser();
        user2.setUserName("User Two");
        user2.setEmail("duplicate@example.com");
        assertThrows(DataIntegrityViolationException.class, () -> usersRepository.saveAndFlush(user2));
    }

    /**
     * **Non-null Constraint Violation**
     * Verify that null values for required fields are rejected.
     */
    @Test
    void testNonNullConstraintViolation() {
        AppUser user = new AppUser();
        // userName and email are required.
        assertThrows(DataIntegrityViolationException.class, () -> usersRepository.saveAndFlush(user));
    }

    /**
     * **Length Constraint Violation**
     * Confirm that values exceeding defined length limits are not persisted.
     */
    @Test
    void testLengthConstraintViolation() {
        AppUser user = new AppUser();
        user.setUserName("A".repeat(51));  // 51 characters, limit is 50
        user.setEmail("user@example.com");
        assertThrows(DataIntegrityViolationException.class, () -> usersRepository.saveAndFlush(user));
    }

    // =========================================================================
    // ✅ 3. Query Method Tests (including Custom Queries)
    // =========================================================================

    // (If custom query methods are added to UsersRepository, tests for them would go here.)

    // =========================================================================
    // ✅ 4. Bulk Operations Tests
    // =========================================================================

    /**
     * **SaveAll and DeleteAll entities**
     * Validate that bulk insertions and deletions function correctly.
     */
    @Test
    void testBulkOperations() {
        AppUser user1 = new AppUser(null, "Bulk User1", "bulk1@example.com", null);
        AppUser user2 = new AppUser(null, "Bulk User2", "bulk2@example.com", null);
        usersRepository.saveAll(List.of(user1, user2));
        List<AppUser> users = usersRepository.findAll();
        assertThat(users.size()).isGreaterThanOrEqualTo(2);
        usersRepository.deleteAll(List.of(user1, user2));
        Optional<AppUser> userOpt1 = usersRepository.findById(user1.getUserId());
        Optional<AppUser> userOpt2 = usersRepository.findById(user2.getUserId());
        assertThat(userOpt1).isNotPresent();
        assertThat(userOpt2).isNotPresent();
    }

    /**
     * **Bulk Operations Edge Case Test**
     * Verify that calling bulk operations with an empty list behaves as expected.
     */
    @Test
    void testBulkOperationsEmpty() {
        List<AppUser> emptyList = new ArrayList<>();
        assertDoesNotThrow(() -> usersRepository.saveAll(emptyList));
        assertDoesNotThrow(() -> usersRepository.deleteAll(emptyList));
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
     * We use TransactionTemplate inline to update an AppUser entity in a new transaction, mark it for rollback,
     * and then verify that the changes were not committed.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testTransactionalRollback() {
        // Persist a new AppUser.
        AppUser user = new AppUser();
        user.setUserName("TxUser");
        user.setEmail("txuser@example.com");
        AppUser savedUser = usersRepository.save(user);
        Long userId = savedUser.getUserId();
        String originalUserName = savedUser.getUserName();

        // Use TransactionTemplate to run an update in a new transaction and mark it for rollback.
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.execute(status -> {
            AppUser userToUpdate = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            userToUpdate.setUserName("UpdatedTxUser");
            usersRepository.saveAndFlush(userToUpdate);
            status.setRollbackOnly();
            return null;
        });

        // After rollback, verify that the state in the database remains unchanged.
        AppUser fetchedUser = usersRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        assertThat(fetchedUser.getUserName()).isEqualTo(originalUserName);
    }

    // =========================================================================
    // ✅ 6. Pagination and Sorting Tests
    // =========================================================================

    /**
     * **Pagination and Sorting (Ascending)**
     * Validate that pagination and ascending sorting by userName work as expected.
     */
    @Test
    void testPaginationAndSortingAscending() {
        for (int i = 1; i <= 10; i++) {
            AppUser user = new AppUser();
            user.setUserName("User" + i);
            user.setEmail("user" + i + "@example.com");
            usersRepository.save(user);
        }
        Pageable pageable = PageRequest.of(0, 5, Sort.by("userName").ascending());
        Page<AppUser> page = usersRepository.findAll(pageable);
        assertThat(page.getContent()).hasSize(5);
        List<AppUser> sortedUsers = page.getContent();
        for (int i = 1; i < sortedUsers.size(); i++) {
            assertThat(sortedUsers.get(i - 1).getUserName().compareTo(sortedUsers.get(i).getUserName()))
                .isLessThanOrEqualTo(0);
        }
    }

    /**
     * **Sorting (Descending)**
     * Confirm that sorting by userName in descending order returns results in the expected order.
     */
    @Test
    void testSortingDescending() {
        usersRepository.deleteAll();
        for (int i = 1; i <= 5; i++) {
            AppUser user = new AppUser();
            user.setUserName("User" + i);
            user.setEmail("user" + i + "@example.com");
            usersRepository.save(user);
        }
        Pageable pageable = PageRequest.of(0, 5, Sort.by("userName").descending());
        Page<AppUser> page = usersRepository.findAll(pageable);
        List<AppUser> sortedUsers = page.getContent();
        for (int i = 1; i < sortedUsers.size(); i++) {
            assertThat(sortedUsers.get(i - 1).getUserName().compareTo(sortedUsers.get(i).getUserName()))
                .isGreaterThanOrEqualTo(0);
        }
    }

    // =========================================================================
    // ✅ 7. Auditing and Optional Field Tests
    // =========================================================================

    /**
     * **Optional Field Handling**
     * Verify that the userTimes collection is handled correctly when not set.
     */
    @Test
    void testUserTimesOptionalField() {
        AppUser user = new AppUser();
        user.setUserName("OptionalTestUser");
        user.setEmail("optional@example.com");
        AppUser savedUser = usersRepository.save(user);
        // Since userTimes is not initialized, it should be null or an empty list.
        assertTrue(savedUser.getUserTimes() == null || savedUser.getUserTimes().isEmpty(),
                "userTimes should be null or empty if not set.");
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
        String longUserName = "A".repeat(50);
        AppUser user = new AppUser();
        user.setUserName(longUserName);
        user.setEmail("long@example.com");
        AppUser savedUser = usersRepository.save(user);
        assertThat(savedUser.getUserName().length()).isEqualTo(50);
    }

    /**
     * **Empty Strings**
     * Validate that empty string values are handled correctly.
     */
    @Test
    void testEmptyStringHandling() {
        AppUser user = new AppUser();
        user.setUserName("");
        user.setEmail("empty@example.com");
        AppUser savedUser = usersRepository.save(user);
        assertThat(savedUser.getUserName()).isEqualTo("");
    }

    /**
     * **Special Characters and Unicode**
     * Confirm that special and Unicode characters are persisted correctly.
     */
    @Test
    void testSpecialCharacters() {
        AppUser user = new AppUser();
        user.setUserName("特殊字符");
        user.setEmail("special@example.com");
        AppUser savedUser = usersRepository.save(user);
        assertThat(savedUser.getUserName()).isEqualTo("特殊字符");
    }

    /**
     * **Save Null Entity Exception Test**
     * Verify that passing a null entity to the save method throws an InvalidDataAccessApiUsageException.
     */
    @Test
    void testSaveNullEntity() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> usersRepository.save(null));
    }

    /**
     * **Find by ID with Null Exception Test**
     * Verify that passing a null ID to findById throws an InvalidDataAccessApiUsageException.
     */
    @Test
    void testFindByIdWithNull() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> usersRepository.findById(null));
    }

    /**
     * **Invalid Pagination Parameters**
     * Verify that negative pagination parameters throw an IllegalArgumentException.
     */
    @Test
    void testInvalidPaginationParameters() {
        assertThrows(IllegalArgumentException.class, () -> {
            Pageable pageable = PageRequest.of(-1, 5, Sort.by("userName").ascending());
            usersRepository.findAll(pageable);
        });
    }
}
