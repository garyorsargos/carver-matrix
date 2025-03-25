package com.fmc.starterApp.models.entity;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

/**
 * Unit tests for the {@link User2} entity.
 * 
 * <p>This test class verifies the behavior of the User2 model, including:
 * <ul>
 *   <li>Proper initialization and setting of fields using the no-args and all-args constructors.</li>
 *   <li>Default initialization of the {@code createdAt} timestamp.</li>
 *   <li>Enforcement of non-null constraints via Lombok's {@code @NonNull} annotation in setters and constructors.</li>
 *   <li>Edge case handling for empty strings, string lengths, special characters, leading/trailing whitespace, and case sensitivity.</li>
 *   <li>Default equals and hashCode behavior (object identity, since custom implementations are not provided).</li>
 *   <li>Email validation using Jakarta Bean Validation annotations.</li>
 *   <li>Additional tests for overriding setters and ensuring auditing field immutability.</li>
 * </ul>
 * 
 * <p>Note: The no-arguments constructor does not enforce non-null constraints; fields marked with
 * {@code @NonNull} will remain {@code null} until explicitly set via setters.</p>
 */
public class User2UnitTest {

    // =========================================================================
    // ✅ 1. Basic Instantiation and Field Tests
    // =========================================================================

    /**
     * Tests that the no-args constructor and subsequent setter calls properly assign values to the fields.
     */
    @Test
    void testNoArgsConstructorAndSetters() {
        User2 user = new User2();
        // Using setters to initialize fields.
        user.setUserId(1L);
        user.setKeycloakId("kc-12345");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setFullName("John Doe");
        user.setUsername("johndoe");
        user.setEmail("johndoe@example.com");

        // Verifying that getters return the correct values.
        assertEquals(1L, user.getUserId());
        assertEquals("kc-12345", user.getKeycloakId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("John Doe", user.getFullName());
        assertEquals("johndoe", user.getUsername());
        assertEquals("johndoe@example.com", user.getEmail());
    }

    /**
     * Tests that the all-args constructor initializes all fields as expected.
     */
    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        User2 user = new User2(1L, "kc-12345", "John", "Doe", "John Doe", "johndoe", "johndoe@example.com", now);

        // Verifying that the all-args constructor initializes each field properly.
        assertEquals(1L, user.getUserId());
        assertEquals("kc-12345", user.getKeycloakId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("John Doe", user.getFullName());
        assertEquals("johndoe", user.getUsername());
        assertEquals("johndoe@example.com", user.getEmail());
        assertEquals(now, user.getCreatedAt());
    }

    // =========================================================================
    // ✅ 2. Constraint Validation Tests
    // =========================================================================

    /**
     * Tests Lombok's {@code @NonNull} enforcement in setters.
     * Setting a non-null field to null should throw a NullPointerException.
     */
    @Test
    void testSetterNonNullEnforcement() {
        User2 user = new User2();

        // Expect a NullPointerException when setting keycloakId to null.
        NullPointerException ex1 = assertThrows(NullPointerException.class, () -> {
            user.setKeycloakId(null);
        });
        assertTrue(ex1.getMessage().contains("keycloakId"));

        // Expect a NullPointerException when setting username to null.
        NullPointerException ex2 = assertThrows(NullPointerException.class, () -> {
            user.setUsername(null);
        });
        assertTrue(ex2.getMessage().contains("username"));

        // Expect a NullPointerException when setting email to null.
        NullPointerException ex3 = assertThrows(NullPointerException.class, () -> {
            user.setEmail(null);
        });
        assertTrue(ex3.getMessage().contains("email"));
    }

    /**
     * Tests Lombok's {@code @NonNull} enforcement in the all-arguments constructor.
     * Passing null for any non-null field should trigger a NullPointerException.
     */
    @Test
    void testAllArgsConstructorNonNullEnforcement() {
        LocalDateTime now = LocalDateTime.now();

        // Passing null for keycloakId.
        NullPointerException ex1 = assertThrows(NullPointerException.class, () -> {
            new User2(1L, null, "John", "Doe", "John Doe", "johndoe", "john@example.com", now);
        });
        assertTrue(ex1.getMessage().contains("keycloakId"));

        // Passing null for username.
        NullPointerException ex2 = assertThrows(NullPointerException.class, () -> {
            new User2(1L, "kc-1", "John", "Doe", "John Doe", null, "john@example.com", now);
        });
        assertTrue(ex2.getMessage().contains("username"));

        // Passing null for email.
        NullPointerException ex3 = assertThrows(NullPointerException.class, () -> {
            new User2(1L, "kc-1", "John", "Doe", "John Doe", "johndoe", null, now);
        });
        assertTrue(ex3.getMessage().contains("email"));
    }

    /**
     * Tests that a valid email passes the validation constraints.
     */
    @Test
    void testValidEmail() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        User2 user = new User2();
        user.setKeycloakId("validKeycloakId");
        user.setUsername("validUsername");
        user.setEmail("valid@example.com");

        Set<ConstraintViolation<User2>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Expected no constraint violations for a valid email");
    }

    /**
     * Tests that an invalid email format triggers validation constraint violations.
     */
    @Test
    void testInvalidEmail() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        User2 user = new User2();
        user.setKeycloakId("validKeycloakId");
        user.setUsername("validUsername");
        user.setEmail("invalid-email");

        Set<ConstraintViolation<User2>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Expected constraint violations for an invalid email");

        boolean emailViolationFound = violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("email"));
        assertTrue(emailViolationFound, "Expected a violation on the email field");
    }
    
    /**
     * Tests that string fields constrained by maximum length behave as expected.
     */
    @Test
    void testMaxLengthStringValues() {
        String fiftyChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwx"; // 50 characters
        User2 user = new User2();
        user.setFirstName(fiftyChars);
        user.setLastName(fiftyChars);
        user.setFullName(fiftyChars);
        user.setUsername(fiftyChars);
        user.setEmail(fiftyChars + "@test.com");

        assertEquals(50, user.getFirstName().length());
        assertEquals(50, user.getLastName().length());
        assertEquals(50, user.getFullName().length());
        assertEquals(50, user.getUsername().length());
    }

    // =========================================================================
    // ✅ 3. Default Values and Auditing Tests
    // =========================================================================

    /**
     * Tests that the {@code createdAt} field is automatically initialized to the current timestamp.
     */
    @Test
    void testDefaultCreatedAt() {
        User2 user = new User2();
        assertNotNull(user.getCreatedAt());

        LocalDateTime oneSecondAgo = LocalDateTime.now().minusSeconds(1);
        LocalDateTime oneSecondLater = LocalDateTime.now().plusSeconds(1);
        assertTrue(user.getCreatedAt().isAfter(oneSecondAgo) && user.getCreatedAt().isBefore(oneSecondLater),
            "createdAt should be set to the current time");
    }

    /**
     * Tests that the {@code createdAt} field can be explicitly set to {@code null}.
     */
    @Test
    void testSetCreatedAtToNull() {
        User2 user = new User2();
        assertNotNull(user.getCreatedAt());
        user.setCreatedAt(null);
        assertNull(user.getCreatedAt());
    }
    
    /**
     * Tests that the {@code createdAt} field remains unchanged after updating other fields.
     */
    @Test
    void testCreatedAtImmutability() {
        User2 user = new User2();
        LocalDateTime originalCreatedAt = user.getCreatedAt();
        user.setUsername("newUsername");
        // Ensure other field updates do not affect createdAt.
        assertEquals(originalCreatedAt, user.getCreatedAt(), "createdAt should remain unchanged after updating other fields");
    }

    // =========================================================================
    // ✅ 4. Equality and toString Tests
    // =========================================================================

    /**
     * Tests the default equals and hashCode behavior.
     * Since no custom implementations are provided, two instances with identical field values are not equal.
     */
    @Test
    void testEqualsAndHashCodeBehavior() {
        LocalDateTime now = LocalDateTime.now();
        User2 user1 = new User2(1L, "kc-1", "John", "Doe", "John Doe", "johndoe", "john@example.com", now);
        User2 user2 = new User2(1L, "kc-1", "John", "Doe", "John Doe", "johndoe", "john@example.com", now);

        assertNotEquals(user1, user2, "Different instances should not be equal without custom equals implementation");
        assertEquals(user1, user1, "The same instance should be equal to itself");
    }
    
    /**
     * Tests that the toString() method returns a non-null string and is overridden to include key field values.
     */
    @Test
    void testToStringMethod() {
        LocalDateTime now = LocalDateTime.now();
        User2 user = new User2(1L, "kc-toString", "Alice", "Smith", "Alice Smith", "alice", "alice@example.com", now);
        String str = user.toString();
        assertNotNull(str, "toString() should not return null");
        // The default Object.toString() produces a string like "com.fmc.starterApp.models.entity.User2@<hashcode>".
        String defaultPrefix = User2.class.getName() + "@";
        assertFalse(str.startsWith(defaultPrefix), "toString() should be overridden to include key field values");
    }


    // =========================================================================
    // ✅ 5. Edge Case Tests
    // =========================================================================

    /**
     * Tests that setting fields to empty strings works as expected.
     */
    @Test
    void testEmptyStringValues() {
        User2 user = new User2();
        user.setKeycloakId("");
        user.setFirstName("");
        user.setLastName("");
        user.setFullName("");
        user.setUsername("");
        user.setEmail("");

        assertEquals("", user.getKeycloakId());
        assertEquals("", user.getFirstName());
        assertEquals("", user.getLastName());
        assertEquals("", user.getFullName());
        assertEquals("", user.getUsername());
        assertEquals("", user.getEmail());
    }

    /**
     * Tests that string values longer than the column definition are accepted in the entity.
     * (Length checks are enforced at the database level.)
     */
    @Test
    void testLongStringValuesExceedingColumnDefinition() {
        String longString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwx1234567890"; // >50 chars
        User2 user = new User2();
        user.setFirstName(longString);
        user.setLastName(longString);
        user.setFullName(longString);
        user.setUsername(longString);
        user.setEmail(longString + "@example.com");

        assertEquals(longString, user.getFirstName());
        assertEquals(longString, user.getLastName());
        assertEquals(longString, user.getFullName());
        assertEquals(longString, user.getUsername());
    }

    /**
     * Tests that a setter call can override an existing value.
     */
    @Test
    void testSetterOverride() {
        User2 user = new User2();
        user.setFirstName("Initial");
        assertEquals("Initial", user.getFirstName());
        user.setFirstName("Updated");
        assertEquals("Updated", user.getFirstName());
    }

    /**
     * Tests that the userId field accepts negative values when set manually.
     */
    @Test
    void testUserIdNegativeValue() {
        User2 user = new User2();
        user.setUserId(-1L);
        assertEquals(-1L, user.getUserId());
    }

    // --- Additional Edge Case Tests ---

    /**
     * Tests handling of leading and trailing whitespace.
     * Verifies that the entity preserves the whitespace as provided.
     */
    @Test
    void testWhitespaceHandling() {
        User2 user = new User2();
        user.setKeycloakId("  kc-whitespace  ");
        user.setUsername("  username  ");
        user.setEmail("  email@example.com  ");
        assertEquals("  kc-whitespace  ", user.getKeycloakId());
        assertEquals("  username  ", user.getUsername());
        assertEquals("  email@example.com  ", user.getEmail());
    }

    /**
     * Tests case sensitivity by setting values that differ only by letter case.
     */
    @Test
    void testCaseSensitivity() {
        User2 user1 = new User2();
        user1.setUsername("TestUser");
        User2 user2 = new User2();
        user2.setUsername("testuser");
        // Verify that the values are exactly what were set.
        assertEquals("TestUser", user1.getUsername());
        assertEquals("testuser", user2.getUsername());
        // Since no normalization occurs, the values should be considered different.
        assertNotEquals(user1.getUsername(), user2.getUsername(), "Usernames differing only in case should be distinct");
    }

    /**
     * Tests that the userId field accepts upper boundary values.
     */
    @Test
    void testUserIdUpperBoundary() {
        User2 user = new User2();
        user.setUserId(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, user.getUserId());
    }

    /**
     * Tests combining null optional fields.
     * Sets multiple optional fields to null simultaneously to ensure proper handling.
     */
    @Test
    void testMultipleOptionalFieldsNullCombination() {
        User2 user = new User2();
        user.setKeycloakId("kc-null-combo");
        user.setUsername("nullCombo");
        user.setEmail("nullcombo@example.com");
        user.setFirstName(null);
        user.setLastName(null);
        user.setFullName(null);

        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getFullName());
    }
}
