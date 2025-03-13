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

public class User2UnitTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        User2 user = new User2();
        // Using setters to initialize fields
        user.setUserId(1L);
        user.setKeycloakId("kc-12345");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setFullName("John Doe");
        user.setUsername("johndoe");
        user.setEmail("johndoe@example.com");

        // Verifying that getters return the correct values
        assertEquals(1L, user.getUserId());
        assertEquals("kc-12345", user.getKeycloakId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("John Doe", user.getFullName());
        assertEquals("johndoe", user.getUsername());
        assertEquals("johndoe@example.com", user.getEmail());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        User2 user = new User2(1L, "kc-12345", "John", "Doe", "John Doe", "johndoe", "johndoe@example.com", now);

        // Verifying that the all-args constructor initializes the fields properly
        assertEquals(1L, user.getUserId());
        assertEquals("kc-12345", user.getKeycloakId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("John Doe", user.getFullName());
        assertEquals("johndoe", user.getUsername());
        assertEquals("johndoe@example.com", user.getEmail());
        assertEquals(now, user.getCreatedAt());
    }

    @Test
    void testDefaultCreatedAt() {
        User2 user = new User2();
        // The default value for createdAt should be automatically set via field initialization
        assertNotNull(user.getCreatedAt());

        // Ensure that createdAt is set to a value close to the current time
        LocalDateTime oneSecondAgo = LocalDateTime.now().minusSeconds(1);
        LocalDateTime oneSecondLater = LocalDateTime.now().plusSeconds(1);
        assertTrue(user.getCreatedAt().isAfter(oneSecondAgo) && user.getCreatedAt().isBefore(oneSecondLater),
            "createdAt should be set to the current time");
    }

    @Test
    void testSetterNonNullEnforcement() {
        User2 user = new User2();

        // Using the setter for keycloakId with a null value should throw a NullPointerException.
        NullPointerException ex1 = assertThrows(NullPointerException.class, () -> {
            user.setKeycloakId(null);
        });
        assertTrue(ex1.getMessage().contains("keycloakId"), "Exception message should mention keycloakId");

        // Similarly for username.
        NullPointerException ex2 = assertThrows(NullPointerException.class, () -> {
            user.setUsername(null);
        });
        assertTrue(ex2.getMessage().contains("username"), "Exception message should mention username");

        // And for email.
        NullPointerException ex3 = assertThrows(NullPointerException.class, () -> {
            user.setEmail(null);
        });
        assertTrue(ex3.getMessage().contains("email"), "Exception message should mention email");
    }

    @Test
    void testAllArgsConstructorNonNullEnforcement() {
        LocalDateTime now = LocalDateTime.now();

        // Passing a null for keycloakId in the all-args constructor should trigger a NullPointerException.
        NullPointerException ex1 = assertThrows(NullPointerException.class, () -> {
            new User2(1L, null, "John", "Doe", "John Doe", "johndoe", "john@example.com", now);
        });
        assertTrue(ex1.getMessage().contains("keycloakId"));

        // Passing a null for username should trigger a NullPointerException.
        NullPointerException ex2 = assertThrows(NullPointerException.class, () -> {
            new User2(1L, "kc-1", "John", "Doe", "John Doe", null, "john@example.com", now);
        });
        assertTrue(ex2.getMessage().contains("username"));

        // Passing a null for email should trigger a NullPointerException.
        NullPointerException ex3 = assertThrows(NullPointerException.class, () -> {
            new User2(1L, "kc-1", "John", "Doe", "John Doe", "johndoe", null, now);
        });
        assertTrue(ex3.getMessage().contains("email"));
    }

    @Test
    void testEmptyStringValues() {
        User2 user = new User2();
        user.setKeycloakId("");
        user.setFirstName("");
        user.setLastName("");
        user.setFullName("");
        user.setUsername("");
        user.setEmail("");

        assertEquals("", user.getKeycloakId(), "KeycloakId should be an empty string");
        assertEquals("", user.getFirstName(), "FirstName should be an empty string");
        assertEquals("", user.getLastName(), "LastName should be an empty string");
        assertEquals("", user.getFullName(), "FullName should be an empty string");
        assertEquals("", user.getUsername(), "Username should be an empty string");
        assertEquals("", user.getEmail(), "Email should be an empty string");
    }

    @Test
    void testMaxLengthStringValues() {
        // Create a string of exactly 50 characters.
        String fiftyChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwx"; // 50 characters
        User2 user = new User2();
        user.setFirstName(fiftyChars);
        user.setLastName(fiftyChars);
        user.setFullName(fiftyChars);
        user.setUsername(fiftyChars);
        // For email, although no length limit is enforced in the entity (other than uniqueness and not-null),
        // we combine the 50-char string with a domain for demonstration.
        user.setEmail(fiftyChars + "@test.com");

        assertEquals(50, user.getFirstName().length(), "FirstName should be 50 characters long");
        assertEquals(50, user.getLastName().length(), "LastName should be 50 characters long");
        assertEquals(50, user.getFullName().length(), "FullName should be 50 characters long");
        assertEquals(50, user.getUsername().length(), "Username should be 50 characters long");
    }

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

        // The entity does not enforce the column length on the setter level,
        // so it should hold the entire string.
        assertEquals(longString, user.getFirstName(), "FirstName should accept strings longer than 50 characters");
        assertEquals(longString, user.getLastName(), "LastName should accept strings longer than 50 characters");
        assertEquals(longString, user.getFullName(), "FullName should accept strings longer than 50 characters");
        assertEquals(longString, user.getUsername(), "Username should accept strings longer than 50 characters");
    }

    @Test
    void testSetterOverride() {
        User2 user = new User2();
        user.setFirstName("Initial");
        assertEquals("Initial", user.getFirstName(), "Initial value should be set");

        // Override the previous value.
        user.setFirstName("Updated");
        assertEquals("Updated", user.getFirstName(), "Setter should override previous value");
    }

    @Test
    void testEqualsAndHashCodeBehavior() {
        // Since no custom equals/hashCode are defined, two objects with the same field values
        // should not be equal unless they are the same instance.
        LocalDateTime now = LocalDateTime.now();
        User2 user1 = new User2(1L, "kc-1", "John", "Doe", "John Doe", "johndoe", "john@example.com", now);
        User2 user2 = new User2(1L, "kc-1", "John", "Doe", "John Doe", "johndoe", "john@example.com", now);

        assertNotEquals(user1, user2, "Different instances should not be equal without custom equals implementation");
        assertEquals(user1, user1, "The same instance should be equal to itself");
    }

    @Test
    void testSetCreatedAtToNull() {
        User2 user = new User2();
        // By default, createdAt is set via field initialization.
        assertNotNull(user.getCreatedAt(), "createdAt should be initialized by default");

        // Explicitly setting createdAt to null.
        user.setCreatedAt(null);
        assertNull(user.getCreatedAt(), "createdAt should be null after being explicitly set to null");
    }

    @Test
    void testUserIdNegativeValue() {
        User2 user = new User2();
        user.setUserId(-1L);
        assertEquals(-1L, user.getUserId(), "UserId should accept negative values if set manually");
    }

    @Test
    void testValidEmail() {
        // Create a new validator instance for this test.
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        User2 user = new User2();
        user.setKeycloakId("validKeycloakId");
        user.setUsername("validUsername");
        user.setEmail("valid@example.com");

        Set<ConstraintViolation<User2>> violations = validator.validate(user);
        // Expect no constraint violations when email is valid.
        assertTrue(violations.isEmpty(), "Expected no constraint violations for a valid email");
    }

    @Test
    void testInvalidEmail() {
        // Create a new validator instance for this test.
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        User2 user = new User2();
        user.setKeycloakId("validKeycloakId");
        user.setUsername("validUsername");
        user.setEmail("invalid-email"); // invalid format

        Set<ConstraintViolation<User2>> violations = validator.validate(user);
        // Expect violations for the invalid email.
        assertFalse(violations.isEmpty(), "Expected constraint violations for an invalid email");

        // Verify that at least one violation is related to the email field.
        boolean emailViolationFound = violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("email"));
        assertTrue(emailViolationFound, "Expected a violation on the email field");
    }
}
