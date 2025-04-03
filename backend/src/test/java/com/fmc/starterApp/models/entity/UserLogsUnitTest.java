package com.fmc.starterApp.models.entity;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link UserLogs} entity.
 *
 * <p>This test class verifies the behavior of the UserLogs model, including:
 * <ul>
 *   <li>Basic instantiation using both the no-args and all-args constructors, and correct assignment of fields.</li>
 *   <li>Enforcement of non-null constraints on mandatory fields (appUser and loginTime) via setters and the all-args constructor.</li>
 *   <li>Default behavior of fields (e.g. the primary key remains null if the entity is not persisted).</li>
 *   <li>The {@code toString()} method returns a meaningful representation of the entity.</li>
 *   <li>Edge case testing for required fields (e.g., ensuring that null values trigger exceptions).</li>
 * </ul>
 * </p>
 */
public class UserLogsUnitTest {

    // =========================================================================
    // ✅ 1. Basic Instantiation and Field Tests
    // =========================================================================

    /**
     * Tests that the no-args constructor and subsequent setter calls properly assign values.
     */
    @Test
    void testNoArgsConstructorAndSetters() {
        UserLogs logs = new UserLogs();
        // Create a dummy AppUser for testing.
        AppUser user = new AppUser();
        user.setUserId(1L);
        user.setUserName("TestUser");
        user.setEmail("test@example.com");

        // Set mandatory fields.
        logs.setAppUser(user);
        Date loginTime = new Date();
        logs.setLoginTime(loginTime);

        // Verify that getters return the correct values.
        assertEquals(user, logs.getAppUser());
        assertEquals(loginTime, logs.getLoginTime());
    }

    /**
     * Tests that the all-args constructor initializes all fields as expected.
     */
    @Test
    void testAllArgsConstructor() {
        Date loginTime = new Date();
        AppUser user = new AppUser();
        user.setUserId(2L);
        user.setUserName("User2");
        user.setEmail("user2@example.com");

        UserLogs logs = new UserLogs(user, loginTime, 100L);
        assertEquals(user, logs.getAppUser());
        assertEquals(loginTime, logs.getLoginTime());
        assertEquals(100L, logs.getId());
    }

    // =========================================================================
    // ✅ 2. Constraint Validation Tests
    // =========================================================================

    /**
     * Tests that setting mandatory fields to null via setters throws a NullPointerException.
     */
    @Test
    void testSetterNonNullEnforcement() {
        UserLogs logs = new UserLogs();

        // Setting appUser to null should throw an exception.
        NullPointerException ex1 = assertThrows(NullPointerException.class, () -> logs.setAppUser(null));
        assertTrue(ex1.getMessage().contains("appUser"));

        // Setting loginTime to null should throw an exception.
        NullPointerException ex2 = assertThrows(NullPointerException.class, () -> logs.setLoginTime(null));
        assertTrue(ex2.getMessage().contains("loginTime"));
    }

    /**
     * Tests that the all-args constructor enforces non-null constraints.
     */
    @Test
    void testAllArgsConstructorNonNullEnforcement() {
        Date loginTime = new Date();
        // Passing null for appUser.
        NullPointerException ex1 = assertThrows(NullPointerException.class, () -> new UserLogs(null, loginTime, 10L));
        assertTrue(ex1.getMessage().contains("appUser"));

        // Passing null for loginTime.
        AppUser user = new AppUser();
        user.setUserId(3L);
        user.setUserName("User3");
        user.setEmail("user3@example.com");
        NullPointerException ex2 = assertThrows(NullPointerException.class, () -> new UserLogs(user, null, 10L));
        assertTrue(ex2.getMessage().contains("loginTime"));
    }

    // =========================================================================
    // ✅ 3. Default Values and Auditing Tests
    // =========================================================================

    /**
     * Tests that the primary key (id) is null if the entity is not persisted.
     */
    @Test
    void testDefaultValues() {
        UserLogs logs = new UserLogs();
        // Since id is auto-generated, if the entity is not persisted, it should remain null.
        assertNull(logs.getId());
    }

    // =========================================================================
    // ✅ 4. Equality and toString Tests
    // =========================================================================

    /**
     * Tests that the toString() method returns a non-null string and is overridden to include key field values.
     */
    @Test
    void testToStringMethod() {
        Date loginTime = new Date();
        AppUser user = new AppUser();
        user.setUserId(4L);
        user.setUserName("User4");
        user.setEmail("user4@example.com");

        UserLogs logs = new UserLogs(user, loginTime, 50L);
        String str = logs.toString();
        assertNotNull(str);
        String defaultPrefix = UserLogs.class.getName() + "@";
        assertFalse(str.startsWith(defaultPrefix), "toString() should be overridden to include key field values");
    }

    // =========================================================================
    // ✅ 5. Edge Case Tests
    // =========================================================================

    /**
     * Tests that mandatory fields are properly set when provided.
     */
    @Test
    void testMandatoryFieldsSet() {
        AppUser user = new AppUser();
        user.setUserId(5L);
        user.setUserName("User5");
        user.setEmail("user5@example.com");
        Date loginTime = new Date();

        UserLogs logs = new UserLogs(user, loginTime, 200L);
        assertNotNull(logs.getAppUser());
        assertNotNull(logs.getLoginTime());
    }
}
