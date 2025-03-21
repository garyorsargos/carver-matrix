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
 *   <li>Edge case handling for empty strings and string lengths.</li>
 *   <li>Default equals and hashCode behavior (object identity, since custom implementations are not provided).</li>
 *   <li>Email validation using Jakarta Bean Validation annotations.</li>
 * </ul>
 * 
 * <p>Note: The no-arguments constructor does not enforce non-null constraints; fields marked with
 * {@code @NonNull} will remain {@code null} until explicitly set via setters.</p>
 *
 * @author Ricky Chen
 * @version 1.0
 */
public class User2UnitTest {

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

    /**
     * Tests that the {@code createdAt} field is automatically initialized to the current timestamp.
     */
    @Test
    void testDefaultCreatedAt() {
        User2 user = new User2();
        // Check that createdAt is not null by default.
        assertNotNull(user.getCreatedAt());

        // Validate that the createdAt time is within one second of the current time.
        LocalDateTime oneSecondAgo = LocalDateTime.now().minusSeconds(1);
        LocalDateTime oneSecondLater = LocalDateTime.now().plusSeconds(1);
        assertTrue(user.getCreatedAt().isAfter(oneSecondAgo) && user.getCreatedAt().isBefore(oneSecondLater),
            "createdAt should be set to the current time");
    }

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
        assertTrue(ex1.getMessage().contains("keycloakId"), "Exception message should mention keycloakId");

        // Expect a NullPointerException when setting username to null.
        NullPointerException ex2 = assertThrows(NullPointerException.class, () -> {
            user.setUsername(null);
        });
        assertTrue(ex2.getMessage().contains("username"), "Exception message should mention username");

        // Expect a NullPointerException when setting email to null.
        NullPointerException ex3 = assertThrows(NullPointerException.class, () -> {
            user.setEmail(null);
        });
        assertTrue(ex3.getMessage().contains("email"), "Exception message should mention email");
    }

    /**
     * Tests Lombok's {@code @NonNull} enforcement in the all-arguments constructor.
     * Passing null for any non-null field should trigger a NullPointerException.
     */
    @Test
    void testAllArgsConstructorNonNullEnforcement() {
        LocalDateTime now = LocalDateTime.now();

        // Passing null for keycloakId in the all-args constructor.
        NullPointerException ex1 = assertThrows(NullPointerException.class, () -> {
            new User2(1L, null, "John", "Doe", "John Doe", "johndoe", "john@example.com", now);
        });
        assertTrue(ex1.getMessage().contains("keycloakId"));

        // Passing null for username in the all-args constructor.
        NullPointerException ex2 = assertThrows(NullPointerException.class, () -> {
            new User2(1L, "kc-1", "John", "Doe", "John Doe", null, "john@example.com", now);
        });
        assertTrue(ex2.getMessage().contains("username"));

        // Passing null for email in the all-args constructor.
        NullPointerException ex3 = assertThrows(NullPointerException.class, () -> {
            new User2(1L, "kc-1", "John", "Doe", "John Doe", "johndoe", null, now);
        });
        assertTrue(ex3.getMessage().contains("email"));
    }

    /**
     * Tests that setting fields to empty strings works as expected.
     */
    @Test
    void testEmptyStringValues() {
        User2 user = new User2();
        // Setting all string fields to empty strings.
        user.setKeycloakId("");
        user.setFirstName("");
        user.setLastName("");
        user.setFullName("");
        user.setUsername("");
        user.setEmail("");

        // Verify that the fields are indeed empty strings.
        assertEquals("", user.getKeycloakId(), "KeycloakId should be an empty string");
        assertEquals("", user.getFirstName(), "FirstName should be an empty string");
        assertEquals("", user.getLastName(), "LastName should be an empty string");
        assertEquals("", user.getFullName(), "FullName should be an empty string");
        assertEquals("", user.getUsername(), "Username should be an empty string");
        assertEquals("", user.getEmail(), "Email should be an empty string");
    }

    /**
     * Tests that fields constrained by maximum length behave as expected.
     */
    @Test
    void testMaxLengthStringValues() {
        // Create a string of exactly 50 characters.
        String fiftyChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwx"; // 50 characters
        User2 user = new User2();
        user.setFirstName(fiftyChars);
        user.setLastName(fiftyChars);
        user.setFullName(fiftyChars);
        user.setUsername(fiftyChars);
        // For email, combining the 50-char string with a domain.
        user.setEmail(fiftyChars + "@test.com");

        // Verify that the string lengths meet expectations.
        assertEquals(50, user.getFirstName().length(), "FirstName should be 50 characters long");
        assertEquals(50, user.getLastName().length(), "LastName should be 50 characters long");
        assertEquals(50, user.getFullName().length(), "FullName should be 50 characters long");
        assertEquals(50, user.getUsername().length(), "Username should be 50 characters long");
    }

    /**
     * Tests that string values longer than the column definition are accepted in the entity,
     * as the enforcement of length is handled by the database.
     */
    @Test
    void testLongStringValuesExceedingColumnDefinition() {
        // Create a string longer than 50 characters.
        String longString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwx1234567890"; // >50 chars
        User2 user = new User2();
        user.setFirstName(longString);
        user.setLastName(longString);
        user.setFullName(longString);
        user.setUsername(longString);
        user.setEmail(longString + "@example.com");

        // The entity's setters accept long strings; length checks occur at the database level.
        assertEquals(longString, user.getFirstName(), "FirstName should accept strings longer than 50 characters");
        assertEquals(longString, user.getLastName(), "LastName should accept strings longer than 50 characters");
        assertEquals(longString, user.getFullName(), "FullName should accept strings longer than 50 characters");
        assertEquals(longString, user.getUsername(), "Username should accept strings longer than 50 characters");
    }

    /**
     * Tests that a setter call can override an existing value.
     */
    @Test
    void testSetterOverride() {
        User2 user = new User2();
        user.setFirstName("Initial");
        // Verify initial value.
        assertEquals("Initial", user.getFirstName(), "Initial value should be set");

        // Override with a new value.
        user.setFirstName("Updated");
        assertEquals("Updated", user.getFirstName(), "Setter should override previous value");
    }

    /**
     * Tests the default equals and hashCode behavior.
     * Since no custom implementations are provided, two instances with identical field values are not equal.
     */
    @Test
    void testEqualsAndHashCodeBehavior() {
        LocalDateTime now = LocalDateTime.now();
        User2 user1 = new User2(1L, "kc-1", "John", "Doe", "John Doe", "johndoe", "john@example.com", now);
        User2 user2 = new User2(1L, "kc-1", "John", "Doe", "John Doe", "johndoe", "john@example.com", now);

        // Different instances with the same field values should not be equal.
        assertNotEquals(user1, user2, "Different instances should not be equal without custom equals implementation");
        // The same instance should be equal to itself.
        assertEquals(user1, user1, "The same instance should be equal to itself");
    }

    /**
     * Tests that the {@code createdAt} field can be explicitly set to {@code null}.
     */
    @Test
    void testSetCreatedAtToNull() {
        User2 user = new User2();
        // Verify that createdAt is automatically initialized.
        assertNotNull(user.getCreatedAt(), "createdAt should be initialized by default");

        // Setting createdAt to null should update the value.
        user.setCreatedAt(null);
        assertNull(user.getCreatedAt(), "createdAt should be null after being explicitly set to null");
    }

    /**
     * Tests that the userId field accepts negative values when set manually.
     */
    @Test
    void testUserIdNegativeValue() {
        User2 user = new User2();
        user.setUserId(-1L);
        assertEquals(-1L, user.getUserId(), "UserId should accept negative values if set manually");
    }

    /**
     * Tests that a valid email passes the validation constraints.
     */
    @Test
    void testValidEmail() {
        // Create a new validator instance for this test.
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        User2 user = new User2();
        user.setKeycloakId("validKeycloakId");
        user.setUsername("validUsername");
        user.setEmail("valid@example.com");

        // Validate the user and expect no constraint violations for a valid email.
        Set<ConstraintViolation<User2>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Expected no constraint violations for a valid email");
    }

    /**
     * Tests that an invalid email format triggers validation constraint violations.
     */
    @Test
    void testInvalidEmail() {
        // Create a new validator instance for this test.
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        User2 user = new User2();
        user.setKeycloakId("validKeycloakId");
        user.setUsername("validUsername");
        // Set an invalid email format.
        user.setEmail("invalid-email");

        // Validate the user and expect constraint violations for the email field.
        Set<ConstraintViolation<User2>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Expected constraint violations for an invalid email");

        // Confirm that one of the violations specifically targets the email field.
        boolean emailViolationFound = violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("email"));
        assertTrue(emailViolationFound, "Expected a violation on the email field");
    }
}
