package com.fmc.starterApp.models.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link MatrixImage} entity.
 *
 * <p>This test class verifies the behavior of the MatrixImage model, including:
 * <ul>
 *   <li>Basic instantiation using both the no-args and all-args constructors and verifying getter/setter functionality.</li>
 *   <li>Enforcement of non-null constraints on mandatory fields (specifically, {@code imageUrl}).</li>
 *   <li>Verification of length constraints for {@code imageUrl} (limited to 500 characters).</li>
 *   <li>Default population of the {@code uploadedAt} field and its manual override.</li>
 *   <li>Testing that the {@code toString()} method returns a meaningful representation of the entity.</li>
 *   <li>Edge case tests:
 *         <ul>
 *             <li>Empty string values for {@code imageUrl}.</li>
 *             <li>Handling of special characters and Unicode in {@code imageUrl}.</li>
 *             <li>Boundary value tests for {@code imageUrl} length.</li>
 *             <li>Setter override behavior for {@code imageUrl}.</li>
 *             <li>Verifying that {@code carverMatrix} is nullable.</li>
 *         </ul>
 *   </li>
 * </ul>
 * </p>
 */
public class MatrixImageUnitTest {

    // =========================================================================
    // ✅ 1. Basic Instantiation and Field Tests
    // =========================================================================

    @Test
    void testNoArgsConstructorAndSetters() {
        MatrixImage image = new MatrixImage();
        
        // Create a dummy CarverMatrix for testing.
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(1L);
        matrix.setName("Test Matrix");
        
        // Use setters to initialize mandatory fields.
        image.setCarverMatrix(matrix);
        image.setImageUrl("http://example.com/image.png");

        // Verify getters.
        assertEquals(matrix, image.getCarverMatrix());
        assertEquals("http://example.com/image.png", image.getImageUrl());
        assertNotNull(image.getUploadedAt(), "uploadedAt should be automatically set");
    }

    @Test
    void testAllArgsConstructor() {
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(2L);
        matrix.setName("Matrix2");

        LocalDateTime now = LocalDateTime.now();
        MatrixImage image = new MatrixImage(10L, matrix, "http://example.com/image.png", now);

        assertEquals(10L, image.getImageId());
        assertEquals(matrix, image.getCarverMatrix());
        assertEquals("http://example.com/image.png", image.getImageUrl());
        assertEquals(now, image.getUploadedAt());
    }

    // =========================================================================
    // ✅ 2. Constraint Validation Tests
    // =========================================================================

    @Test
    void testSetterNonNullEnforcement() {
        MatrixImage image = new MatrixImage();

        // Since carverMatrix is now nullable, setting it to null should be allowed.
        image.setCarverMatrix(null);
        assertNull(image.getCarverMatrix());

        // Expect exception when setting imageUrl to null.
        NullPointerException ex = assertThrows(NullPointerException.class, () -> image.setImageUrl(null));
        assertTrue(ex.getMessage().contains("imageUrl"));
    }

    @Test
    void testAllArgsConstructorNonNullEnforcement() {
        LocalDateTime now = LocalDateTime.now();
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(3L);
        matrix.setName("Matrix3");

        // Passing null for imageUrl should trigger a NullPointerException.
        NullPointerException ex = assertThrows(NullPointerException.class, () ->
            new MatrixImage(1L, matrix, null, now));
        assertTrue(ex.getMessage().contains("imageUrl"));
    }

    // New test: Verify that carverMatrix is nullable.
    @Test
    void testCarverMatrixNullable() {
        MatrixImage image = new MatrixImage();
        image.setImageUrl("http://example.com/image.png");
        image.setCarverMatrix(null);
        assertNull(image.getCarverMatrix(), "carverMatrix should be nullable");
    }

    @Test
    void testLengthConstraintForImageUrl() {
        MatrixImage image = new MatrixImage();
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(4L);
        matrix.setName("Matrix4");
        image.setCarverMatrix(matrix);

        // Set imageUrl with exactly 500 characters.
        String fiveHundredChars = "A".repeat(500);
        image.setImageUrl(fiveHundredChars);
        assertEquals(500, image.getImageUrl().length());

        // Set imageUrl with 501 characters.
        String longUrl = "A".repeat(501);
        image.setImageUrl(longUrl);
        // At the entity level, the value is stored as set; enforcement occurs at the database level.
        assertEquals(501, image.getImageUrl().length());
    }

    // =========================================================================
    // ✅ 3. Default Values and Auditing Tests
    // =========================================================================

    @Test
    void testDefaultUploadedAt() {
        MatrixImage image = new MatrixImage();
        assertNotNull(image.getUploadedAt(), "uploadedAt should be automatically set");
    }

    @Test
    void testUploadedAtManualOverride() {
        MatrixImage image = new MatrixImage();
        LocalDateTime customTime = LocalDateTime.of(2020, 1, 1, 0, 0);
        image.setUploadedAt(customTime);
        assertEquals(customTime, image.getUploadedAt());
    }

    // =========================================================================
    // ✅ 4. Equality and toString Tests
    // =========================================================================

    @Test
    void testToStringMethod() {
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(5L);
        matrix.setName("MatrixTest");
        LocalDateTime now = LocalDateTime.now();
        MatrixImage image = new MatrixImage(20L, matrix, "http://example.com/image.png", now);
        String str = image.toString();
        assertNotNull(str, "toString() should not return null");
        assertTrue(str.contains("http://example.com/image.png"), "toString() should include the imageUrl");
    }

    // =========================================================================
    // ✅ 5. Edge Case Tests
    // =========================================================================

    @Test
    void testEmptyStringValues() {
        MatrixImage image = new MatrixImage();
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(6L);
        matrix.setName("MatrixEdge");
        image.setCarverMatrix(matrix);
        image.setImageUrl("");

        assertEquals("", image.getImageUrl());
    }

    @Test
    void testSpecialCharactersAndUnicode() {
        MatrixImage image = new MatrixImage();
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(7L);
        matrix.setName("MatrixUnicode");
        image.setCarverMatrix(matrix);
        String specialUrl = "http://例子.测试/图片.png";
        image.setImageUrl(specialUrl);
        assertEquals(specialUrl, image.getImageUrl());
    }

    @Test
    void testBoundaryValueForImageUrlLength() {
        MatrixImage image = new MatrixImage();
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(8L);
        matrix.setName("MatrixBoundary");
        image.setCarverMatrix(matrix);
        
        // Exactly 500 characters.
        String fiveHundredChars = "A".repeat(500);
        image.setImageUrl(fiveHundredChars);
        assertEquals(500, image.getImageUrl().length());

        // More than 500 characters.
        String longUrl = "A".repeat(510);
        image.setImageUrl(longUrl);
        assertEquals(510, image.getImageUrl().length());
    }

    @Test
    void testSetterOverride() {
        MatrixImage image = new MatrixImage();
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(9L);
        matrix.setName("MatrixOverride");
        image.setCarverMatrix(matrix);
        image.setImageUrl("http://initial.com/image.png");
        assertEquals("http://initial.com/image.png", image.getImageUrl());
        image.setImageUrl("http://updated.com/image.png");
        assertEquals("http://updated.com/image.png", image.getImageUrl());
    }
}
