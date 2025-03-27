package com.fmc.starterApp.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import com.fmc.starterApp.models.entity.PostgresExampleObject;

/**
 * Integration tests for {@link PostgresRepository}, verifying CRUD operations, constraints, custom query methods,
 * bulk operations, transactional rollback, pagination/sorting, and edge case handling.
 *
 * <p>This test class employs an in-memory H2 database configured for PostgreSQL compatibility to simulate production-like
 * database behavior. It covers essential scenarios from the Repository Testing Checklist:
 * <ul>
 *   <li><strong>Basic CRUD Tests:</strong> Create, Read, Update, and Delete operations.</li>
 *   <li><strong>Constraint Validation Tests:</strong> Unique, Non-null, and Length constraints.</li>
 *   <li><strong>Query Method Tests:</strong> Derived and custom query methods (e.g., findByName, findFirstById, deleteAllById).</li>
 *   <li><strong>Bulk Operations Tests:</strong> Testing saveAll and deleteAll, including edge cases with empty lists.</li>
 *   <li><strong>Transactional Tests:</strong> Rollback scenarios and verifying the database state remains unchanged.</li>
 *   <li><strong>Pagination and Sorting Tests:</strong> Using PageRequest and Sort in various directions.</li>
 *   <li><strong>Edge Case and Exception Handling Tests:</strong> Testing boundary conditions and unexpected inputs.</li>
 * </ul>
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class PostgresRepositoryDataAccessTest {

    @Autowired
    private PostgresRepository postgresRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    // =========================================================================
    // ✅ 1. Basic CRUD Tests
    // =========================================================================

    // ---------- Create Operation Tests ----------
    /**
     * **Save new entity**
     * Verify that a new PostgresExampleObject is correctly persisted.
     */
    @Test
    void testCreatePostgresExampleObject() {
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName("Test Name");
        PostgresExampleObject savedObj = postgresRepository.save(obj);
        assertNotNull(savedObj.getId(), "The object should have a generated ID after save.");
        assertThat(savedObj.getName()).isEqualTo("Test Name");
    }

    // ---------- Read Operation Tests ----------
    /**
     * **Find by ID (positive case)**
     * Confirm that retrieval by primary key returns the correct object.
     */
    @Test
    void testFindByIdPositive() {
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName("Read Test");
        PostgresExampleObject savedObj = postgresRepository.save(obj);
        Optional<PostgresExampleObject> foundObj = postgresRepository.findById(savedObj.getId());
        assertTrue(foundObj.isPresent(), "Object should be found by its ID.");
        assertThat(foundObj.get().getName()).isEqualTo("Read Test");
    }

    /**
     * **Find by ID (negative case)**
     * Confirm that querying a non-existent ID returns an empty result.
     */
    @Test
    void testFindByIdNegative() {
        Optional<PostgresExampleObject> foundObj = postgresRepository.findById(-1L);
        assertThat(foundObj).isNotPresent();
    }

    /**
     * **Find by unique field (positive case)**
     * Verify that querying by name returns the correct object.
     */
    @Test
    void testFindByNamePositive() {
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName("UniqueName");
        postgresRepository.save(obj);
        List<PostgresExampleObject> list = postgresRepository.findByName("UniqueName");
        assertFalse(list.isEmpty(), "findByName should return a non-empty list.");
        assertThat(list.get(0).getName()).isEqualTo("UniqueName");
    }

    /**
     * **Find by unique field (negative case)**
     * Verify that querying with a non-existent name returns an empty list.
     */
    @Test
    void testFindByNameNegative() {
        List<PostgresExampleObject> list = postgresRepository.findByName("NonExistentName");
        assertTrue(list.isEmpty(), "findByName should return an empty list for a non-existent name.");
    }

    /**
     * **Find by unique field (null parameter)**
     * Ensure that querying with a null name yields an empty result.
     */
    @Test
    void testFindByNameWithNull() {
        List<PostgresExampleObject> list = postgresRepository.findByName(null);
        assertTrue(list.isEmpty(), "findByName should return an empty list when name is null.");
    }

    /**
     * **Find first by ID (positive case)**
     * Verify that the custom query findFirstById returns the correct object.
     */
    @Test
    void testFindFirstByIdPositive() {
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName("FirstByIdTest");
        PostgresExampleObject savedObj = postgresRepository.save(obj);
        PostgresExampleObject foundObj = postgresRepository.findFirstById(savedObj.getId());
        assertNotNull(foundObj, "findFirstById should return the object.");
        assertThat(foundObj.getName()).isEqualTo("FirstByIdTest");
    }

    /**
     * **Find first by ID (negative case)**
     * Verify that findFirstById returns null for a non-existent ID.
     */
    @Test
    void testFindFirstByIdNegative() {
        PostgresExampleObject foundObj = postgresRepository.findFirstById(-1L);
        assertNull(foundObj, "findFirstById should return null for a non-existent ID.");
    }

    // ---------- Update Operation Tests ----------
    /**
     * **Update existing entity**
     * Verify that changes to an object are correctly persisted.
     */
    @Test
    void testUpdatePostgresExampleObject() {
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName("Initial Name");
        PostgresExampleObject savedObj = postgresRepository.save(obj);
        savedObj.setName("Updated Name");
        PostgresExampleObject updatedObj = postgresRepository.saveAndFlush(savedObj);
        assertThat(updatedObj.getName()).isEqualTo("Updated Name");
    }

    /**
     * **Update non-existent entity**
     * Verify that attempting to update an entity that doesn't exist creates a new record.
     */
    @Test
    void testUpdateNonExistentPostgresExampleObject() {
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setId(999999L);
        obj.setName("Nonexistent");
        PostgresExampleObject savedObj = postgresRepository.save(obj);
        assertNotNull(savedObj.getId(), "A new record should be created if the entity did not previously exist.");
    }

    // ---------- Delete Operation Tests ----------
    /**
     * **Delete existing entity**
     * Confirm that an object is removed from the repository after deletion.
     */
    @Test
    void testDeletePostgresExampleObject() {
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName("Delete Test");
        PostgresExampleObject savedObj = postgresRepository.save(obj);
        postgresRepository.delete(savedObj);
        Optional<PostgresExampleObject> deletedObj = postgresRepository.findById(savedObj.getId());
        assertThat(deletedObj).isNotPresent();
    }

    /**
     * **Delete non-existent entity**
     * Confirm that attempting to delete a non-persisted entity does not throw unexpected exceptions.
     */
    @Test
    void testDeleteNonExistentPostgresExampleObject() {
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName("Nonexistent");
        assertDoesNotThrow(() -> postgresRepository.delete(obj), "Deleting a transient entity should be a no-op.");
    }

    // =========================================================================
    // ✅ 2. Constraint Validation Tests
    // =========================================================================

    /**
     * **Non-null Constraint Violation**
     * Verify that null values for required fields (name) are rejected.
     */
    @Test
    void testNonNullConstraintViolation() {
        PostgresExampleObject obj = new PostgresExampleObject();
        // Not setting name should cause a constraint violation.
        assertThrows(DataIntegrityViolationException.class, () -> postgresRepository.saveAndFlush(obj));
    }

    /**
     * **Length Constraint Violation**
     * Confirm that values exceeding defined length limits (name > 100 characters) are not persisted.
     */
    @Test
    void testLengthConstraintViolation() {
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName("A".repeat(101));
        assertThrows(DataIntegrityViolationException.class, () -> postgresRepository.saveAndFlush(obj));
    }

    // =========================================================================
    // ✅ 3. Query Method Tests (including Custom Queries)
    // =========================================================================
    // (Custom query tests for additional query methods can be added here.)

    // =========================================================================
    // ✅ 4. Bulk Operations Tests
    // =========================================================================

    /**
     * **SaveAll and DeleteAll Operation**
     * Validate that multiple entities can be inserted in bulk and then removed.
     */
    @Test
    void testBulkOperations() {
        PostgresExampleObject obj1 = new PostgresExampleObject(null, "Bulk1");
        PostgresExampleObject obj2 = new PostgresExampleObject(null, "Bulk2");
        postgresRepository.saveAll(List.of(obj1, obj2));
        List<PostgresExampleObject> list = postgresRepository.findAll();
        assertThat(list.size()).isGreaterThanOrEqualTo(2);
        postgresRepository.deleteAll(List.of(obj1, obj2));
        Optional<PostgresExampleObject> found1 = postgresRepository.findById(obj1.getId());
        Optional<PostgresExampleObject> found2 = postgresRepository.findById(obj2.getId());
        assertThat(found1).isNotPresent();
        assertThat(found2).isNotPresent();
    }

    /**
     * **Bulk Operations Edge Case Test**
     * Verify that calling bulk operations with an empty list behaves as expected.
     */
    @Test
    void testBulkOperationsEmpty() {
        List<PostgresExampleObject> emptyList = new ArrayList<>();
        assertDoesNotThrow(() -> postgresRepository.saveAll(emptyList));
        assertDoesNotThrow(() -> postgresRepository.deleteAll(emptyList));
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
     * We use TransactionTemplate inline to update a PostgresExampleObject entity, mark the transaction for rollback,
     * and then verify that the update was not committed.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testTransactionalRollback() {
        // Persist a new entity.
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName("Initial Name");
        PostgresExampleObject savedObj = postgresRepository.save(obj);
        Long objId = savedObj.getId();
        String originalName = savedObj.getName();

        // Use TransactionTemplate to run an update and mark for rollback.
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.execute(status -> {
            PostgresExampleObject toUpdate = postgresRepository.findById(objId)
                    .orElseThrow(() -> new RuntimeException("Object not found"));
            toUpdate.setName("Updated Name");
            postgresRepository.saveAndFlush(toUpdate);
            status.setRollbackOnly();
            return null;
        });

        // After the rollback, verify that the name remains unchanged.
        PostgresExampleObject fetchedObj = postgresRepository.findById(objId)
                    .orElseThrow(() -> new RuntimeException("Object not found"));
        assertThat(fetchedObj.getName()).isEqualTo(originalName);
    }

    // =========================================================================
    // ✅ 6. Pagination and Sorting Tests
    // =========================================================================

    /**
     * **Pagination**
     * Validate that pagination correctly limits and offsets results.
     */
    @Test
    void testPagination() {
        for (int i = 1; i <= 10; i++) {
            PostgresExampleObject obj = new PostgresExampleObject();
            obj.setName("Name" + i);
            postgresRepository.save(obj);
        }
        Pageable pageable = PageRequest.of(0, 5, Sort.by("name").ascending());
        Page<PostgresExampleObject> page = postgresRepository.findAll(pageable);
        assertThat(page.getContent()).hasSize(5);
    }

    /**
     * **Sorting (Ascending and Descending)**
     * Confirm that sorting returns results in the expected order for both ascending and descending directions.
     */
    @Test
    void testSorting() {
        postgresRepository.deleteAll();
        for (int i = 1; i <= 5; i++) {
            PostgresExampleObject obj = new PostgresExampleObject();
            obj.setName("Name" + i);
            postgresRepository.save(obj);
        }
        // Ascending sort
        Pageable ascPageable = PageRequest.of(0, 5, Sort.by("name").ascending());
        Page<PostgresExampleObject> ascPage = postgresRepository.findAll(ascPageable);
        List<PostgresExampleObject> ascList = ascPage.getContent();
        for (int i = 1; i < ascList.size(); i++) {
            assertThat(ascList.get(i - 1).getName().compareTo(ascList.get(i).getName()))
                    .isLessThanOrEqualTo(0);
        }
        // Descending sort
        Pageable descPageable = PageRequest.of(0, 5, Sort.by("name").descending());
        Page<PostgresExampleObject> descPage = postgresRepository.findAll(descPageable);
        List<PostgresExampleObject> descList = descPage.getContent();
        for (int i = 1; i < descList.size(); i++) {
            assertThat(descList.get(i - 1).getName().compareTo(descList.get(i).getName()))
                    .isGreaterThanOrEqualTo(0);
        }
    }

    // =========================================================================
    // ✅ 7. Auditing and Optional Field Tests
    // =========================================================================
    // (Not applicable – PostgresExampleObject does not have auditing or optional fields.)

    // =========================================================================
    // ✅ 8. Edge Case and Exception Handling Tests
    // =========================================================================

    /**
     * **Empty Strings Test**
     * Validate that empty string values are handled correctly.
     */
    @Test
    void testEmptyStringHandling() {
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName("");
        PostgresExampleObject savedObj = postgresRepository.save(obj);
        assertThat(savedObj.getName()).isEqualTo("");
    }

    /**
     * **Special Characters and Unicode Test**
     * Confirm that special and Unicode characters are persisted correctly.
     */
    @Test
    void testSpecialCharacters() {
        PostgresExampleObject obj = new PostgresExampleObject();
        String specialName = "测试 - テスト - اختبار";
        obj.setName(specialName);
        PostgresExampleObject savedObj = postgresRepository.save(obj);
        assertThat(savedObj.getName()).isEqualTo(specialName);
    }

    /**
     * **Save Null Entity Exception Test**
     * Verify that passing a null entity to the save method throws an InvalidDataAccessApiUsageException.
     */
    @Test
    void testSaveNullEntity() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> postgresRepository.save(null));
    }

    /**
     * **Find by ID with Null Exception Test**
     * Verify that passing a null ID to findById throws an InvalidDataAccessApiUsageException.
     */
    @Test
    void testFindByIdWithNull() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> postgresRepository.findById(null));
    }

    /**
     * **Invalid Pagination Parameters Test**
     * Verify that negative pagination parameters throw an IllegalArgumentException.
     */
    @Test
    void testInvalidPaginationParameters() {
        assertThrows(IllegalArgumentException.class, () -> {
            Pageable pageable = PageRequest.of(-1, 5, Sort.by("name").ascending());
            postgresRepository.findAll(pageable);
        });
    }
}
