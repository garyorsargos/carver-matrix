package com.fmc.starterApp.services;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.fmc.starterApp.models.entity.CarverMatrix;
import com.fmc.starterApp.models.entity.MatrixImage;
import com.fmc.starterApp.repositories.CarverMatrixRepository;
import com.fmc.starterApp.repositories.MatrixImageRepository;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Integration tests for {@link ImageService}, verifying that the service layer:
 * <ul>
 *   <li>Executes its methods correctly and integrates with the underlying repositories (or external systems).</li>
 *   <li>Validates input parameters and handles errors gracefully.</li>
 *   <li>Manages transactions correctly.</li>
 * </ul>
 *
 * <p>This test class uses an in-memory H2 database and a mocked S3Client. Tests are organized into:
 * <ol>
 *   <li>Basic Functionality Tests</li>
 *   <li>Input Validation Tests</li>
 *   <li>Business Logic Tests</li>
 *   <li>Transactional and Integration Tests</li>
 *   <li>Edge Case and Exception Handling Tests</li>
 *   <li>Caching and Performance Tests (if applicable)</li>
 * </ol>
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class ImageServiceTest {

    @Autowired
    private ImageService imageService;

    @Autowired
    private MatrixImageRepository matrixImageRepository;

    @Autowired
    private CarverMatrixRepository carverMatrixRepository;

    @MockBean
    private S3Client s3Client;

    // =========================================================================
    // ✅ 1. Basic Functionality Tests (Unit Test)
    // =========================================================================

    // ---------- Method Invocation Tests ----------
    /**
     * **uploadImage - Valid Input Test**
     * Verify that uploadImage executes successfully with valid input and returns a non-null URL.
     */
    @Test
    @Transactional
    void testUploadImage_ValidInput() throws IOException {
        // Arrange: create and persist a CarverMatrix.
        CarverMatrix matrix = new CarverMatrix();
        matrix.setName("Test Matrix");
        matrix = carverMatrixRepository.save(matrix);

        // Create a valid MultipartFile.
        MockMultipartFile file = new MockMultipartFile("file", "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE, "image content".getBytes());

        // Act: call uploadImage.
        String url = imageService.uploadImage(file, matrix.getMatrixId());

        // Assert: verify that the URL is non-null.
        assertNotNull(url, "Expected a non-null URL for the uploaded image");

        // Also verify that metadata is persisted.
        List<MatrixImage> images = matrixImageRepository.findAll();
        assertFalse(images.isEmpty(), "Expected at least one MatrixImage in the repository");
        MatrixImage savedImage = images.get(0);
        assertEquals(url, savedImage.getImageUrl(), "The saved image URL should match the returned URL");
        assertEquals(matrix.getMatrixId(), savedImage.getCarverMatrix().getMatrixId(), "The MatrixImage should be associated with the correct CarverMatrix");
    }

    // ---------- Invalid Input Test ----------
    /**
     * **uploadImage - Null File Test**
     * Verify that passing a null MultipartFile to uploadImage throws an IllegalArgumentException.
     */
    @Test
    void testUploadImage_NullFile() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> imageService.uploadImage(null, 1L),
                "Expected uploadImage to throw IllegalArgumentException for null file");
        assertThat(ex.getMessage()).contains("MultipartFile must not be null");
    }

    /**
     * **uploadImage - Null MatrixId Test**
     * Verify that passing a null matrixId to uploadImage throws an IllegalArgumentException.
     */
    @Test
    void testUploadImage_NullMatrixId() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg",
                MediaType.IMAGE_JPEG_VALUE, "content".getBytes());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> imageService.uploadImage(file, null),
                "Expected uploadImage to throw IllegalArgumentException for null matrixId");
        assertThat(ex.getMessage()).contains("MatrixId must not be null");
    }

    /**
     * **uploadImage - Invalid File Name Test**
     * Verify that if the file's original filename is null or empty, an IllegalArgumentException is thrown.
     */
    @Test
    void testUploadImage_InvalidFileName() {
        // Create a file with an empty filename.
        MockMultipartFile file = new MockMultipartFile("file", "",
                MediaType.IMAGE_JPEG_VALUE, "content".getBytes());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> imageService.uploadImage(file, 1L),
                "Expected uploadImage to throw IllegalArgumentException for invalid file name");
        assertThat(ex.getMessage()).contains("Invalid file name");
    }

    // =========================================================================
    // ✅ 2. Business Logic Tests (Unit Test)
    // =========================================================================

    // ---------- Correct Calculation/Processing Test ----------
    /**
     * **uploadImage - Filename Cleaning Test**
     * Verify that the filename is cleaned by replacing invalid characters with underscores.
     */
    @Test
    @Transactional
    void testUploadImage_FilenameCleaning() throws IOException {
        // Arrange: create a CarverMatrix.
        CarverMatrix matrix = new CarverMatrix();
        matrix.setName("Test Matrix");
        matrix = carverMatrixRepository.save(matrix);

        // Create a file with special characters in its name.
        String originalFilename = "test@image#1!.jpg";
        MockMultipartFile file = new MockMultipartFile("file", originalFilename,
                MediaType.IMAGE_JPEG_VALUE, "dummy content".getBytes());

        // Act: call uploadImage.
        String url = imageService.uploadImage(file, matrix.getMatrixId());

        // Assert: the URL should contain the cleaned filename (invalid characters replaced by underscores).
        assertNotNull(url);
        assertTrue(url.contains("_image_1_.jpg"), "Filename should be cleaned in the URL");
    }

    // ---------- Conditional Flow Test ----------
    /**
     * **uploadImage - S3 Upload Failure Test**
     * Verify that if the S3 upload fails, a RuntimeException is thrown.
     */
    @Test
    @Transactional
    void testUploadImage_S3UploadFailure() throws IOException {
        // Arrange: create a CarverMatrix.
        CarverMatrix matrix = new CarverMatrix();
        matrix.setName("Test Matrix for S3 Failure");
        CarverMatrix savedMatrix = carverMatrixRepository.save(matrix);

        MockMultipartFile file = new MockMultipartFile("file", "fail.jpg",
                MediaType.IMAGE_JPEG_VALUE, "content".getBytes());

        // Force the s3Client.putObject method to throw an exception.
        doThrow(new RuntimeException("S3 failure")).when(s3Client)
                .putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // Act & Assert: expect a RuntimeException.
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> imageService.uploadImage(file, savedMatrix.getMatrixId()),
                "Expected uploadImage to throw RuntimeException when S3 upload fails");
        assertThat(ex.getMessage()).contains("Failed to upload file to S3");
    }

    // =========================================================================
    // ✅ 3. Transactional and Integration Tests
    // =========================================================================

    // ---------- End-to-End Service Integration Test ----------
    /**
     * **uploadImage - End-to-End Integration Test**
     * Verify that a valid image upload persists metadata in the database and returns a valid URL.
     */
    @Test
    @Transactional
    void testUploadImage_EndToEndIntegration() throws IOException {
        // Arrange: create a CarverMatrix.
        CarverMatrix matrix = new CarverMatrix();
        matrix.setName("Integration Matrix");
        CarverMatrix matrixSaved = carverMatrixRepository.save(matrix);

        MockMultipartFile file = new MockMultipartFile("file", "integration.jpg",
                MediaType.IMAGE_JPEG_VALUE, "integration content".getBytes());

        // Act: upload the image.
        String url = imageService.uploadImage(file, matrixSaved.getMatrixId());

        // Assert: verify the returned URL is non-null and that a MatrixImage is saved.
        assertNotNull(url);
        List<MatrixImage> images = matrixImageRepository.findAll();
        assertThat(images).extracting(MatrixImage::getImageUrl).contains(url);
    }

    // =========================================================================
    // ✅ 4. Edge Case and Exception Handling Tests (Unit Test)
    // =========================================================================

    // ---------- Null Input Exception Test ----------
    /**
     * **uploadImage - Null File Exception Test**
     * Verify that uploadImage throws an IllegalArgumentException when the file is null.
     */
    @Test
    void testUploadImage_ThrowsExceptionForNullFile() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> imageService.uploadImage(null, 1L),
                "Expected uploadImage to throw IllegalArgumentException for null file");
        assertThat(ex.getMessage()).contains("MultipartFile must not be null");
    }

    /**
     * **uploadImage - Null MatrixId Exception Test**
     * Verify that uploadImage throws an IllegalArgumentException when the matrixId is null.
     */
    @Test
    void testUploadImage_ThrowsExceptionForNullMatrixId() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg",
                MediaType.IMAGE_JPEG_VALUE, "content".getBytes());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> imageService.uploadImage(file, null),
                "Expected uploadImage to throw IllegalArgumentException for null matrixId");
        assertThat(ex.getMessage()).contains("MatrixId must not be null");
    }

    // ---------- Unexpected Error Handling Test ----------
    /**
     * **uploadImage - Unexpected Error Handling Test**
     * Verify that unexpected errors (such as S3 upload failures) are propagated with meaningful messages.
     */
    @Test
    void testUploadImage_UnexpectedErrorHandling() throws IOException {
        // Arrange: create a CarverMatrix.
        CarverMatrix matrix = new CarverMatrix();
        matrix.setName("Unexpected Error Matrix");
        CarverMatrix matrixSaved = carverMatrixRepository.save(matrix);

        // Create a valid file.
        MockMultipartFile file = new MockMultipartFile("file", "unexpected.jpg",
                MediaType.IMAGE_JPEG_VALUE, "content".getBytes());

        // Force the S3 client to throw an exception.
        doThrow(new RuntimeException("Unexpected S3 error")).when(s3Client)
                .putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // Act & Assert.
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> imageService.uploadImage(file, matrixSaved.getMatrixId()),
                "Expected uploadImage to throw RuntimeException for unexpected errors");
        assertThat(ex.getMessage()).contains("Failed to upload file to S3");
    }

    // =========================================================================
    // ✅ 5. Caching and Performance Tests (if applicable)
    // =========================================================================
    // (Not applicable unless caching is implemented in the service layer.)
}
