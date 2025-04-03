package com.fmc.starterApp.models.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link PostgresExampleObject} entity.
 *
 * <p>This test class verifies the behavior of the PostgresExampleObject model, including:
 * <ul>
 *   <li>Proper initialization using the no-args and all-args constructors, and correct assignment of fields via getters/setters.</li>
 *   <li>Enforcement of non-null constraints (the {@code name} field must not be null).</li>
 *   <li>Default behavior of the primary key (the {@code id} remains null if the entity is not persisted).</li>
 *   <li>Correct behavior of {@code equals()}, {@code hashCode()}, and {@code toString()} methods.</li>
 *   <li>Edge case handling for empty strings, boundary lengths, whitespace, and case sensitivity.</li>
 * </ul>
 * </p>
 */
public class PostgresExampleObjectUnitTest {

    // =========================================================================
    // ✅ 1. Basic Instantiation and Field Tests
    // =========================================================================

    /**
     * Tests that the no-args constructor and subsequent setter calls properly assign values to the fields.
     */
    @Test
    void testNoArgsConstructorAndSetters() {
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setId(1L);
        obj.setName("Example Name");

        assertEquals(1L, obj.getId());
        assertEquals("Example Name", obj.getName());
    }

    /**
     * Tests that the all-args constructor initializes all fields as expected.
     */
    @Test
    void testAllArgsConstructor() {
        PostgresExampleObject obj = new PostgresExampleObject(2L, "All Args Name");
        assertEquals(2L, obj.getId());
        assertEquals("All Args Name", obj.getName());
    }

    // =========================================================================
    // ✅ 2. Constraint Validation Tests
    // =========================================================================

    /**
     * Tests that setting a mandatory field to null via its setter throws a NullPointerException.
     */
    @Test
    void testSetterNonNullEnforcement() {
        PostgresExampleObject obj = new PostgresExampleObject();
        // The name field is marked as @NonNull.
        NullPointerException ex = assertThrows(NullPointerException.class, () -> obj.setName(null));
        assertTrue(ex.getMessage().contains("name"));
    }

    // =========================================================================
    // ✅ 3. Default Values and Auditing Tests
    // =========================================================================

    /**
     * Tests that the primary key (id) remains null if the entity is not persisted.
     */
    @Test
    void testDefaultId() {
        PostgresExampleObject obj = new PostgresExampleObject();
        assertNull(obj.getId(), "Expected id to be null if not persisted");
    }

    // =========================================================================
    // ✅ 4. Equality and toString Tests
    // =========================================================================

    /**
     * Tests that two distinct instances with identical field values are not equal and have different hash codes.
     * This test verifies that object identity is used for equality.
     */
    @Test
    void testEqualsAndHashCodeBehavior() {
        PostgresExampleObject obj1 = new PostgresExampleObject(1L, "TestName");
        PostgresExampleObject obj2 = new PostgresExampleObject(1L, "TestName");
        assertNotEquals(obj1, obj2, "Different instances with identical fields should not be equal");
        assertNotEquals(obj1.hashCode(), obj2.hashCode(), "Hash codes should differ for distinct objects");
    }

    /**
     * Tests that the toString() method returns a meaningful representation of the entity.
     */
    @Test
    void testToStringMethod() {
        PostgresExampleObject obj = new PostgresExampleObject(1L, "TestName");
        String str = obj.toString();
        assertNotNull(str, "toString() should not return null");
        // Check that key field values appear in the toString() output.
        assertTrue(str.contains("TestName"), "toString() should contain the name field value");
    }

    // =========================================================================
    // ✅ 5. Edge Case Tests
    // =========================================================================

    /**
     * Tests that setting the name to an empty string works as expected.
     */
    @Test
    void testEmptyStringValues() {
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName("");
        assertEquals("", obj.getName(), "Name should be an empty string when set to empty");
    }

    /**
     * Tests that a string value exactly at the maximum allowed length is handled correctly,
     * and that a longer string can be set at the entity level (length enforcement occurs at the database level).
     */
    @Test
    void testLongStringValuesExceedingColumnDefinition() {
        // Create a string of exactly 100 characters.
        String hundredChars = "A".repeat(100);
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName(hundredChars);
        assertEquals(100, obj.getName().length(), "Name should be 100 characters long");

        // Now test with a string longer than 100 characters.
        String longString = "A".repeat(101);
        obj.setName(longString);
        assertEquals(101, obj.getName().length(), "Entity accepts strings longer than 100 characters at the entity level");
    }

    /**
     * Tests that a setter call correctly overrides an existing value.
     */
    @Test
    void testSetterOverride() {
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName("InitialName");
        assertEquals("InitialName", obj.getName());
        obj.setName("UpdatedName");
        assertEquals("UpdatedName", obj.getName());
    }

    /**
     * Tests that leading and trailing whitespace in the name field is preserved as provided.
     */
    @Test
    void testWhitespaceHandling() {
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName("  Name With Spaces  ");
        assertEquals("  Name With Spaces  ", obj.getName());
    }

    /**
     * Tests that the name field is case sensitive if no normalization is applied.
     */
    @Test
    void testCaseSensitivity() {
        PostgresExampleObject obj1 = new PostgresExampleObject();
        PostgresExampleObject obj2 = new PostgresExampleObject();
        obj1.setName("CaseSensitive");
        obj2.setName("casesensitive");
        assertNotEquals(obj1.getName(), obj2.getName(), "Names differing only in case should be considered distinct");
    }

    /**
     * Tests that the entity correctly handles special characters and Unicode values in the name field.
     */
    @Test
    void testSpecialCharactersAndUnicode() {
        PostgresExampleObject obj = new PostgresExampleObject();
        String specialName = "测试 - テスト - اختبار";
        obj.setName(specialName);
        assertEquals(specialName, obj.getName(), "The name should correctly store special and Unicode characters");
    }

}
