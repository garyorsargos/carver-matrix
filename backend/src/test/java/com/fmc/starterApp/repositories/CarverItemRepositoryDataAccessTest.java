package com.fmc.starterApp.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.fmc.starterApp.models.entity.CarverItem;
import com.fmc.starterApp.models.entity.CarverMatrix;
import com.fmc.starterApp.models.entity.User2;

/**
 * Integration tests for {@link CarverItemRepository}, verifying CRUD operations, constraint validations,
 * custom query methods, bulk operations, transactional behavior, pagination/sorting, auditing, and edge case handling.
 *
 * <p>This test class employs an in-memory H2 database configured for PostgreSQL compatibility.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class CarverItemRepositoryDataAccessTest {

    @Autowired
    private CarverItemRepository carverItemRepository;

    @Autowired
    private CarverMatrixRepository carverMatrixRepository;

    @Autowired
    private User2Repository user2Repository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    // Helper method: Create and persist a valid User2.
    private User2 createAndPersistUser() {
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        return user2Repository.save(user);
    }

    // Helper method: Create and persist a valid CarverMatrix for a given User2.
    private CarverMatrix createAndPersistMatrix(User2 user) {
        CarverMatrix matrix = new CarverMatrix();
        matrix.setUser(user);
        matrix.setName("Test Matrix");
        matrix.setDescription("Matrix for CarverItem tests");
        return carverMatrixRepository.save(matrix);
    }

    // =========================================================================
    // ✅ 1. Basic CRUD Tests
    // =========================================================================

    // ---------- Create Operation Tests ----------
    /**
     * **Save new entity**
     * Verify that a new CarverItem entity is correctly persisted.
     */
    @Test
    void testCreateCarverItem() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);

        CarverItem item = new CarverItem();
        item.setCarverMatrix(matrix);
        item.setItemName("Test Item");
        CarverItem savedItem = carverItemRepository.save(item);
        assertNotNull(savedItem.getItemId(), "The item should have a generated ID after save.");
        assertThat(savedItem.getItemName()).isEqualTo("Test Item");
    }

    // ---------- Read Operation Tests ----------
    /**
     * **Find by ID (positive case)**
     * Confirm retrieval by primary key returns the correct CarverItem entity.
     */
    @Test
    void testFindByIdPositive() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);
        
        CarverItem item = new CarverItem();
        item.setCarverMatrix(matrix);
        item.setItemName("FindById Item");
        CarverItem savedItem = carverItemRepository.save(item);
        Optional<CarverItem> foundItem = carverItemRepository.findById(savedItem.getItemId());
        assertTrue(foundItem.isPresent(), "CarverItem should be found by its ID.");
        assertThat(foundItem.get().getItemName()).isEqualTo("FindById Item");
    }

    /**
     * **Find by ID (negative case)**
     * Confirm that querying a non-existent ID returns an empty result.
     */
    @Test
    void testFindByIdNegative() {
        Optional<CarverItem> foundItem = carverItemRepository.findById(-1L);
        assertThat(foundItem).isNotPresent();
    }

    /**
     * **Find by unique field (via association) - positive case**
     * Verify that the custom query method returns the items associated with a given CarverMatrix.
     */
    @Test
    void testFindByCarverMatrixMatrixIdPositive() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);
        
        CarverItem item1 = new CarverItem();
        item1.setCarverMatrix(matrix);
        item1.setItemName("Item 1");
        CarverItem item2 = new CarverItem();
        item2.setCarverMatrix(matrix);
        item2.setItemName("Item 2");
        carverItemRepository.save(item1);
        carverItemRepository.save(item2);
        
        List<CarverItem> items = carverItemRepository.findByCarverMatrix_MatrixId(matrix.getMatrixId());
        assertFalse(items.isEmpty(), "Custom query should return items for the given matrix.");
        assertThat(items.size()).isGreaterThanOrEqualTo(2);
    }

    /**
     * **Find by unique field (negative case)**
     * Confirm that querying with a non-existent matrix ID returns an empty list.
     */
    @Test
    void testFindByCarverMatrixMatrixIdNegative() {
        List<CarverItem> items = carverItemRepository.findByCarverMatrix_MatrixId(-1L);
        assertThat(items).isEmpty();
    }

    // ---------- Update Operation Tests ----------
    /**
     * **Update existing entity**
     * Verify that changes to a CarverItem entity are correctly persisted.
     */
    @Test
    void testUpdateCarverItem() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);
        
        CarverItem item = new CarverItem();
        item.setCarverMatrix(matrix);
        item.setItemName("Original Item");
        CarverItem savedItem = carverItemRepository.save(item);
        
        savedItem.setItemName("Updated Item");
        carverItemRepository.saveAndFlush(savedItem);
        
        CarverItem updatedItem = carverItemRepository.findById(savedItem.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));
        assertThat(updatedItem.getItemName()).isEqualTo("Updated Item");
    }

    /**
     * **Update non-existent entity**
     * Verify that attempting to update an entity that doesn't exist creates a new record.
     */
    @Test
    void testUpdateNonExistentCarverItem() {
        CarverItem item = new CarverItem();
        item.setItemId(999999L);
        // Set only the required field.
        item.setItemName("NonExistent Item");
        CarverItem savedItem = carverItemRepository.save(item);
        assertNotNull(savedItem.getItemId(), "A new record should be created if the entity did not previously exist.");
    }

    // ---------- Delete Operation Tests ----------
    /**
     * **Delete existing entity**
     * Confirm that a CarverItem entity is removed from the repository after deletion.
     */
    @Test
    void testDeleteCarverItem() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);
        
        CarverItem item = new CarverItem();
        item.setCarverMatrix(matrix);
        item.setItemName("Delete Item");
        CarverItem savedItem = carverItemRepository.save(item);
        carverItemRepository.delete(savedItem);
        Optional<CarverItem> fetchedItem = carverItemRepository.findById(savedItem.getItemId());
        assertThat(fetchedItem).isNotPresent();
    }

    /**
     * **Delete non-existent entity**
     * Confirm that attempting to delete a non-persisted CarverItem does not throw unexpected exceptions.
     */
    @Test
    void testDeleteNonExistentCarverItem() {
        CarverItem nonExistentItem = new CarverItem();
        nonExistentItem.setItemName("NonExistent Item");
        assertDoesNotThrow(() -> carverItemRepository.delete(nonExistentItem));
    }

    // =========================================================================
    // ✅ 2. Constraint Validation Tests
    // =========================================================================

    /**
     * **Non-null Constraint Violation**
     * Verify that null values for required fields (itemName) are rejected.
     */
    @Test
    void testNonNullConstraintViolation() {
        CarverItem item = new CarverItem();
        // Not setting itemName (which is marked @NonNull) should cause an exception.
        assertThrows(DataIntegrityViolationException.class, () -> carverItemRepository.saveAndFlush(item));
    }

    /**
     * **Length Constraint Violation**
     * Confirm that values exceeding defined length limits for itemName (100 characters) are not persisted.
     */
    @Test
    void testLengthConstraintViolation() {
        CarverItem item = new CarverItem();
        item.setItemName("A".repeat(101)); // 101 characters (limit is 100)
        assertThrows(DataIntegrityViolationException.class, () -> carverItemRepository.saveAndFlush(item));
    }

    // =========================================================================
    // ✅ 3. Query Method Tests (including Custom Queries)
    // =========================================================================

    // (The findByCarverMatrix_MatrixId tests already cover the custom query method.)

    // =========================================================================
    // ✅ 4. Bulk Operations Tests
    // =========================================================================

    /**
     * **SaveAll Operation**
     * Validate that multiple CarverItem entities can be inserted in bulk.
     */
    @Test
    void testBulkSave() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);
        
        CarverItem item1 = new CarverItem();
        item1.setCarverMatrix(matrix);
        item1.setItemName("Bulk Item 1");
        CarverItem item2 = new CarverItem();
        item2.setCarverMatrix(matrix);
        item2.setItemName("Bulk Item 2");
        carverItemRepository.saveAll(List.of(item1, item2));
        
        List<CarverItem> items = carverItemRepository.findByCarverMatrix_MatrixId(matrix.getMatrixId());
        assertThat(items.size()).isGreaterThanOrEqualTo(2);
    }

    /**
     * **DeleteAll Operation**
     * Ensure that bulk deletions remove CarverItem entities as expected.
     */
    @Test
    void testBulkDelete() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);
        
        CarverItem item1 = new CarverItem();
        item1.setCarverMatrix(matrix);
        item1.setItemName("Bulk Delete Item 1");
        CarverItem item2 = new CarverItem();
        item2.setCarverMatrix(matrix);
        item2.setItemName("Bulk Delete Item 2");
        carverItemRepository.saveAll(List.of(item1, item2));
        
        carverItemRepository.deleteAll(List.of(item1, item2));
        List<CarverItem> items = carverItemRepository.findByCarverMatrix_MatrixId(matrix.getMatrixId());
        assertThat(items).isEmpty();
    }

    /**
     * **Bulk Operations Edge Case Test**
     * Verify that calling bulk operations with an empty list behaves as expected.
     */
    @Test
    void testBulkOperationsEmpty() {
        List<CarverItem> emptyList = new ArrayList<>();
        assertDoesNotThrow(() -> carverItemRepository.saveAll(emptyList));
        assertDoesNotThrow(() -> carverItemRepository.deleteAll(emptyList));
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
     * We use TransactionTemplate inline to update a CarverItem entity in a new transaction,
     * mark it for rollback, and then verify that the changes were not committed.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testTransactionalRollback() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);
        
        CarverItem item = new CarverItem();
        item.setCarverMatrix(matrix);
        item.setItemName("Tx Item");
        CarverItem savedItem = carverItemRepository.save(item);
        Long itemId = savedItem.getItemId();
        String originalName = savedItem.getItemName();

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(status -> {
            CarverItem itemToUpdate = carverItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Item not found"));
            itemToUpdate.setItemName("Updated Tx Item");
            carverItemRepository.saveAndFlush(itemToUpdate);
            status.setRollbackOnly();
            return null;
        });

        CarverItem fetchedItem = carverItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        assertThat(fetchedItem.getItemName()).isEqualTo(originalName);
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
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);
        // Save 10 items.
        for (int i = 1; i <= 10; i++) {
            CarverItem item = new CarverItem();
            item.setCarverMatrix(matrix);
            item.setItemName("Item" + i);
            carverItemRepository.save(item);
        }
        Pageable pageable = PageRequest.of(0, 5, Sort.by("itemName").ascending());
        Page<CarverItem> page = carverItemRepository.findAll(pageable);
        assertThat(page.getContent()).hasSize(5);
    }

    /**
     * **Sorting (Ascending and Descending)**
     * Confirm that sorting returns results in the expected order.
     */
    @Test
    void testSortingAscending() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);
        for (int i = 1; i <= 5; i++) {
            CarverItem item = new CarverItem();
            item.setCarverMatrix(matrix);
            item.setItemName("Item" + i);
            carverItemRepository.save(item);
        }
        Pageable pageable = PageRequest.of(0, 5, Sort.by("itemName").ascending());
        Page<CarverItem> page = carverItemRepository.findAll(pageable);
        List<CarverItem> sortedItems = page.getContent();
        for (int i = 1; i < sortedItems.size(); i++) {
            assertThat(sortedItems.get(i - 1).getItemName().compareTo(sortedItems.get(i).getItemName()))
                    .isLessThanOrEqualTo(0);
        }
    }

    @Test
    void testSortingDescending() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);
        for (int i = 1; i <= 5; i++) {
            CarverItem item = new CarverItem();
            item.setCarverMatrix(matrix);
            item.setItemName("Item" + i);
            carverItemRepository.save(item);
        }
        Pageable pageable = PageRequest.of(0, 5, Sort.by("itemName").descending());
        Page<CarverItem> page = carverItemRepository.findAll(pageable);
        List<CarverItem> sortedItems = page.getContent();
        for (int i = 1; i < sortedItems.size(); i++) {
            assertThat(sortedItems.get(i - 1).getItemName().compareTo(sortedItems.get(i).getItemName()))
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
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);
        CarverItem item = new CarverItem();
        item.setCarverMatrix(matrix);
        item.setItemName("Timestamp Item");
        CarverItem savedItem = carverItemRepository.save(item);
        assertNotNull(savedItem.getCreatedAt(), "The createdAt field should be automatically populated.");
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
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);
        String hundredChars = "A".repeat(100);
        CarverItem item = new CarverItem();
        item.setCarverMatrix(matrix);
        item.setItemName(hundredChars);
        CarverItem savedItem = carverItemRepository.save(item);
        assertThat(savedItem.getItemName().length()).isEqualTo(100);

        String longName = "A".repeat(101);
        item.setItemName(longName);
        assertThrows(DataIntegrityViolationException.class, () -> carverItemRepository.saveAndFlush(item));
    }

    /**
     * **Empty Strings**
     * Validate that empty string values are handled correctly.
     */
    @Test
    void testEmptyStringHandling() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);
        CarverItem item = new CarverItem();
        item.setCarverMatrix(matrix);
        item.setItemName(""); // Empty string test
        CarverItem savedItem = carverItemRepository.save(item);
        assertThat(savedItem.getItemName()).isEqualTo("");
    }

    /**
     * **Special Characters and Unicode**
     * Confirm that special and Unicode characters are persisted correctly.
     */
    @Test
    void testSpecialCharacters() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);
        CarverItem item = new CarverItem();
        item.setCarverMatrix(matrix);
        String specialName = "测试 - テスト - اختبار";
        item.setItemName(specialName);
        CarverItem savedItem = carverItemRepository.save(item);
        assertThat(savedItem.getItemName()).isEqualTo(specialName);
    }

    /**
     * **Save Null Entity Exception Test**
     * Verify that passing a null entity to the save method throws an InvalidDataAccessApiUsageException.
     */
    @Test
    void testSaveNullEntity() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> carverItemRepository.save(null));
    }

    /**
     * **Find by ID with Null Exception Test**
     * Verify that passing a null ID to findById throws an InvalidDataAccessApiUsageException.
     */
    @Test
    void testFindByIdWithNull() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> carverItemRepository.findById(null));
    }

    /**
     * **Invalid Pagination Parameters**
     * Verify that negative pagination parameters throw an IllegalArgumentException.
     */
    @Test
    void testInvalidPaginationParameters() {
        assertThrows(IllegalArgumentException.class, () -> {
            Pageable pageable = PageRequest.of(-1, 5, Sort.by("itemName").ascending());
            carverItemRepository.findAll(pageable);
        });
    }

    @Test
    void testOptionalFieldHandlingForCarverItem() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);

        CarverItem item = new CarverItem();
        item.setCarverMatrix(matrix);
        item.setItemName("Optional Field Test Item");
        // Do not set targetUsers; expect it to be an empty array instead.
        CarverItem savedItem = carverItemRepository.save(item);
        assertArrayEquals(new String[0], savedItem.getTargetUsers(), "Expected targetUsers to be an empty array when not set");
    }

}
