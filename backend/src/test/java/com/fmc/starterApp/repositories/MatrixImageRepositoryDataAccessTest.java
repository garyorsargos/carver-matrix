package com.fmc.starterApp.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import com.fmc.starterApp.models.entity.MatrixImage;
import com.fmc.starterApp.models.entity.User2;

/**
 * Integration tests for {@link MatrixImageRepository}, verifying CRUD operations, constraints,
 * bulk operations, transactional behavior, pagination, sorting, auditing, and edge case handling.
 *
 * <p>This test class employs an in-memory H2 database configured for PostgreSQL compatibility.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class MatrixImageRepositoryDataAccessTest {

    @Autowired
    private MatrixImageRepository matrixImageRepository;

    @Autowired
    private CarverMatrixRepository carverMatrixRepository;

    @Autowired
    private User2Repository user2Repository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    // ----------------------------------------------------------------------------
    // Helper Methods
    // ----------------------------------------------------------------------------

    private User2 createAndPersistUser() {
        User2 user = new User2();
        user.setKeycloakId("user-" + System.nanoTime());
        user.setUsername("testuser-" + System.nanoTime());
        user.setEmail("testuser-" + System.nanoTime() + "@example.com");
        return user2Repository.save(user);
    }

    private CarverMatrix createAndPersistMatrix(User2 user) {
        CarverMatrix matrix = new CarverMatrix();
        matrix.setUser(user);
        matrix.setName("Test Matrix");
        matrix.setDescription("Matrix for testing MatrixImage");
        return carverMatrixRepository.save(matrix);
    }

    // ----------------------------------------------------------------------------
    // ✅ 1. Basic CRUD Tests
    // ----------------------------------------------------------------------------

    // ---------- Create Operation Tests ----------
    /**
     * **Save new entity**  
     * Verify that a new MatrixImage entity is correctly persisted.
     */
    @Test
    void testCreateMatrixImage() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);

        MatrixImage image = new MatrixImage();
        image.setCarverMatrix(matrix);
        image.setImageUrl("https://example.com/image.png");

        MatrixImage savedImage = matrixImageRepository.save(image);
        assertNotNull(savedImage.getImageId(), "The image should have a generated ID after save.");
        assertThat(savedImage.getImageUrl()).isEqualTo("https://example.com/image.png");
    }

    // ---------- Read Operation Tests ----------
    /**
     * **Find by ID (positive case)**  
     * Confirm retrieval by primary key returns the correct entity.
     */
    @Test
    void testFindMatrixImageByIdPositive() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);

        MatrixImage image = new MatrixImage();
        image.setCarverMatrix(matrix);
        image.setImageUrl("https://example.com/read.png");
        MatrixImage savedImage = matrixImageRepository.save(image);

        Optional<MatrixImage> foundImage = matrixImageRepository.findById(savedImage.getImageId());
        assertTrue(foundImage.isPresent(), "MatrixImage should be found by its ID.");
        assertThat(foundImage.get().getImageUrl()).isEqualTo("https://example.com/read.png");
    }

    /**
     * **Find by ID (negative case)**  
     * Confirm that querying a non-existent ID returns an empty result.
     */
    @Test
    void testFindMatrixImageByIdNegative() {
        Optional<MatrixImage> foundImage = matrixImageRepository.findById(-1L);
        assertThat(foundImage).isNotPresent();
    }

    // ---------- Update Operation Tests ----------
    /**
     * **Update existing entity**  
     * Verify that changes to a MatrixImage entity are correctly persisted.
     */
    @Test
    void testUpdateMatrixImage() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);

        MatrixImage image = new MatrixImage();
        image.setCarverMatrix(matrix);
        image.setImageUrl("https://example.com/original.png");
        MatrixImage savedImage = matrixImageRepository.save(image);

        // Update the imageUrl.
        savedImage.setImageUrl("https://example.com/updated.png");
        matrixImageRepository.saveAndFlush(savedImage);

        MatrixImage updatedImage = matrixImageRepository.findById(savedImage.getImageId())
            .orElseThrow(() -> new RuntimeException("MatrixImage not found"));
        assertThat(updatedImage.getImageUrl()).isEqualTo("https://example.com/updated.png");
    }

    // ---------- Delete Operation Tests ----------
    /**
     * **Delete existing entity**  
     * Confirm that a MatrixImage entity is removed from the repository after deletion.
     */
    @Test
    void testDeleteMatrixImage() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);

        MatrixImage image = new MatrixImage();
        image.setCarverMatrix(matrix);
        image.setImageUrl("https://example.com/delete.png");
        MatrixImage savedImage = matrixImageRepository.save(image);

        matrixImageRepository.delete(savedImage);
        Optional<MatrixImage> fetchedImage = matrixImageRepository.findById(savedImage.getImageId());
        assertThat(fetchedImage).isNotPresent();
    }

    /**
     * **Delete non-existent entity**  
     * Confirm that attempting to delete a non-persisted MatrixImage does not throw unexpected exceptions.
     */
    @Test
    void testDeleteNonExistentMatrixImage() {
        MatrixImage nonExistentImage = new MatrixImage();
        // Not persisting the entity.
        assertDoesNotThrow(() -> matrixImageRepository.delete(nonExistentImage));
    }

    // ----------------------------------------------------------------------------
    // ✅ 2. Constraint Validation Tests
    // ----------------------------------------------------------------------------

    /**
     * **Non-null Constraint Violation**  
     * Verify that null values for required fields are rejected.
     * In this case, imageUrl is marked as @NonNull.
     */
    @Test
    void testNonNullConstraintViolationForMatrixImage() {
        MatrixImage image = new MatrixImage();
        // Not setting imageUrl (and carverMatrix is required as per our mapping)
        assertThrows(DataIntegrityViolationException.class, () -> matrixImageRepository.saveAndFlush(image));
    }

    /**
     * **Length Constraint Violation**  
     * Confirm that values exceeding defined length limits are not persisted.
     * Here, imageUrl should be at most 500 characters.
     */
    @Test
    void testLengthConstraintViolationForMatrixImage() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);

        MatrixImage image = new MatrixImage();
        image.setCarverMatrix(matrix);
        // Create a string that exceeds 500 characters.
        String longUrl = "http://example.com/" + "a".repeat(490);
        image.setImageUrl(longUrl);
        assertThrows(DataIntegrityViolationException.class, () -> matrixImageRepository.saveAndFlush(image));
    }

    // ----------------------------------------------------------------------------
    // ✅ 3. Bulk Operations Tests
    // ----------------------------------------------------------------------------

    /**
     * **SaveAll and DeleteAll Operations**  
     * Validate that bulk insertions and deletions function correctly.
     */
    @Test
    void testBulkOperationsForMatrixImages() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);

        MatrixImage image1 = new MatrixImage();
        image1.setCarverMatrix(matrix);
        image1.setImageUrl("https://example.com/bulk1.png");

        MatrixImage image2 = new MatrixImage();
        image2.setCarverMatrix(matrix);
        image2.setImageUrl("https://example.com/bulk2.png");

        matrixImageRepository.saveAll(List.of(image1, image2));
        List<MatrixImage> images = matrixImageRepository.findAll();
        assertThat(images.size()).isGreaterThanOrEqualTo(2);

        matrixImageRepository.deleteAll(List.of(image1, image2));
        Optional<MatrixImage> fetched1 = matrixImageRepository.findById(image1.getImageId());
        Optional<MatrixImage> fetched2 = matrixImageRepository.findById(image2.getImageId());
        assertThat(fetched1).isNotPresent();
        assertThat(fetched2).isNotPresent();
    }

    /**
     * **Bulk Operations Edge Case Test**  
     * Verify that calling bulk operations with an empty list behaves as expected.
     */
    @Test
    void testBulkOperationsEmptyForMatrixImages() {
        List<MatrixImage> emptyList = new ArrayList<>();
        assertDoesNotThrow(() -> matrixImageRepository.saveAll(emptyList));
        assertDoesNotThrow(() -> matrixImageRepository.deleteAll(emptyList));
    }

    // ----------------------------------------------------------------------------
    // ✅ 4. Transactional Tests
    // ----------------------------------------------------------------------------

    /**
     * **Transactional Rollback**  
     * Simulate an exception within a transaction to verify that the repository rolls back the transaction,
     * leaving the database state unchanged.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testTransactionalRollbackForMatrixImages() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);

        MatrixImage image = new MatrixImage();
        image.setCarverMatrix(matrix);
        image.setImageUrl("https://example.com/tx.png");
        MatrixImage savedImage = matrixImageRepository.save(image);
        Long imageId = savedImage.getImageId();
        String originalUrl = savedImage.getImageUrl();

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(status -> {
            MatrixImage imageToUpdate = matrixImageRepository.findById(imageId)
                    .orElseThrow(() -> new RuntimeException("MatrixImage not found"));
            imageToUpdate.setImageUrl("https://example.com/updated_tx.png");
            matrixImageRepository.saveAndFlush(imageToUpdate);
            status.setRollbackOnly();
            return null;
        });

        MatrixImage fetchedImage = matrixImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("MatrixImage not found"));
        assertThat(fetchedImage.getImageUrl()).isEqualTo(originalUrl);
    }

    // ----------------------------------------------------------------------------
    // ✅ 5. Pagination and Sorting Tests
    // ----------------------------------------------------------------------------

    /**
     * **Pagination and Sorting (Ascending)**  
     * Validate that pagination and ascending sorting by imageUrl work as expected.
     */
    @Test
    void testPaginationAndSortingAscendingForMatrixImages() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);
        // Create 10 images with sequential URLs.
        for (int i = 1; i <= 10; i++) {
            MatrixImage image = new MatrixImage();
            image.setCarverMatrix(matrix);
            image.setImageUrl("https://example.com/image" + i + ".png");
            matrixImageRepository.save(image);
        }
        Pageable pageable = PageRequest.of(0, 5, Sort.by("imageUrl").ascending());
        Page<MatrixImage> page = matrixImageRepository.findAll(pageable);
        assertThat(page.getContent()).hasSize(5);
        List<MatrixImage> sortedImages = page.getContent();
        for (int i = 1; i < sortedImages.size(); i++) {
            assertThat(sortedImages.get(i - 1).getImageUrl().compareTo(sortedImages.get(i).getImageUrl()))
                .isLessThanOrEqualTo(0);
        }
    }

    /**
     * **Sorting (Descending)**  
     * Confirm that sorting by imageUrl in descending order returns results in the expected order.
     */
    @Test
    void testSortingDescendingForMatrixImages() {
        // Clear any existing images.
        matrixImageRepository.deleteAll();
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);
        // Create 5 images.
        for (int i = 1; i <= 5; i++) {
            MatrixImage image = new MatrixImage();
            image.setCarverMatrix(matrix);
            image.setImageUrl("https://example.com/image" + i + ".png");
            matrixImageRepository.save(image);
        }
        Pageable pageable = PageRequest.of(0, 5, Sort.by("imageUrl").descending());
        Page<MatrixImage> page = matrixImageRepository.findAll(pageable);
        List<MatrixImage> sortedImages = page.getContent();
        for (int i = 1; i < sortedImages.size(); i++) {
            assertThat(sortedImages.get(i - 1).getImageUrl().compareTo(sortedImages.get(i).getImageUrl()))
                .isGreaterThanOrEqualTo(0);
        }
    }

    // ----------------------------------------------------------------------------
    // ✅ 6. Auditing and Optional Field Tests
    // ----------------------------------------------------------------------------

    /**
     * **Creation Timestamp**  
     * Verify that the uploadedAt field is automatically populated upon entity creation.
     */
    @Test
    void testCreationTimestampForMatrixImages() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);

        MatrixImage image = new MatrixImage();
        image.setCarverMatrix(matrix);
        image.setImageUrl("https://example.com/timestamp.png");
        MatrixImage savedImage = matrixImageRepository.save(image);
        assertNotNull(savedImage.getUploadedAt(), "The uploadedAt field should be automatically populated.");
    }

    // ----------------------------------------------------------------------------
    // ✅ 7. Edge Case and Exception Handling Tests
    // ----------------------------------------------------------------------------

    /**
     * **Special Characters and Unicode**  
     * Confirm that special and Unicode characters in imageUrl are persisted correctly.
     */
    @Test
    void testSpecialCharactersForMatrixImages() {
        User2 user = createAndPersistUser();
        CarverMatrix matrix = createAndPersistMatrix(user);

        MatrixImage image = new MatrixImage();
        image.setCarverMatrix(matrix);
        String specialUrl = "https://example.com/测试-テスト-اختبار.png";
        image.setImageUrl(specialUrl);
        MatrixImage savedImage = matrixImageRepository.save(image);
        assertThat(savedImage.getImageUrl()).isEqualTo(specialUrl);
    }

    /**
     * **Save Null Entity Exception Test**  
     * Verify that passing a null entity to the save method throws an InvalidDataAccessApiUsageException.
     */
    @Test
    void testSaveNullMatrixImageEntity() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> matrixImageRepository.save(null));
    }

    /**
     * **Find by ID with Null Exception Test**  
     * Verify that passing a null ID to findById throws an InvalidDataAccessApiUsageException.
     */
    @Test
    void testFindMatrixImageByIdWithNull() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> matrixImageRepository.findById(null));
    }

    /**
     * **Invalid Pagination Parameters**  
     * Verify that negative pagination parameters throw an IllegalArgumentException.
     */
    @Test
    void testInvalidPaginationParametersForMatrixImages() {
        assertThrows(IllegalArgumentException.class, () -> {
            Pageable pageable = PageRequest.of(-1, 5, Sort.by("imageUrl").ascending());
            matrixImageRepository.findAll(pageable);
        });
    }
}
