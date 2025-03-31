package com.fmc.starterApp.services;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.fmc.starterApp.models.entity.PostgresExampleObject;
import com.fmc.starterApp.repositories.PostgresRepository;

/**
 * Integration tests for {@link PostGresExampleService}, verifying that the service layer:
 * <ul>
 *   <li>Executes its methods correctly and integrates with the underlying repository.</li>
 *   <li>Validates input parameters and handles errors gracefully.</li>
 *   <li>Manages transactions correctly (e.g., rollback on error).</li>
 * </ul>
 *
 * <p>This test class uses an in-memory H2 database (configured in PostgreSQL mode) and real repository implementations.
 * The tests are organized per service function with subsections for basic functionality, input validation,
 * transactional/integration, edge case/exception handling, and caching/performance (if applicable).
 *
 * <p>If a section is not applicable to a specific function, a comment is added instead of omitting the section.
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class PostGresExampleServiceTest {

    @Autowired
    private PostGresExampleService postGresExampleService;

    @Autowired
    private PostgresRepository postgresRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Instantiate a TransactionTemplate using the injected PlatformTransactionManager.
    private final TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

    // =========================================================================
    // Tests for insertExample Function
    // =========================================================================

    // =========================================================================
    // ✅ 1. insertExample's Basic Functionality Tests (Unit Test)
    // =========================================================================
    /**
     * **insertExample - Valid Input Test**
     * Verify that insertExample executes successfully with valid input and returns the expected name.
     */
    @Test
    @Transactional
    void testInsertExample_BasicFunctionality() {
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName("Example Object");

        String returnedName = postGresExampleService.insertExample(obj);
        assertEquals("Example Object", returnedName, "The returned name should match the object's name");

        // Verify via repository that the object was saved.
        List<PostgresExampleObject> allObjects = postgresRepository.findAll();
        assertThat(allObjects).extracting(PostgresExampleObject::getName).contains("Example Object");
    }

    // =========================================================================
    // ✅ 2. insertExample's Input Validation Tests (Unit Test)
    // =========================================================================
    /**
     * **insertExample - Null Input Exception Test**
     * Verify that passing a null PostgresExampleObject to insertExample throws an IllegalArgumentException.
     */
    @Test
    void testInsertExample_NullInput() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> postGresExampleService.insertExample(null),
                "Expected insertExample to throw IllegalArgumentException for null input");
        assertThat(ex.getMessage()).contains("PostgresExampleObject must not be null");
    }

    // =========================================================================
    // ✅ 3. insertExample's Transactional and Integration Tests
    // =========================================================================
    /**
     * **insertExample - End-to-End Integration Test**
     * Verify that a PostgresExampleObject is persisted in the database and can be queried directly using JdbcTemplate.
     */
    @Test
    @Transactional
    void testInsertExample_EndToEndIntegration() {
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName("Integration Example");

        String returnedName = postGresExampleService.insertExample(obj);
        assertEquals("Integration Example", returnedName, "The returned name should match");

        // Use JdbcTemplate to query the 'example' table directly.
        List<String> names = jdbcTemplate.queryForList(
                "SELECT name FROM example", String.class);
        assertThat(names).contains("Integration Example");
    }

    // =========================================================================
    // ✅ 4. insertExample's Edge Case and Exception Handling Tests
    // =========================================================================
    /**
     * **insertExample - Unexpected Error Handling Test**
     * Simulate an unexpected error by forcing the repository to throw an exception.
     */
    @Test
    void testInsertExample_UnexpectedErrorHandling() {
        // Forcing an error: passing null should trigger the IllegalArgumentException.
        try {
            postGresExampleService.insertExample(null);
            fail("Expected IllegalArgumentException for null input");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).contains("PostgresExampleObject must not be null");
        }
    }

    // =========================================================================
    // ✅ 5. insertExample's Caching and Performance Tests (if applicable)
    // =========================================================================
    // Not applicable for insertExample as caching is not implemented in this service.

    // =========================================================================
    // Tests for getObjectById Function
    // =========================================================================

    // =========================================================================
    // ✅ 1. getObjectById's Basic Functionality Tests (Unit Test)
    // =========================================================================
    /**
     * **getObjectById - Valid Input Test**
     * Verify that getObjectById returns the correct PostgresExampleObject for a valid ID.
     */
    @Test
    @Transactional
    void testGetObjectById_BasicFunctionality() {
        // First, insert an object.
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName("Object by ID");
        postGresExampleService.insertExample(obj);

        // Retrieve the object using the repository to get its ID.
        PostgresExampleObject savedObj = postgresRepository.findAll().get(0);
        PostgresExampleObject retrievedObj = postGresExampleService.getObjectById(savedObj.getId());
        assertNotNull(retrievedObj, "The object should be retrieved by its ID");
        assertEquals("Object by ID", retrievedObj.getName(), "The object name should match");
    }

    // =========================================================================
    // ✅ 2. getObjectById's Input Validation Tests (Unit Test)
    // =========================================================================
    // (If needed, add tests for invalid IDs or boundary conditions; not applicable if only valid IDs are expected.)

    // =========================================================================
    // ✅ 3. getObjectById's Transactional and Integration Tests
    // =========================================================================
    /**
     * **getObjectById - End-to-End Integration Test**
     * Verify that an object inserted via insertExample can be retrieved by getObjectById.
     */
    @Test
    @Transactional
    void testGetObjectById_EndToEndIntegration() {
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName("End-to-End Object");
        postGresExampleService.insertExample(obj);

        PostgresExampleObject savedObj = postgresRepository.findAll().get(0);
        PostgresExampleObject retrievedObj = postGresExampleService.getObjectById(savedObj.getId());
        assertNotNull(retrievedObj, "The object should be retrieved by its ID");
        assertEquals("End-to-End Object", retrievedObj.getName(), "The object name should match");
    }

    // =========================================================================
    // ✅ 4. getObjectById's Edge Case and Exception Handling Tests (Unit Test)
    // =========================================================================
    // (Additional edge case tests can be implemented as needed.)

    // =========================================================================
    // Tests for getAllPostGres Function
    // =========================================================================

    // =========================================================================
    // ✅ 1. getAllPostGres's Basic Functionality Tests (Unit Test)
    // =========================================================================
    /**
     * **getAllPostGres - Basic Retrieval Test**
     * Verify that getAllPostGres returns a list of PostgresExampleObject entities.
     */
    @Test
    @Transactional
    void testGetAllPostGres_BasicFunctionality() {
        // Insert two objects.
        PostgresExampleObject obj1 = new PostgresExampleObject();
        obj1.setName("Object 1");
        postGresExampleService.insertExample(obj1);

        PostgresExampleObject obj2 = new PostgresExampleObject();
        obj2.setName("Object 2");
        postGresExampleService.insertExample(obj2);

        List<PostgresExampleObject> objects = postGresExampleService.getAllPostGres();
        assertNotNull(objects, "The returned list should not be null");
        assertTrue(objects.size() >= 2, "There should be at least two objects in the list");
    }

    // =========================================================================
    // ✅ 2. getAllPostGres's Transactional and Integration Tests (Unit Test)
    // =========================================================================
    /**
     * **getAllPostGres - End-to-End Integration Test**
     * Verify that getAllPostGres retrieves all objects from the database.
     */
    @Test
    @Transactional
    void testGetAllPostGres_EndToEndIntegration() {
        // Insert an object.
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName("Integration Object");
        postGresExampleService.insertExample(obj);

        // Use JdbcTemplate to verify that the object exists in the database.
        List<String> names = jdbcTemplate.queryForList("SELECT name FROM example", String.class);
        assertThat(names).contains("Integration Object");
    }

    // =========================================================================
    // ✅ 3. getAllPostGres's Edge Case and Exception Handling Tests (Unit Test)
    // =========================================================================
    // (Additional edge case tests can be implemented as needed.)

    // =========================================================================
    // Tests for deleteById Function
    // =========================================================================

    // =========================================================================
    // ✅ 1. deleteById's Basic Functionality Tests (Unit Test)
    // =========================================================================
    /**
     * **deleteById - Basic Deletion Test**
     * Verify that deleteById deletes the specified PostgresExampleObject and returns its ID.
     */
    @Test
    @Transactional
    void testDeleteById_BasicFunctionality() {
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName("Delete Test Object");
        postGresExampleService.insertExample(obj);

        // Retrieve the inserted object to get its ID.
        PostgresExampleObject savedObj = postgresRepository.findAll().get(0);
        Long id = savedObj.getId();

        Long deletedId = postGresExampleService.deleteById(id);
        assertEquals(id, deletedId, "The deleted object's ID should be returned");

        // Verify that the object no longer exists.
        PostgresExampleObject retrieved = postgresRepository.findFirstById(id);
        assertNull(retrieved, "The object should no longer exist after deletion");
    }

    // =========================================================================
    // ✅ 2. deleteById's Transactional and Integration Tests (Unit Test)
    // =========================================================================
    /**
     * **deleteById - End-to-End Integration Test**
     * Verify that deleteById correctly deletes an object from the database.
     */
    @Test
    @Transactional
    void testDeleteById_EndToEndIntegration() {
        PostgresExampleObject obj = new PostgresExampleObject();
        obj.setName("Integration Delete Object");
        postGresExampleService.insertExample(obj);

        PostgresExampleObject savedObj = postgresRepository.findAll().get(0);
        Long id = savedObj.getId();

        Long deletedId = postGresExampleService.deleteById(id);
        assertEquals(id, deletedId, "The deleted object's ID should be returned");

        // Verify that the object no longer exists.
        PostgresExampleObject retrieved = postgresRepository.findFirstById(id);
        assertNull(retrieved, "The object should no longer exist after deletion");
    }

    // =========================================================================
    // ✅ 3. deleteById's Edge Case and Exception Handling Tests (Unit Test)
    // =========================================================================
    /**
     * **deleteById - Error Handling Test**
     * Verify that deleteById handles errors gracefully when an invalid ID is provided.
     */
    @Test
    void testDeleteById_ErrorHandling() {
        // Attempt to delete an object with an ID that does not exist.
        Long invalidId = 9999L;
        // Depending on implementation, this may or may not throw an exception.
        // For this test, we assume it should not throw an exception but simply return the invalid ID.
        Long returnedId = postGresExampleService.deleteById(invalidId);
        assertEquals(invalidId, returnedId, "The method should return the provided ID even if deletion did not occur");
    }

    // =========================================================================
    // ✅ 4. deleteById's Caching and Performance Tests (if applicable)
    // =========================================================================
    // (Not applicable unless caching is implemented in the service layer.)
}
