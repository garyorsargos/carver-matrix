package com.fmc.starterApp.models.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link CarverMatrix} entity.
 *
 * <p>This test class verifies the behavior of the CarverMatrix model, including:
 * <ul>
 *   <li>Basic instantiation using the no-args and all-args constructors and verifying getter/setter functionality.</li>
 *   <li>Constraint validation for mandatory fields (e.g., {@code user} and {@code name}) and length constraints on string fields.</li>
 *   <li>Default value tests (e.g., verifying that {@code createdAt} is automatically populated).</li>
 *   <li>Equality and {@code toString()} behavior.</li>
 *   <li>Edge case tests for empty strings, special characters/unicode, boundary values for {@code name} and {@code description}, and collection handling.</li>
 *   <li>Testing bidirectional relationship methods: {@code addItem(CarverItem)} and {@code removeItem(CarverItem)}.</li>
 * </ul>
 * </p>
 */
public class CarverMatrixUnitTest {

    // =========================================================================
    // ✅ 1. Basic Instantiation and Field Tests
    // =========================================================================

    /**
     * Tests that the no-args constructor and subsequent setter calls properly assign values.
     */
    @Test
    void testNoArgsConstructorAndSetters() {
        CarverMatrix matrix = new CarverMatrix();
        matrix.setMatrixId(1L);

        // Create a dummy User2.
        User2 user = new User2();
        user.setUserId(10L);
        user.setUsername("TestUser");
        user.setEmail("testuser@example.com");
        matrix.setUser(user);

        matrix.setName("Matrix Name");
        matrix.setDescription("This is a test matrix description.");
        // Set hosts and participants as arrays.
        matrix.setHosts(new String[] { "host@example.com" });
        matrix.setParticipants(new String[] { "participant@example.com" });

        // Verify getters.
        assertEquals(1L, matrix.getMatrixId());
        assertEquals(user, matrix.getUser());
        assertEquals("Matrix Name", matrix.getName());
        assertEquals("This is a test matrix description.", matrix.getDescription());
        assertArrayEquals(new String[] { "host@example.com" }, matrix.getHosts());
        assertArrayEquals(new String[] { "participant@example.com" }, matrix.getParticipants());
    }

    /**
     * Tests that the all-args constructor initializes all fields as expected.
     */
    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        String[] hosts = { "host1@example.com", "host2@example.com" };
        String[] participants = { "participant1@example.com" };
        List<CarverItem> items = new ArrayList<>();  // Start with an empty list.
        
        // Create a dummy User2.
        User2 user = new User2();
        user.setUserId(20L);
        user.setUsername("UserForMatrix");
        user.setEmail("userformatrix@example.com");

        CarverMatrix matrix = new CarverMatrix(2L, user, "Matrix AllArgs", "Detailed description for matrix.", now, hosts, participants, items,
                1.0, 1.2, 0.8, 0.5, 1.1, 0.9, true, false, true);

        assertEquals(2L, matrix.getMatrixId());
        assertEquals(user, matrix.getUser());
        assertEquals("Matrix AllArgs", matrix.getName());
        assertEquals("Detailed description for matrix.", matrix.getDescription());
        assertEquals(now, matrix.getCreatedAt());
        assertArrayEquals(hosts, matrix.getHosts());
        assertArrayEquals(participants, matrix.getParticipants());
        assertEquals(items, matrix.getItems());
        assertEquals(1.0, matrix.getCMulti());
        assertEquals(1.2, matrix.getAMulti());
        assertEquals(0.8, matrix.getRMulti());
        assertEquals(0.5, matrix.getVMulti());
        assertEquals(1.1, matrix.getEMulti());
        assertEquals(0.9, matrix.getR2Multi());
        assertTrue(matrix.getRandomAssignment());
        assertFalse(matrix.getRoleBased());
        assertTrue(matrix.getFivePointScoring());
    }

    // =========================================================================
    // ✅ 2. Constraint Validation Tests
    // =========================================================================

    /**
     * Tests that setting mandatory fields to null via setters throws a NullPointerException.
     */
    @Test
    void testSetterNonNullEnforcement() {
        CarverMatrix matrix = new CarverMatrix();

        // Only the 'name' field is marked @NonNull, so only setting name to null should throw.
        NullPointerException ex = assertThrows(NullPointerException.class, () -> matrix.setName(null));
        assertTrue(ex.getMessage().contains("name"));
    }

    /**
     * Tests that the all-args constructor enforces non-null constraints.
     */
    @Test
    void testAllArgsConstructorNonNullEnforcement() {
        LocalDateTime now = LocalDateTime.now();
        String[] hosts = { "host@example.com" };
        String[] participants = { "participant@example.com" };
        List<CarverItem> items = new ArrayList<>();

        // We no longer enforce a non-null check on user, so passing null for user should work.
        CarverMatrix matrixWithNullUser = new CarverMatrix(
                1L, 
                null,   // user is allowed to be null
                "Valid Name", 
                "Valid Description", 
                now, 
                hosts, 
                participants, 
                items,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 
                false, false, false
        );
        // Assert that matrix is created with a null user.
        assertNull(matrixWithNullUser.getUser(), "User should be allowed to be null");

        // But passing null for name should throw a NullPointerException.
        User2 dummyUser = new User2();
        dummyUser.setUserId(30L);
        dummyUser.setUsername("Dummy");
        dummyUser.setEmail("dummy@example.com");
        NullPointerException ex = assertThrows(NullPointerException.class, () ->
            new CarverMatrix(1L, dummyUser, null, "Valid Description", now, hosts, participants, items,
                    1.0, 1.0, 1.0, 1.0, 1.0, 1.0, false, false, false)
        );
        assertTrue(ex.getMessage().contains("name"));
    }


    /**
     * Tests that string fields respect the maximum length constraints.
     * For name (100 characters) and description (1000 characters).
     */
    @Test
    void testLengthConstraint() {
        String hundredChars = "A".repeat(100);
        String thousandChars = "B".repeat(1000);

        CarverMatrix matrix = new CarverMatrix();
        // Set mandatory fields.
        User2 dummyUser = new User2();
        dummyUser.setUserId(40L);
        dummyUser.setUsername("DummyUser");
        dummyUser.setEmail("dummyuser@example.com");
        matrix.setUser(dummyUser);

        matrix.setName(hundredChars);
        matrix.setDescription(thousandChars);

        assertEquals(100, matrix.getName().length());
        assertEquals(1000, matrix.getDescription().length());
    }

    // =========================================================================
    // ✅ 3. Default Values and Auditing Tests
    // =========================================================================

    /**
     * Tests that the {@code createdAt} field is automatically populated upon entity instantiation.
     */
    @Test
    void testDefaultCreatedAt() {
        CarverMatrix matrix = new CarverMatrix();
        assertNotNull(matrix.getCreatedAt());

        LocalDateTime oneSecondAgo = LocalDateTime.now().minusSeconds(1);
        LocalDateTime oneSecondLater = LocalDateTime.now().plusSeconds(1);
        assertTrue(matrix.getCreatedAt().isAfter(oneSecondAgo) && matrix.getCreatedAt().isBefore(oneSecondLater),
                "createdAt should be set to the current time");
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
        LocalDateTime now = LocalDateTime.now();
        String[] hosts = { "host@example.com" };
        String[] participants = { "participant@example.com" };
        List<CarverItem> items = new ArrayList<>();
        User2 user = new User2();
        user.setUserId(50L);
        user.setUsername("TestUser");
        user.setEmail("testuser@example.com");

        CarverMatrix matrix1 = new CarverMatrix(1L, user, "Matrix", "Description", now, hosts, participants, items,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, true, false, true);
        CarverMatrix matrix2 = new CarverMatrix(1L, user, "Matrix", "Description", now, hosts, participants, items,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, true, false, true);

        // Expect that matrix1 and matrix2 are not equal because they are distinct objects.
        assertNotEquals(matrix1, matrix2, "Different instances with identical fields should not be equal");
        // Optionally, verify that their hash codes differ as well.
        assertNotEquals(matrix1.hashCode(), matrix2.hashCode(), "Hash codes should differ for distinct objects");
    }

    /**
     * Tests that the toString() method returns a non-null string and includes key field values.
     */
    @Test
    void testToStringMethod() {
        LocalDateTime now = LocalDateTime.now();
        String[] hosts = { "host@example.com" };
        String[] participants = { "participant@example.com" };
        List<CarverItem> items = new ArrayList<>();
        User2 user = new User2();
        user.setUserId(60L);
        user.setUsername("ToStringUser");
        user.setEmail("tostringuser@example.com");

        CarverMatrix matrix = new CarverMatrix(2L, user, "ToStringMatrix", "Test Description", now, hosts, participants, items,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, false, true, false);
        String str = matrix.toString();
        assertNotNull(str, "toString() should not return null");
        // Check that key fields appear in the output.
        assertTrue(str.contains("ToStringMatrix"), "toString() should include the matrix name");
        assertTrue(str.contains("Test Description"), "toString() should include the description");
    }

    // =========================================================================
    // ✅ 5. Edge Case Tests
    // =========================================================================

    /**
     * Tests that setting fields to empty strings works as expected.
     */
    @Test
    void testEmptyStringValues() {
        CarverMatrix matrix = new CarverMatrix();
        // Set empty strings for mandatory fields.
        User2 dummyUser = new User2();
        dummyUser.setUserId(70L);
        dummyUser.setUsername("Dummy");
        dummyUser.setEmail("dummy@example.com");
        matrix.setUser(dummyUser);
        matrix.setName("");
        matrix.setDescription("");

        assertEquals("", matrix.getName());
        assertEquals("", matrix.getDescription());
    }

    /**
     * Tests that special characters and Unicode values are handled correctly in string fields.
     */
    @Test
    void testSpecialCharactersAndUnicode() {
        CarverMatrix matrix = new CarverMatrix();
        User2 dummyUser = new User2();
        dummyUser.setUserId(80L);
        dummyUser.setUsername("DummyUser");
        dummyUser.setEmail("dummy@example.com");
        matrix.setUser(dummyUser);
        String specialName = "测试 - テスト - اختبار";
        String specialDescription = "Описание: ☃️ ❄️ 👍";
        matrix.setName(specialName);
        matrix.setDescription(specialDescription);

        assertEquals(specialName, matrix.getName(), "Matrix name should handle special and Unicode characters");
        assertEquals(specialDescription, matrix.getDescription(), "Matrix description should handle special and Unicode characters");
    }

    /**
     * Tests that boundary values for string lengths are accepted.
     * Specifically, tests that the name is exactly 100 characters and description exactly 1000 characters.
     */
    @Test
    void testBoundaryValueForLength() {
        String hundredChars = "A".repeat(100);
        String thousandChars = "B".repeat(1000);

        CarverMatrix matrix = new CarverMatrix();
        User2 dummyUser = new User2();
        dummyUser.setUserId(90L);
        dummyUser.setUsername("BoundaryUser");
        dummyUser.setEmail("boundary@example.com");
        matrix.setUser(dummyUser);
        matrix.setName(hundredChars);
        matrix.setDescription(thousandChars);

        assertEquals(100, matrix.getName().length(), "Matrix name should be 100 characters long");
        assertEquals(1000, matrix.getDescription().length(), "Matrix description should be 1000 characters long");
    }

    /**
     * Tests that the addItem and removeItem methods maintain the bidirectional relationship between
     * CarverMatrix and CarverItem.
     */
    @Test
    void testAddAndRemoveItem() {
        CarverMatrix matrix = new CarverMatrix();
        User2 dummyUser = new User2();
        dummyUser.setUserId(100L);
        dummyUser.setUsername("ItemTestUser");
        dummyUser.setEmail("itemtest@example.com");
        matrix.setUser(dummyUser);
        matrix.setName("Matrix for Items");
        matrix.setDescription("Testing add and remove item functionality.");

        CarverItem item = new CarverItem();
        item.setItemName("Test Item");

        // Initially, items should be empty.
        assertTrue(matrix.getItems().isEmpty(), "Matrix items should initially be empty");

        // Add an item and verify the bidirectional relationship.
        matrix.addItem(item);
        assertTrue(matrix.getItems().contains(item), "Matrix should contain the added item");
        assertEquals(matrix, item.getCarverMatrix(), "Item's matrix should be set to the current matrix");

        // Remove the item and verify that the relationship is removed.
        matrix.removeItem(item);
        assertFalse(matrix.getItems().contains(item), "Matrix should not contain the removed item");
        assertNull(item.getCarverMatrix(), "Item's matrix should be null after removal");
    }
}
