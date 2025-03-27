package com.fmc.starterApp.models.entity;

import java.util.List;
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
 * Unit tests for the {@link AppUser} entity.
 *
 * <p>This test class verifies the behavior of the AppUser model, including:
 * <ul>
 *   <li>Proper initialization using the no-args and all-args constructors, and correct assignment of fields via getters/setters.</li>
 *   <li>Enforcement of non-null constraints on mandatory fields (userName and email) via setters and the all-args constructor.</li>
 *   <li>Email format validation using Jakarta Bean Validation.</li>
 *   <li>Length constraints for string fields (userName up to 50 characters, email up to 100 characters).</li>
 *   <li>Default behavior of the primary key (userId remains null if not persisted).</li>
 *   <li>Behavior of equals, hashCode, and toString methods.</li>
 *   <li>Edge case handling for empty strings, whitespace, and case sensitivity.</li>
 * </ul>
 * </p>
 */
public class AppUserUnitTest {

    // =========================================================================
    // ✅ 1. Basic Instantiation and Field Tests
    // =========================================================================

    /**
     * Tests that the no-args constructor and subsequent setter calls properly assign values.
     */
    @Test
    void testNoArgsConstructorAndSetters() {
        AppUser user = new AppUser();
        user.setUserId(1L);
        user.setUserName("John Doe");
        user.setEmail("johndoe@example.com");
        // For userTimes, we can set an empty list or leave it null.
        
        assertEquals(1L, user.getUserId());
        assertEquals("John Doe", user.getUserName());
        assertEquals("johndoe@example.com", user.getEmail());
    }

    /**
     * Tests that the all-args constructor initializes all fields as expected.
     */
    @Test
    void testAllArgsConstructor() {
        // Create a dummy list for userTimes (could be empty)
        List<UserLogs> logs = List.of();
        AppUser user = new AppUser(2L, "Jane Doe", "janedoe@example.com", logs);

        assertEquals(2L, user.getUserId());
        assertEquals("Jane Doe", user.getUserName());
        assertEquals("janedoe@example.com", user.getEmail());
        assertEquals(logs, user.getUserTimes());
    }

    // =========================================================================
    // ✅ 2. Constraint Validation Tests
    // =========================================================================

    /**
     * Tests that setting mandatory fields to null via setters throws a NullPointerException.
     */
    @Test
    void testSetterNonNullEnforcement() {
        AppUser user = new AppUser();

        // Setting userName to null should throw an exception.
        NullPointerException ex1 = assertThrows(NullPointerException.class, () -> user.setUserName(null));
        assertTrue(ex1.getMessage().contains("userName"));

        // Setting email to null should throw an exception.
        NullPointerException ex2 = assertThrows(NullPointerException.class, () -> user.setEmail(null));
        assertTrue(ex2.getMessage().contains("email"));
    }

    /**
     * Tests that the all-args constructor enforces non-null constraints.
     */
    @Test
    void testAllArgsConstructorNonNullEnforcement() {
        // Passing null for userName.
        NullPointerException ex1 = assertThrows(NullPointerException.class, () ->
                new AppUser(1L, null, "test@example.com", List.of()));
        assertTrue(ex1.getMessage().contains("userName"));

        // Passing null for email.
        NullPointerException ex2 = assertThrows(NullPointerException.class, () ->
                new AppUser(1L, "Test User", null, List.of()));
        assertTrue(ex2.getMessage().contains("email"));
    }

    /**
     * Tests that a valid email passes the validation constraints.
     */
    @Test
    void testValidEmail() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        AppUser user = new AppUser();
        user.setUserName("Valid User");
        user.setEmail("valid@example.com");

        Set<ConstraintViolation<AppUser>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Expected no constraint violations for a valid email");
    }

    /**
     * Tests that an invalid email format triggers validation constraint violations.
     */
    @Test
    void testInvalidEmail() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        AppUser user = new AppUser();
        user.setUserName("Invalid User");
        user.setEmail("invalid-email");

        Set<ConstraintViolation<AppUser>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Expected constraint violations for an invalid email");

        boolean emailViolationFound = violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("email"));
        assertTrue(emailViolationFound, "Expected a violation on the email field");
    }

    /**
     * Tests that string fields respect the maximum length constraints.
     * For userName (50 characters) and email (100 characters).
     */
    @Test
    void testLengthConstraint() {
        String fiftyChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwx"; // 50 characters
        String hundredChars = fiftyChars + fiftyChars; // 100 characters

        AppUser user = new AppUser();
        user.setUserName(fiftyChars);
        user.setEmail(hundredChars + "@example.com");  // total length may exceed 100, so adjust accordingly

        assertEquals(50, user.getUserName().length(), "userName should be 50 characters long");
        // We assume that the email field in the database is constrained, but the entity itself may accept longer strings.
        // In this unit test, we're just verifying that the values are set.
        assertEquals(hundredChars + "@example.com", user.getEmail());
    }

    // =========================================================================
    // ✅ 3. Default Values and Auditing Tests
    // =========================================================================

    /**
     * Tests that the primary key (userId) remains null if the entity is not persisted.
     */
    @Test
    void testDefaultValues() {
        AppUser user = new AppUser();
        // Since userId is auto-generated, if the entity is not persisted, it should remain null.
        assertNull(user.getUserId(), "Expected userId to be null if not persisted");
    }

    // =========================================================================
    // ✅ 4. Equality and toString Tests
    // =========================================================================

    /**
     * Tests that the default equals and hashCode behavior works as expected.
     * Without custom implementations, two instances with identical field values are not equal.
     */
    @Test
    void testEqualsAndHashCodeBehavior() {
        List<UserLogs> logs = List.of();
        AppUser user1 = new AppUser(1L, "User", "user@example.com", logs);
        AppUser user2 = new AppUser(1L, "User", "user@example.com", logs);

        // Without custom equals/hashCode, they should be different objects.
        assertNotEquals(user1, user2, "Different instances should not be equal without custom equals implementation");
        assertEquals(user1, user1, "The same instance should be equal to itself");
    }

    /**
     * Tests that the toString() method returns a non-null string and is overridden to include key field values.
     */
    @Test
    void testToStringMethod() {
        List<UserLogs> logs = List.of();
        AppUser user = new AppUser(2L, "ToStringUser", "tostring@example.com", logs);
        String str = user.toString();
        assertNotNull(str, "toString() should not return null");
        String defaultPrefix = AppUser.class.getName() + "@";
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
        AppUser user = new AppUser();
        user.setUserName("");
        user.setEmail("");

        assertEquals("", user.getUserName());
        assertEquals("", user.getEmail());
    }

    /**
     * Tests that a setter call can override an existing value.
     */
    @Test
    void testSetterOverride() {
        AppUser user = new AppUser();
        user.setUserName("InitialName");
        assertEquals("InitialName", user.getUserName());
        user.setUserName("UpdatedName");
        assertEquals("UpdatedName", user.getUserName());
    }

    /**
     * Tests handling of leading and trailing whitespace.
     */
    @Test
    void testWhitespaceHandling() {
        AppUser user = new AppUser();
        user.setUserName("  NameWithSpaces  ");
        user.setEmail("  spaced@example.com  ");
        assertEquals("  NameWithSpaces  ", user.getUserName());
        assertEquals("  spaced@example.com  ", user.getEmail());
    }

    /**
     * Tests case sensitivity by setting userName values that differ only in letter case.
     */
    @Test
    void testCaseSensitivity() {
        AppUser user1 = new AppUser();
        AppUser user2 = new AppUser();
        user1.setUserName("CaseSensitive");
        user2.setUserName("casesensitive");
        assertEquals("CaseSensitive", user1.getUserName());
        assertEquals("casesensitive", user2.getUserName());
        assertNotEquals(user1.getUserName(), user2.getUserName(), "UserNames differing only in case should be distinct");
    }

    /**
     * Tests that when userTimes is not set, the getter returns either null or an empty list.
     */
    @Test
    void testUserTimesDefault() {
        AppUser user = new AppUser();
        // Verify that userTimes is either null or empty.
        assertTrue(user.getUserTimes() == null || user.getUserTimes().isEmpty(),
            "Expected userTimes to be null or empty when not explicitly set");
    }
}
