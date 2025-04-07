package com.fmc.starterApp.models.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link CarverItem} entity.
 *
 * <p>This test class verifies the behavior of the CarverItem model, including:
 * <ul>
 *   <li>Basic instantiation using both the no-args and all-args constructors, and correct assignment via getters/setters.</li>
 *   <li>Enforcement of non-null constraints on mandatory fields (e.g., {@code itemName}); note that {@code carverMatrix} is now nullable to allow removal.</li>
 *   <li>Verification of length constraints for {@code itemName} (limited to 100 characters).</li>
 *   <li>Default population of the {@code createdAt} field.</li>
 *   <li>Testing that the {@code toString()} method returns a meaningful representation.</li>
 *   <li>Edge case tests:
 *         <ul>
 *             <li>Empty string values, special characters/unicode, and boundary length values for {@code itemName}.</li>
 *             <li>Setter override behavior.</li>
 *             <li>Bidirectional relationship management with {@link CarverMatrix}.</li>
 *             <li>Metric fields (criticality, accessibility, recoverability, vulnerability, effect, recognizability) default to empty maps if not set.</li>
 *             <li>Target Users field: handling null, empty, and non-empty arrays.</li>
 *             <li>Manual overriding of the {@code createdAt} value.</li>
 *         </ul>
 *   </li>
 * </ul>
 * </p>
 */
public class CarverItemUnitTest {

    // =========================================================================
    // ✅ 1. Basic Instantiation and Field Tests
    // =========================================================================

    @Test
    void testNoArgsConstructorAndSetters() {
        CarverItem item = new CarverItem();
        
        // Create a dummy CarverMatrix for the mandatory relationship.
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(1L);
        matrix.setName("Test Matrix");
        item.setCarverMatrix(matrix);
        
        // Set mandatory field.
        item.setItemName("Test Item");

        // Set some metric values as maps.
        Map<String, Integer> crit = new HashMap<>();
        crit.put("score", 5);
        item.setCriticality(crit);
        
        Map<String, Integer> acc = new HashMap<>();
        acc.put("score", 3);
        item.setAccessibility(acc);
        
        Map<String, Integer> recov = new HashMap<>();
        recov.put("score", 4);
        item.setRecoverability(recov);
        
        Map<String, Integer> vul = new HashMap<>();
        vul.put("score", 2);
        item.setVulnerability(vul);
        
        Map<String, Integer> eff = new HashMap<>();
        eff.put("score", 3);
        item.setEffect(eff);
        
        Map<String, Integer> recog = new HashMap<>();
        recog.put("score", 1);
        item.setRecognizability(recog);
        
        // Set targetUsers.
        String[] targets = {"user1@example.com", "user2@example.com"};
        item.setTargetUsers(targets);

        // Verify getters return expected values.
        assertEquals(matrix, item.getCarverMatrix());
        assertEquals("Test Item", item.getItemName());
        assertEquals(5, item.getCriticality().get("score"));
        assertEquals(3, item.getAccessibility().get("score"));
        assertEquals(4, item.getRecoverability().get("score"));
        assertEquals(2, item.getVulnerability().get("score"));
        assertEquals(3, item.getEffect().get("score"));
        assertEquals(1, item.getRecognizability().get("score"));
        assertArrayEquals(targets, item.getTargetUsers());
        assertNotNull(item.getCreatedAt(), "createdAt should be automatically set");
    }

    @Test
    void testAllArgsConstructor() {
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(2L);
        matrix.setName("Matrix2");

        String[] targets = {"target@example.com"};
        LocalDateTime now = LocalDateTime.now();

        Map<String, Integer> crit = new HashMap<>();
        crit.put("score", 4);
        Map<String, Integer> acc = new HashMap<>();
        acc.put("score", 3);
        Map<String, Integer> recov = new HashMap<>();
        recov.put("score", 2);
        Map<String, Integer> vul = new HashMap<>();
        vul.put("score", 1);
        Map<String, Integer> eff = new HashMap<>();
        eff.put("score", 5);
        Map<String, Integer> recog = new HashMap<>();
        recog.put("score", 6);
        ArrayList<String> images = new ArrayList<String>();

        CarverItem item = new CarverItem(10L, matrix, "AllArgsItem", crit, acc, recov, vul, eff, recog, targets, images, now);

        assertEquals(10L, item.getItemId());
        assertEquals(matrix, item.getCarverMatrix());
        assertEquals("AllArgsItem", item.getItemName());
        assertEquals(4, item.getCriticality().get("score"));
        assertEquals(3, item.getAccessibility().get("score"));
        assertEquals(2, item.getRecoverability().get("score"));
        assertEquals(1, item.getVulnerability().get("score"));
        assertEquals(5, item.getEffect().get("score"));
        assertEquals(6, item.getRecognizability().get("score"));
        assertArrayEquals(targets, item.getTargetUsers());
        assertEquals(now, item.getCreatedAt());
    }

    // =========================================================================
    // ✅ 2. Constraint Validation Tests
    // =========================================================================

    /**
     * Tests that setting mandatory fields to null via setters behaves as expected.
     * Since carverMatrix is now nullable (to allow removal), we only expect a NullPointerException for itemName.
     */
    @Test
    void testSetterNonNullEnforcement() {
        CarverItem item = new CarverItem();

        // Setting carverMatrix to null should be allowed.
        item.setCarverMatrix(null);
        assertNull(item.getCarverMatrix());

        // Expect exception when setting itemName to null.
        NullPointerException ex = assertThrows(NullPointerException.class, () -> item.setItemName(null));
        assertTrue(ex.getMessage().contains("itemName"));
    }

    @Test
    void testAllArgsConstructorNonNullEnforcement() {
        LocalDateTime now = LocalDateTime.now();
        String[] targets = {"user@example.com"};
        
        // Create a dummy CarverMatrix.
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(3L);
        matrix.setName("Matrix3");

        // Passing null for itemName should trigger a NullPointerException.
        NullPointerException ex = assertThrows(NullPointerException.class, () ->
            new CarverItem(1L, matrix, null, new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), targets, new ArrayList<String>(),now));
        assertTrue(ex.getMessage().contains("itemName"));
    }

    @Test
    void testLengthConstraintForItemName() {
        CarverItem item = new CarverItem();
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(4L);
        matrix.setName("Matrix4");
        item.setCarverMatrix(matrix);

        // Set itemName with exactly 100 characters.
        String hundredChars = "A".repeat(100);
        item.setItemName(hundredChars);
        assertEquals(100, item.getItemName().length());

        // Set itemName with 101 characters.
        String longString = "A".repeat(101);
        item.setItemName(longString);
        // At the entity level, the value is stored as set; enforcement occurs at the database level.
        assertEquals(101, item.getItemName().length());
    }

    // =========================================================================
    // ✅ 3. Default Values and Auditing Tests
    // =========================================================================

    @Test
    void testDefaultCreatedAt() {
        CarverItem item = new CarverItem();
        assertNotNull(item.getCreatedAt(), "createdAt should be automatically set");
    }

    @Test
    void testCreatedAtManualOverride() {
        CarverItem item = new CarverItem();
        LocalDateTime customTime = LocalDateTime.of(2020, 1, 1, 0, 0);
        item.setCreatedAt(customTime);
        assertEquals(customTime, item.getCreatedAt());
    }

    // =========================================================================
    // ✅ 4. Equality and toString Tests
    // =========================================================================

    @Test
    void testToStringMethod() {
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(5L);
        matrix.setName("MatrixTest");
        String[] targets = {"target@example.com"};
        LocalDateTime now = LocalDateTime.now();

        Map<String, Integer> crit = new HashMap<>();
        crit.put("score", 2);
        Map<String, Integer> acc = new HashMap<>();
        acc.put("score", 2);
        Map<String, Integer> recov = new HashMap<>();
        recov.put("score", 2);
        Map<String, Integer> vul = new HashMap<>();
        vul.put("score", 2);
        Map<String, Integer> eff = new HashMap<>();
        eff.put("score", 2);
        Map<String, Integer> recog = new HashMap<>();
        recog.put("score", 2);
        ArrayList<String> images = new ArrayList<String>();

        CarverItem item = new CarverItem(20L, matrix, "ToStringItem", crit, acc, recov, vul, eff, recog, targets, images, now);
        String str = item.toString();
        assertNotNull(str);
        assertTrue(str.contains("ToStringItem"), "toString() should include the itemName");
    }

    // =========================================================================
    // ✅ 5. Edge Case Tests
    // =========================================================================

    @Test
    void testEmptyStringValues() {
        CarverItem item = new CarverItem();
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(6L);
        matrix.setName("MatrixEdge");
        item.setCarverMatrix(matrix);
        item.setItemName("");

        assertEquals("", item.getItemName());
    }

    @Test
    void testSpecialCharactersAndUnicode() {
        CarverItem item = new CarverItem();
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(7L);
        matrix.setName("MatrixUnicode");
        item.setCarverMatrix(matrix);
        String specialName = "测试 - テスト - اختبار";
        item.setItemName(specialName);
        assertEquals(specialName, item.getItemName());
    }

    @Test
    void testBoundaryValueForItemNameLength() {
        CarverItem item = new CarverItem();
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(8L);
        matrix.setName("MatrixBoundary");
        item.setCarverMatrix(matrix);
        
        // Exactly 100 characters.
        String hundredChars = "A".repeat(100);
        item.setItemName(hundredChars);
        assertEquals(100, item.getItemName().length());

        // More than 100 characters.
        String longString = "A".repeat(110);
        item.setItemName(longString);
        assertEquals(110, item.getItemName().length());
    }

    @Test
    void testSetterOverride() {
        CarverItem item = new CarverItem();
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(9L);
        matrix.setName("MatrixOverride");
        item.setCarverMatrix(matrix);
        item.setItemName("InitialItem");
        assertEquals("InitialItem", item.getItemName());
        item.setItemName("UpdatedItem");
        assertEquals("UpdatedItem", item.getItemName());
    }

    @Test
    void testCarverMatrixBidirectionalRelationship() {
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(10L);
        matrix.setName("BidirectionalTest");

        CarverItem item = new CarverItem();
        item.setItemName("BidirectionalItem");

        // Initially, the item's carverMatrix should be null.
        assertNull(item.getCarverMatrix());

        // Add item to matrix.
        matrix.addItem(item);
        assertTrue(matrix.getItems().contains(item));
        assertEquals(matrix, item.getCarverMatrix());

        // Remove the item.
        matrix.removeItem(item);
        assertFalse(matrix.getItems().contains(item));
        assertNull(item.getCarverMatrix());
    }

    @Test
    void testNumericFieldsDefault() {
        CarverItem item = new CarverItem();
        // Set mandatory fields.
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(11L);
        matrix.setName("NumericTestMatrix");
        item.setCarverMatrix(matrix);
        item.setItemName("NumericTestItem");

        // All numeric metric fields should be empty maps by default.
        assertTrue(item.getCriticality().isEmpty(), "Expected criticality to be an empty map by default");
        assertTrue(item.getAccessibility().isEmpty(), "Expected accessibility to be an empty map by default");
        assertTrue(item.getRecoverability().isEmpty(), "Expected recoverability to be an empty map by default");
        assertTrue(item.getVulnerability().isEmpty(), "Expected vulnerability to be an empty map by default");
        assertTrue(item.getEffect().isEmpty(), "Expected effect to be an empty map by default");
        assertTrue(item.getRecognizability().isEmpty(), "Expected recognizability to be an empty map by default");
    }

    @Test
    void testTargetUsersField() {
        CarverItem item = new CarverItem();
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(12L);
        matrix.setName("TargetUsersTestMatrix");
        item.setCarverMatrix(matrix);
        item.setItemName("TargetUsersTestItem");

        // Expect that, by default, the targetUsers field is an empty array.
        assertArrayEquals(new String[0], item.getTargetUsers(), "Expected default targetUsers to be an empty array");

        // Setting explicitly to an empty array should still be an empty array.
        item.setTargetUsers(new String[0]);
        assertEquals(0, item.getTargetUsers().length, "Expected targetUsers length to be 0 when set to an empty array");

        // Setting to a non-empty array.
        String[] emails = {"email1@example.com", "email2@example.com"};
        item.setTargetUsers(emails);
        assertArrayEquals(emails, item.getTargetUsers(), "Expected targetUsers to equal the provided email array");
    }
}
