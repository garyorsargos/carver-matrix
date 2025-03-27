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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.fmc.starterApp.models.entity.CarverMatrix;
import com.fmc.starterApp.models.entity.User2;

/**
 * Integration tests for {@link CarverMatrixRepository}, verifying CRUD operations, constraints,
 * custom queries, bulk operations, transactional behavior, pagination, sorting, auditing, and edge case handling.
 *
 * <p>This test class employs an in-memory H2 database configured for PostgreSQL compatibility.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class CarverMatrixRepositoryDataAccessTest {

    @Autowired
    private CarverMatrixRepository carverMatrixRepository;

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
     * Verify that a new CarverMatrix entity is correctly persisted.
     */
    @Test
    void testCreateCarverMatrix() {
        // Create a unique user using the repository.
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        user = user2Repository.save(user);

        CarverMatrix matrix = new CarverMatrix();
        matrix.setUser(user);
        matrix.setName("Test Matrix");
        matrix.setDescription("A description for Test Matrix");
        CarverMatrix savedMatrix = carverMatrixRepository.save(matrix);
        assertNotNull(savedMatrix.getMatrixId(), "The matrix should have a generated ID after save.");
        assertThat(savedMatrix.getName()).isEqualTo("Test Matrix");
    }

    // ---------- Read Operation Tests ----------
    /**
     * **Find by ID (positive case)**  
     * Confirm retrieval by primary key returns the correct entity.
     */
    @Test
    void testFindByIdPositive() {
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        user = user2Repository.save(user);

        CarverMatrix matrix = new CarverMatrix();
        matrix.setUser(user);
        matrix.setName("FindById Matrix");
        CarverMatrix savedMatrix = carverMatrixRepository.save(matrix);
        Optional<CarverMatrix> foundMatrix = carverMatrixRepository.findById(savedMatrix.getMatrixId());
        assertTrue(foundMatrix.isPresent(), "CarverMatrix should be found by its ID.");
        assertThat(foundMatrix.get().getName()).isEqualTo("FindById Matrix");
    }

    /**
     * **Find by ID (negative case)**  
     * Confirm that querying a non-existent ID returns an empty result.
     */
    @Test
    void testFindByIdNegative() {
        Optional<CarverMatrix> foundMatrix = carverMatrixRepository.findById(-1L);
        assertThat(foundMatrix).isNotPresent();
    }

    /**
     * **Find by unique field (positive case)**  
     * Verify that querying by the matrixId via a custom method returns the correct entity.
     */
    @Test
    void testFindFirstByMatrixIdPositive() {
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        user = user2Repository.save(user);

        CarverMatrix matrix = new CarverMatrix();
        matrix.setUser(user);
        matrix.setName("UniqueMatrix");
        CarverMatrix savedMatrix = carverMatrixRepository.save(matrix);
        CarverMatrix fetchedMatrix = carverMatrixRepository.findFirstByMatrixId(savedMatrix.getMatrixId());
        assertNotNull(fetchedMatrix, "The matrix should be fetched by findFirstByMatrixId.");
        assertThat(fetchedMatrix.getName()).isEqualTo("UniqueMatrix");
    }

    /**
     * **Find by unique field (negative case)**  
     * Confirm that querying with a non-existent matrixId returns null.
     */
    @Test
    void testFindFirstByMatrixIdNegative() {
        CarverMatrix fetchedMatrix = carverMatrixRepository.findFirstByMatrixId(-1L);
        assertNull(fetchedMatrix, "Non-existent matrix ID should return null.");
    }

    // ---------- Update Operation Tests ----------
    /**
     * **Update existing entity**  
     * Verify that changes to a CarverMatrix entity are correctly persisted.
     */
    @Test
    void testUpdateCarverMatrix() {
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        user = user2Repository.save(user);

        CarverMatrix matrix = new CarverMatrix();
        matrix.setUser(user);
        matrix.setName("Original Matrix");
        matrix.setDescription("Original Description");
        CarverMatrix savedMatrix = carverMatrixRepository.save(matrix);

        savedMatrix.setName("Updated Matrix");
        savedMatrix.setDescription("Updated Description");
        carverMatrixRepository.saveAndFlush(savedMatrix);

        CarverMatrix updatedMatrix = carverMatrixRepository.findFirstByMatrixId(savedMatrix.getMatrixId());
        assertThat(updatedMatrix.getName()).isEqualTo("Updated Matrix");
        assertThat(updatedMatrix.getDescription()).isEqualTo("Updated Description");
    }

    /**
     * **Update non-existent entity**  
     * Verify that attempting to update an entity that doesn't exist creates a new record.
     */
    @Test
    void testUpdateNonExistentCarverMatrix() {
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(999999L);
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        user = user2Repository.save(user);
        matrix.setUser(user);
        matrix.setName("NonExistent Matrix");
        matrix.setDescription("Should be created as new record");
        CarverMatrix savedMatrix = carverMatrixRepository.save(matrix);
        assertNotNull(savedMatrix.getMatrixId(), "A new record should be created if the entity did not previously exist.");
    }

    // ---------- Delete Operation Tests ----------
    /**
     * **Delete existing entity**  
     * Confirm that a CarverMatrix entity is removed from the repository after deletion.
     */
    @Test
    void testDeleteCarverMatrix() {
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        user = user2Repository.save(user);

        CarverMatrix matrix = new CarverMatrix();
        matrix.setUser(user);
        matrix.setName("Delete Matrix");
        CarverMatrix savedMatrix = carverMatrixRepository.save(matrix);
        carverMatrixRepository.delete(savedMatrix);
        CarverMatrix fetchedMatrix = carverMatrixRepository.findFirstByMatrixId(savedMatrix.getMatrixId());
        assertNull(fetchedMatrix, "The matrix should be deleted.");
    }

    /**
     * **Delete non-existent entity**  
     * Confirm that attempting to delete a non-persisted CarverMatrix does not throw unexpected exceptions.
     */
    @Test
    void testDeleteNonExistentCarverMatrix() {
        CarverMatrix nonExistentMatrix = new CarverMatrix();
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        user = user2Repository.save(user);
        nonExistentMatrix.setUser(user);
        nonExistentMatrix.setName("NonExistent Matrix");
        assertDoesNotThrow(() -> carverMatrixRepository.delete(nonExistentMatrix));
    }

    // =========================================================================
    // ✅ 2. Constraint Validation Tests
    // =========================================================================

    /**
     * **Non-null Constraint Violation**  
     * Verify that null values for required fields are rejected.
     * Note: In this design, only the 'name' field is enforced as non-null.
     */
    @Test
    void testNonNullConstraintViolation() {
        CarverMatrix matrix = new CarverMatrix();
        // Not setting 'name' should cause an exception because it is marked @NonNull.
        assertThrows(DataIntegrityViolationException.class, () -> carverMatrixRepository.saveAndFlush(matrix));
    }

    /**
     * **Length Constraint Violation**  
     * Confirm that values exceeding defined length limits are not persisted.
     */
    @Test
    void testLengthConstraintViolation() {
        CarverMatrix matrix = new CarverMatrix();
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        user = user2Repository.save(user);
        matrix.setUser(user);
        String longName = "A".repeat(101); // 101 characters (limit is 100)
        matrix.setName(longName);
        matrix.setDescription("Valid Description");
        assertThrows(DataIntegrityViolationException.class, () -> carverMatrixRepository.saveAndFlush(matrix));
    }

    // =========================================================================
    // ✅ 3. Query Method Tests (including Custom Queries)
    // =========================================================================

    /**
     * **Custom Query Positive Case (findByHost)**  
     * Verify that the custom query findByHost returns the expected results for a valid host email.
     */
    @Test
    void testFindByHostPositive() {
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        user = user2Repository.save(user);

        CarverMatrix matrix = new CarverMatrix();
        matrix.setUser(user);
        matrix.setName("Host Matrix");
        matrix.setHosts(new String[] { "host@example.com" });
        carverMatrixRepository.save(matrix);
        List<CarverMatrix> result = carverMatrixRepository.findByHost("host@example.com");
        assertFalse(result.isEmpty(), "Custom query findByHost should return results for a valid host.");
    }

    /**
     * **Custom Query Negative Case (findByHost)**  
     * Confirm that the custom query findByHost returns an empty result for a non-existent host.
     */
    @Test
    void testFindByHostNegative() {
        List<CarverMatrix> result = carverMatrixRepository.findByHost("nonexistent@example.com");
        assertTrue(result.isEmpty(), "Custom query findByHost should return empty for non-existent host.");
    }

    /**
     * **Custom Query Null Parameter Handling (findByHost)**  
     * Verify that the custom query findByHost returns empty when null is passed as a parameter.
     */
    @Test
    void testFindByHostWithNull() {
        List<CarverMatrix> result = carverMatrixRepository.findByHost(null);
        assertTrue(result.isEmpty(), "Custom query findByHost should return empty when null is passed.");
    }

    /**
     * **Custom Query Positive Case (findByParticipant)**  
     * Verify that the custom query findByParticipant returns the expected results for a valid participant email.
     */
    @Test
    void testFindByParticipantPositive() {
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        user = user2Repository.save(user);

        CarverMatrix matrix = new CarverMatrix();
        matrix.setUser(user);
        matrix.setName("Participant Matrix");
        matrix.setParticipants(new String[] { "participant@example.com" });
        carverMatrixRepository.save(matrix);
        List<CarverMatrix> result = carverMatrixRepository.findByParticipant("participant@example.com");
        assertFalse(result.isEmpty(), "Custom query findByParticipant should return results for a valid participant.");
    }

    /**
     * **Custom Query Negative Case (findByParticipant)**  
     * Confirm that the custom query findByParticipant returns an empty result for a non-existent participant.
     */
    @Test
    void testFindByParticipantNegative() {
        List<CarverMatrix> result = carverMatrixRepository.findByParticipant("nonexistent@example.com");
        assertTrue(result.isEmpty(), "Custom query findByParticipant should return empty for non-existent participant.");
    }

    /**
     * **Custom Query Null Parameter Handling (findByParticipant)**  
     * Verify that the custom query findByParticipant returns empty when null is passed as a parameter.
     */
    @Test
    void testFindByParticipantWithNull() {
        List<CarverMatrix> result = carverMatrixRepository.findByParticipant(null);
        assertTrue(result.isEmpty(), "Custom query findByParticipant should return empty when null is passed.");
    }

    // =========================================================================
    // ✅ 4. Bulk Operations Tests
    // =========================================================================

    /**
     * **SaveAll and DeleteAll Operations**  
     * Validate that bulk insertions and deletions function correctly.
     */
    @Test
    void testBulkOperations() {
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        user = user2Repository.save(user);

        CarverMatrix matrix1 = new CarverMatrix();
        matrix1.setUser(user);
        matrix1.setName("Bulk Matrix 1");
        CarverMatrix matrix2 = new CarverMatrix();
        matrix2.setUser(user);
        matrix2.setName("Bulk Matrix 2");
        carverMatrixRepository.saveAll(List.of(matrix1, matrix2));
        List<CarverMatrix> matrices = carverMatrixRepository.findAll();
        assertThat(matrices.size()).isGreaterThanOrEqualTo(2);
        carverMatrixRepository.deleteAll(List.of(matrix1, matrix2));
        Optional<CarverMatrix> m1 = Optional.ofNullable(carverMatrixRepository.findFirstByMatrixId(matrix1.getMatrixId()));
        Optional<CarverMatrix> m2 = Optional.ofNullable(carverMatrixRepository.findFirstByMatrixId(matrix2.getMatrixId()));
        assertTrue(m1.isEmpty(), "Matrix 1 should be deleted.");
        assertTrue(m2.isEmpty(), "Matrix 2 should be deleted.");
    }

    /**
     * **Bulk Operations Edge Case Test**  
     * Verify that calling bulk operations with an empty list behaves as expected.
     */
    @Test
    void testBulkOperationsEmpty() {
        List<CarverMatrix> emptyList = new ArrayList<>();
        assertDoesNotThrow(() -> carverMatrixRepository.saveAll(emptyList));
        assertDoesNotThrow(() -> carverMatrixRepository.deleteAll(emptyList));
    }

    // =========================================================================
    // ✅ 5. Transactional Tests
    // =========================================================================

    /**
     * **Transactional Rollback**  
     * Simulate an exception within a transaction to verify that the repository rolls back the transaction,
     * leaving the database state unchanged.
     *
     * In this test, we disable the default transaction so that we can manually manage transactions.
     * We use TransactionTemplate inline to update a CarverMatrix entity in a new transaction,
     * mark it for rollback, and then verify that the changes were not committed.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testTransactionalRollback() {
        // Create a unique user.
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        user = user2Repository.save(user);
        
        CarverMatrix matrix = new CarverMatrix();
        matrix.setUser(user);
        matrix.setName("Tx Matrix");
        matrix.setDescription("Original Description");
        CarverMatrix savedMatrix = carverMatrixRepository.save(matrix);
        Long matrixId = savedMatrix.getMatrixId();
        String originalName = savedMatrix.getName();

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(status -> {
            CarverMatrix matrixToUpdate = carverMatrixRepository.findById(matrixId)
                    .orElseThrow(() -> new RuntimeException("Matrix not found"));
            matrixToUpdate.setName("Updated Tx Matrix");
            carverMatrixRepository.saveAndFlush(matrixToUpdate);
            status.setRollbackOnly();
            return null;
        });

        CarverMatrix fetchedMatrix = carverMatrixRepository.findById(matrixId)
                .orElseThrow(() -> new RuntimeException("Matrix not found"));
        assertThat(fetchedMatrix.getName()).isEqualTo(originalName);
    }

    // =========================================================================
    // ✅ 6. Pagination and Sorting Tests
    // =========================================================================

    /**
     * **Pagination and Sorting (Ascending)**  
     * Validate that pagination and ascending sorting by name work as expected.
     */
    @Test
    void testPaginationAndSortingAscending() {
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        user = user2Repository.save(user);
        for (int i = 1; i <= 10; i++) {
            CarverMatrix matrix = new CarverMatrix();
            matrix.setUser(user);
            matrix.setName("Matrix" + i);
            carverMatrixRepository.save(matrix);
        }
        Pageable pageable = PageRequest.of(0, 5, Sort.by("name").ascending());
        Page<CarverMatrix> page = carverMatrixRepository.findAll(pageable);
        assertThat(page.getContent()).hasSize(5);
        List<CarverMatrix> sortedMatrices = page.getContent();
        for (int i = 1; i < sortedMatrices.size(); i++) {
            assertThat(sortedMatrices.get(i - 1).getName().compareTo(sortedMatrices.get(i).getName()))
                .isLessThanOrEqualTo(0);
        }
    }

    /**
     * **Sorting (Descending)**  
     * Confirm that sorting by name in descending order returns results in the expected order.
     */
    @Test
    void testSortingDescending() {
        carverMatrixRepository.deleteAll();
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        user = user2Repository.save(user);
        for (int i = 1; i <= 5; i++) {
            CarverMatrix matrix = new CarverMatrix();
            matrix.setUser(user);
            matrix.setName("Matrix" + i);
            carverMatrixRepository.save(matrix);
        }
        Pageable pageable = PageRequest.of(0, 5, Sort.by("name").descending());
        Page<CarverMatrix> page = carverMatrixRepository.findAll(pageable);
        List<CarverMatrix> sortedMatrices = page.getContent();
        for (int i = 1; i < sortedMatrices.size(); i++) {
            assertThat(sortedMatrices.get(i - 1).getName().compareTo(sortedMatrices.get(i).getName()))
                .isGreaterThanOrEqualTo(0);
        }
    }

    // =========================================================================
    // ✅ 7. Auditing and Optional Field Tests
    // =========================================================================

    /**
     * **Creation Timestamp**  
     * Verify that the creation timestamp (createdAt) is automatically populated upon entity creation.
     */
    @Test
    void testCreationTimestamp() {
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        user = user2Repository.save(user);

        CarverMatrix matrix = new CarverMatrix();
        matrix.setUser(user);
        matrix.setName("Timestamp Matrix");
        CarverMatrix savedMatrix = carverMatrixRepository.save(matrix);
        assertNotNull(savedMatrix.getCreatedAt(), "The createdAt field should be automatically populated.");
    }

    /**
     * **Optional Field Handling**  
     * Verify that entities handle optional fields (set to null) correctly without causing persistence issues.
     */
    @Test
    void testOptionalFieldHandling() {
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        user = user2Repository.save(user);

        CarverMatrix matrix = new CarverMatrix();
        matrix.setUser(user);
        matrix.setName("Optional Field Matrix");
        matrix.setDescription(null);
        CarverMatrix savedMatrix = carverMatrixRepository.save(matrix);
        assertNull(savedMatrix.getDescription(), "The description field should be null.");
    }

    // =========================================================================
    // ✅ 8. Edge Case and Exception Handling Tests
    // =========================================================================

    /**
     * **Max-length Strings**  
     * Confirm that string fields respect maximum allowed lengths.
     */
    @Test
    void testMaxLengthConstraints() {
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        user = user2Repository.save(user);

        CarverMatrix matrix = new CarverMatrix();
        matrix.setUser(user);
        String hundredChars = "A".repeat(100);
        matrix.setName(hundredChars);
        CarverMatrix savedMatrix = carverMatrixRepository.save(matrix);
        assertThat(savedMatrix.getName().length()).isEqualTo(100);

        String longName = "A".repeat(101);
        matrix.setName(longName);
        assertThrows(DataIntegrityViolationException.class, () -> carverMatrixRepository.saveAndFlush(matrix));
    }

    /**
     * **Empty Strings**  
     * Validate that empty string values are handled correctly.
     */
    @Test
    void testEmptyStringHandling() {
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        user = user2Repository.save(user);

        CarverMatrix matrix = new CarverMatrix();
        matrix.setUser(user);
        matrix.setName(""); // Empty string test
        CarverMatrix savedMatrix = carverMatrixRepository.save(matrix);
        assertThat(savedMatrix.getName()).isEqualTo("");
    }

    /**
     * **Special Characters and Unicode**  
     * Confirm that special and Unicode characters are persisted correctly.
     */
    @Test
    void testSpecialCharacters() {
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        user = user2Repository.save(user);

        CarverMatrix matrix = new CarverMatrix();
        matrix.setUser(user);
        String specialName = "测试 - テスト - اختبار";
        matrix.setName(specialName);
        CarverMatrix savedMatrix = carverMatrixRepository.save(matrix);
        assertThat(savedMatrix.getName()).isEqualTo(specialName);
    }

    /**
     * **Save Null Entity Exception Test**  
     * Verify that passing a null entity to the save method throws an InvalidDataAccessApiUsageException.
     */
    @Test
    void testSaveNullEntity() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> carverMatrixRepository.save(null));
    }

    /**
     * **Find by ID with Null Exception Test**  
     * Verify that passing a null ID to findById throws an InvalidDataAccessApiUsageException.
     */
    @Test
    void testFindByIdWithNull() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> carverMatrixRepository.findById(null));
    }

    /**
     * **Invalid Pagination Parameters**  
     * Verify that negative pagination parameters throw an IllegalArgumentException.
     */
    @Test
    void testInvalidPaginationParameters() {
        assertThrows(IllegalArgumentException.class, () -> {
            Pageable pageable = PageRequest.of(-1, 5, Sort.by("name").ascending());
            carverMatrixRepository.findAll(pageable);
        });
    }
}
