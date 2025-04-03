package com.fmc.starterApp.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.fmc.starterApp.models.entity.CarverItem;
import com.fmc.starterApp.models.entity.CarverMatrix;
import com.fmc.starterApp.models.entity.User2;
import com.fmc.starterApp.repositories.CarverItemRepository;
import com.fmc.starterApp.repositories.CarverMatrixRepository;
import com.fmc.starterApp.repositories.User2Repository;

/**
 * Integration tests for {@link CarverMatrixService}, verifying that the service layer:
 * <ul>
 *   <li>Executes its methods correctly and integrates with the underlying repositories.</li>
 *   <li>Validates input parameters and handles errors gracefully.</li>
 *   <li>Manages transactions correctly (e.g., rollback on error).</li>
 * </ul>
 *
 * <p>This test class uses an in-memory H2 database and real repository implementations.
 * The tests are organized per service function with subsections for basic functionality, input validation,
 * transactional/integration, edge case/exception handling, and caching/performance (if applicable).
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class CarverMatrixServiceTest {

    @Autowired
    private CarverMatrixService carverMatrixService;

    @Autowired
    private CarverMatrixRepository carverMatrixRepository;

    @Autowired
    private CarverItemRepository carverItemRepository;

    @Autowired
    private User2Repository user2Repository;

    // -------------------------------------------------------------------------
    // Tests for getMatricesByHost Function
    // -------------------------------------------------------------------------

    // =========================================================================
    // ✅ 1. getMatricesByHost's Basic Functionality Tests (Unit Test)
    // =========================================================================

    /**
     * **getMatricesByHost - Valid Input Test**
     * Verify that getMatricesByHost returns the expected list of matrices for a given user ID.
     */
    @Test
    @Transactional
    void testGetMatricesByHost_BasicFunctionality() {
        // Create and persist a user.
        User2 user = new User2(null, "host-001", "Host", "User", "Host User", "hostuser", "host@example.com", null);
        user = user2Repository.save(user);

        // Create a CarverMatrix with hosts including the user's ID (as string).
        CarverMatrix matrix = new CarverMatrix();
        matrix.setName("Test Matrix");
        matrix.setHosts(new String[]{String.valueOf(user.getUserId())});
        matrix = carverMatrixRepository.save(matrix);

        // Call service and verify the returned list contains our matrix.
        List<CarverMatrix> matrices = carverMatrixService.getMatricesByHost(user.getUserId());
        assertNotNull(matrices);
        assertThat(matrices).extracting(CarverMatrix::getMatrixId).contains(matrix.getMatrixId());
    }

    // =========================================================================
    // ✅ 2. getMatricesByHost's Input Validation Tests (Unit Test)
    // =========================================================================

    /**
     * **getMatricesByHost - Null Input Exception Test**
     * Verify that passing a null userId to getMatricesByHost throws an IllegalArgumentException.
     */
    @Test
    void testGetMatricesByHost_NullInput() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carverMatrixService.getMatricesByHost(null),
                "Expected getMatricesByHost to throw IllegalArgumentException for null input");
        assertThat(ex.getMessage()).contains("UserId must not be null");
    }

    // =========================================================================
    // ✅ 3. getMatricesByHost's Transactional and Integration Tests
    // =========================================================================
    // (Integration with repository is already covered by the basic test above.)

    // =========================================================================
    // ✅ 4. getMatricesByHost's Edge Case and Exception Handling Tests (Unit Test)
    // =========================================================================
    // (Additional edge case tests can be added if necessary.)

    // =========================================================================
    // ✅ 5. getMatricesByHost's Caching and Performance Tests (if applicable)
    // =========================================================================
    // (Not applicable unless caching is implemented.)

    // -------------------------------------------------------------------------
    // Tests for getMatricesByParticipant Function
    // -------------------------------------------------------------------------

    // =========================================================================
    // ✅ 1. getMatricesByParticipant's Basic Functionality Tests (Unit Test)
    // =========================================================================

    /**
     * **getMatricesByParticipant - Valid Input Test**
     * Verify that getMatricesByParticipant returns the expected matrices for a given participant user ID.
     */
    @Test
    @Transactional
    void testGetMatricesByParticipant_BasicFunctionality() {
        // Create and persist a user.
        User2 user = new User2(null, "participant-001", "Part", "User", "Participant User", "partuser", "participant@example.com", null);
        user = user2Repository.save(user);

        // Create a CarverMatrix with participants including the user's ID.
        CarverMatrix matrix = new CarverMatrix();
        matrix.setName("Participant Matrix");
        matrix.setParticipants(new String[]{String.valueOf(user.getUserId())});
        matrix = carverMatrixRepository.save(matrix);

        // Call service and verify the returned list.
        List<CarverMatrix> matrices = carverMatrixService.getMatricesByParticipant(user.getUserId());
        assertNotNull(matrices);
        assertThat(matrices).extracting(CarverMatrix::getMatrixId).contains(matrix.getMatrixId());
    }

    // =========================================================================
    // ✅ 2. getMatricesByParticipant's Input Validation Tests (Unit Test)
    // =========================================================================

    /**
     * **getMatricesByParticipant - Null Input Exception Test**
     * Verify that passing a null userId to getMatricesByParticipant throws an IllegalArgumentException.
     */
    @Test
    void testGetMatricesByParticipant_NullInput() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carverMatrixService.getMatricesByParticipant(null),
                "Expected getMatricesByParticipant to throw IllegalArgumentException for null input");
        assertThat(ex.getMessage()).contains("UserId must not be null");
    }

    // -------------------------------------------------------------------------
    // Tests for getMatrixById Function
    // -------------------------------------------------------------------------

    // =========================================================================
    // ✅ 1. getMatrixById's Basic Functionality Tests (Unit Test)
    // =========================================================================

    /**
     * **getMatrixById - Valid Input Test**
     * Verify that getMatrixById returns the expected CarverMatrix when given a valid matrixId.
     */
    @Test
    @Transactional
    void testGetMatrixById_BasicFunctionality() {
        // Create and persist a matrix.
        CarverMatrix matrix = new CarverMatrix();
        matrix.setName("Matrix Retrieval Test");
        matrix = carverMatrixRepository.save(matrix);

        CarverMatrix fetchedMatrix = carverMatrixService.getMatrixById(matrix.getMatrixId());
        assertNotNull(fetchedMatrix, "Expected a non-null matrix for a valid matrixId");
        assertEquals(matrix.getMatrixId(), fetchedMatrix.getMatrixId());
    }

    // =========================================================================
    // ✅ 2. getMatrixById's Input Validation Tests (Unit Test)
    // =========================================================================

    /**
     * **getMatrixById - Null Input Exception Test**
     * Verify that passing a null matrixId to getMatrixById throws an IllegalArgumentException.
     */
    @Test
    void testGetMatrixById_NullInput() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carverMatrixService.getMatrixById(null),
                "Expected getMatrixById to throw IllegalArgumentException for null matrixId");
        assertThat(ex.getMessage()).contains("MatrixId must not be null");
    }

    // -------------------------------------------------------------------------
    // Tests for createCarverMatrix Function
    // -------------------------------------------------------------------------

    // =========================================================================
    // ✅ 1. createCarverMatrix's Basic Functionality Tests (Unit Test)
    // =========================================================================

    /**
     * **createCarverMatrix - Valid Input Test**
     * Verify that createCarverMatrix successfully creates and returns a persisted CarverMatrix.
     */
    @Test
    @Transactional
    void testCreateCarverMatrix_BasicFunctionality() {
        // Create and persist a user.
        User2 user = new User2(null, "create-001", "Create", "User", "Create User", "createuser", "create@example.com", null);
        user = user2Repository.save(user);

        // Create a new matrix with a couple of CarverItems.
        CarverMatrix matrix = new CarverMatrix();
        matrix.setName("New Matrix");
        // Set hosts and participants (using string of user id)
        matrix.setHosts(new String[]{String.valueOf(user.getUserId())});
        matrix.setParticipants(new String[]{String.valueOf(user.getUserId())});

        // Add a simple CarverItem.
        CarverItem item = new CarverItem();
        item.setItemName("Test Item");
        matrix.setItems(new ArrayList<>(Arrays.asList(item)));

        CarverMatrix createdMatrix = carverMatrixService.createCarverMatrix(matrix, user.getUserId());
        assertNotNull(createdMatrix.getMatrixId(), "Expected matrixId to be generated for the new matrix");
        // Verify that the CarverItem has been associated with the matrix.
        assertThat(createdMatrix.getItems()).isNotEmpty();
        assertEquals(createdMatrix, createdMatrix.getItems().get(0).getCarverMatrix());
    }

    // =========================================================================
    // ✅ 2. createCarverMatrix's Input Validation Tests (Unit Test)
    // =========================================================================

    /**
     * **createCarverMatrix - Null Matrix Input Test**
     * Verify that passing a null CarverMatrix to createCarverMatrix throws an IllegalArgumentException.
     */
    @Test
    void testCreateCarverMatrix_NullMatrix() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carverMatrixService.createCarverMatrix(null, 1L),
                "Expected createCarverMatrix to throw IllegalArgumentException for null matrix");
        assertThat(ex.getMessage()).contains("CarverMatrix must not be null");
    }

    /**
     * **createCarverMatrix - Null UserId Input Test**
     * Verify that passing a null userId to createCarverMatrix throws an IllegalArgumentException.
     */
    @Test
    void testCreateCarverMatrix_NullUserId() {
        CarverMatrix matrix = new CarverMatrix();
        matrix.setName("Matrix with Null User");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carverMatrixService.createCarverMatrix(matrix, null),
                "Expected createCarverMatrix to throw IllegalArgumentException for null userId");
        assertThat(ex.getMessage()).contains("UserId must not be null");
    }

    // =========================================================================
    // ✅ 3. createCarverMatrix's Transactional and Integration Tests
    // =========================================================================
    // (The basic functionality test above serves as an end-to-end integration test.)

    // =========================================================================
    // ✅ 4. createCarverMatrix's Edge Case and Exception Handling Tests (Unit Test)
    // =========================================================================

    /**
     * **createCarverMatrix - User Not Found Exception Test**
     * Verify that createCarverMatrix throws an IllegalArgumentException when the user is not found.
     */
    @Test
    void testCreateCarverMatrix_UserNotFound() {
        CarverMatrix matrix = new CarverMatrix();
        matrix.setName("Matrix with Invalid User");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carverMatrixService.createCarverMatrix(matrix, 999999L),
                "Expected createCarverMatrix to throw IllegalArgumentException when user is not found");
        assertThat(ex.getMessage()).contains("User not found with ID");
    }

    // =========================================================================
    // ✅ 5. createCarverMatrix's Caching and Performance Tests (if applicable)
    // =========================================================================
    // (Not applicable unless caching is implemented.)

    // -------------------------------------------------------------------------
    // Tests for updateCarverMatrix Function
    // -------------------------------------------------------------------------

    // =========================================================================
    // ✅ 1. updateCarverMatrix's Basic Functionality Tests (Unit Test)
    // =========================================================================

    /**
     * **updateCarverMatrix - Valid Update Test**
     * Verify that updateCarverMatrix successfully updates an existing matrix with new values.
     */
    @Test
    @Transactional
    void testUpdateCarverMatrix_BasicFunctionality() {
        // Create and persist a matrix.
        CarverMatrix matrix = new CarverMatrix();
        matrix.setName("Original Name");
        matrix = carverMatrixRepository.save(matrix);

        // Prepare updated values.
        CarverMatrix updatedMatrix = new CarverMatrix();
        updatedMatrix.setName("Updated Name");
        updatedMatrix.setDescription("Updated Description");

        CarverMatrix updated = carverMatrixService.updateCarverMatrix(matrix.getMatrixId(), updatedMatrix);
        assertEquals("Updated Name", updated.getName());
        assertEquals("Updated Description", updated.getDescription());
    }

    // =========================================================================
    // ✅ 2. updateCarverMatrix's Input Validation Tests (Unit Test)
    // =========================================================================

    /**
     * **updateCarverMatrix - Null MatrixId Test**
     * Verify that passing a null matrixId to updateCarverMatrix throws an IllegalArgumentException.
     */
    @Test
    void testUpdateCarverMatrix_NullMatrixId() {
        CarverMatrix updatedMatrix = new CarverMatrix();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carverMatrixService.updateCarverMatrix(null, updatedMatrix),
                "Expected updateCarverMatrix to throw IllegalArgumentException for null matrixId");
        assertThat(ex.getMessage()).contains("MatrixId must not be null");
    }

    /**
     * **updateCarverMatrix - Null UpdatedMatrix Test**
     * Verify that passing a null updatedMatrix to updateCarverMatrix throws an IllegalArgumentException.
     */
    @Test
    void testUpdateCarverMatrix_NullUpdatedMatrix() {
        CarverMatrix matrix = new CarverMatrix();
        matrix.setName("Test Matrix");
        CarverMatrix savedmatrix = carverMatrixRepository.save(matrix);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carverMatrixService.updateCarverMatrix(savedmatrix.getMatrixId(), null),
                "Expected updateCarverMatrix to throw IllegalArgumentException for null updatedMatrix");
        assertThat(ex.getMessage()).contains("Updated matrix must not be null");
    }

    // =========================================================================
    // ✅ 3. updateCarverMatrix's Transactional and Integration Tests
    // =========================================================================
    // (Basic update test above serves as integration test.)

    // =========================================================================
    // ✅ 4. updateCarverMatrix's Edge Case and Exception Handling Tests (Unit Test)
    // =========================================================================

    /**
     * **updateCarverMatrix - Matrix Not Found Test**
     * Verify that updateCarverMatrix throws an IllegalArgumentException when the matrix is not found.
     */
    @Test
    void testUpdateCarverMatrix_MatrixNotFound() {
        CarverMatrix updatedMatrix = new CarverMatrix();
        updatedMatrix.setName("Non-existent");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carverMatrixService.updateCarverMatrix(999999L, updatedMatrix),
                "Expected updateCarverMatrix to throw IllegalArgumentException when matrix is not found");
        assertThat(ex.getMessage()).contains("CarverMatrix not found");
    }

    // -------------------------------------------------------------------------
    // Tests for searchCarverMatrices Function
    // -------------------------------------------------------------------------

    // =========================================================================
    // ✅ 1. searchCarverMatrices's Basic Functionality Tests (Unit Test)
    // =========================================================================

    /**
     * **searchCarverMatrices - Valid Input Test**
     * Verify that searchCarverMatrices returns matrices that match the search criteria.
     */
    @Test
    @Transactional
    void testSearchCarverMatrices_BasicFunctionality() {
        // Create and persist a user.
        User2 user = new User2(null, "search-001", "Search", "User", "Search User", "searchuser", "search@example.com", null);
        user = user2Repository.save(user);

        // Create two matrices with host/participant values containing the user's email.
        CarverMatrix matrix1 = new CarverMatrix();
        matrix1.setName("Alpha Matrix");
        matrix1.setHosts(new String[]{ user.getEmail() }); // Use email here
        matrix1 = carverMatrixRepository.save(matrix1);

        CarverMatrix matrix2 = new CarverMatrix();
        matrix2.setName("Beta Matrix");
        matrix2.setParticipants(new String[]{ user.getEmail() }); // Use email here
        matrix2 = carverMatrixRepository.save(matrix2);

        // Prepare search parameters: search by name containing "Alpha" and set userEmail.
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("name", "Alpha");
        searchParams.put("userEmail", user.getEmail());

        List<CarverMatrix> results = carverMatrixService.searchCarverMatrices(searchParams);
        assertNotNull(results);
        assertThat(results).extracting(CarverMatrix::getName).contains("Alpha Matrix");
    }


    // =========================================================================
    // ✅ 2. searchCarverMatrices's Input Validation Tests (Unit Test)
    // =========================================================================

    /**
     * **searchCarverMatrices - Null Input Exception Test**
     * Verify that passing a null searchParams map to searchCarverMatrices throws an IllegalArgumentException.
     */
    @Test
    void testSearchCarverMatrices_NullInput() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carverMatrixService.searchCarverMatrices(null),
                "Expected searchCarverMatrices to throw IllegalArgumentException for null search parameters");
        assertThat(ex.getMessage()).contains("Search parameters must not be null");
    }

    // =========================================================================
    // ✅ 3. searchCarverMatrices's Edge Case and Exception Handling Tests (Unit Test)
    // =========================================================================
    // (Additional exception handling tests can be added if necessary.)

    // -------------------------------------------------------------------------
    // Tests for updateCarverItemsFromMap Function
    // -------------------------------------------------------------------------

    // =========================================================================
    // ✅ 1. updateCarverItemsFromMap's Basic Functionality Tests (Unit Test)
    // =========================================================================

    /**
     * **updateCarverItemsFromMap - Valid Input Test**
     * Verify that updateCarverItemsFromMap successfully updates the metrics of CarverItems.
     */
    @Test
    @Transactional
    void testUpdateCarverItemsFromMap_BasicFunctionality() {
        // Create and persist a matrix.
        CarverMatrix matrix = new CarverMatrix();
        matrix.setName("Matrix For Item Update");
        matrix = carverMatrixRepository.save(matrix);

        // Create and persist a CarverItem associated with the matrix.
        CarverItem item = new CarverItem();
        item.setItemName("Item To Update");
        item.setCarverMatrix(matrix);
        item = carverItemRepository.save(item);

        // Prepare an update map for the item.
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("itemId", item.getItemId());
        updateMap.put("criticality", 5);
        updateMap.put("accessibility", 3);

        List<Map<String, Object>> updates = new ArrayList<>();
        updates.add(updateMap);

        // Call service to update the item.
        String userEmail = "tester@example.com";
        List<CarverItem> updatedItems = carverMatrixService.updateCarverItemsFromMap(matrix, updates, userEmail);
        assertNotNull(updatedItems);
        assertFalse(updatedItems.isEmpty());
        CarverItem updatedItem = updatedItems.get(0);
        assertEquals(5, updatedItem.getCriticality().get(userEmail));
        assertEquals(3, updatedItem.getAccessibility().get(userEmail));
    }

    // =========================================================================
    // ✅ 2. updateCarverItemsFromMap's Input Validation Tests (Unit Test)
    // =========================================================================

    /**
     * **updateCarverItemsFromMap - Null Matrix Exception Test**
     * Verify that passing a null matrix to updateCarverItemsFromMap throws an IllegalArgumentException.
     */
    @Test
    void testUpdateCarverItemsFromMap_NullMatrix() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carverMatrixService.updateCarverItemsFromMap(null, new ArrayList<>(), "tester@example.com"),
                "Expected updateCarverItemsFromMap to throw IllegalArgumentException for null matrix");
        assertThat(ex.getMessage()).contains("CarverMatrix must not be null");
    }

    /**
     * **updateCarverItemsFromMap - Null Updates Exception Test**
     * Verify that passing a null updates list to updateCarverItemsFromMap throws an IllegalArgumentException.
     */
    @Test
    void testUpdateCarverItemsFromMap_NullUpdates() {
        CarverMatrix matrix = new CarverMatrix();
        matrix.setName("Test Matrix");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carverMatrixService.updateCarverItemsFromMap(matrix, null, "tester@example.com"),
                "Expected updateCarverItemsFromMap to throw IllegalArgumentException for null updates list");
        assertThat(ex.getMessage()).contains("Updates list must not be null");
    }

    /**
     * **updateCarverItemsFromMap - Null UserEmail Exception Test**
     * Verify that passing a null userEmail to updateCarverItemsFromMap throws an IllegalArgumentException.
     */
    @Test
    void testUpdateCarverItemsFromMap_NullUserEmail() {
        CarverMatrix matrix = new CarverMatrix();
        matrix.setName("Test Matrix");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carverMatrixService.updateCarverItemsFromMap(matrix, new ArrayList<>(), null),
                "Expected updateCarverItemsFromMap to throw IllegalArgumentException for null userEmail");
        assertThat(ex.getMessage()).contains("User email must not be null or empty");
    }

    // =========================================================================
    // ✅ 3. updateCarverItemsFromMap's Edge Case and Exception Handling Tests (Unit Test)
    // =========================================================================

    /**
     * **updateCarverItemsFromMap - CarverItem Not Belonging to Matrix Test**
     * Verify that updateCarverItemsFromMap throws an IllegalArgumentException when a CarverItem does not belong to the given matrix.
     */
    @Test
    @Transactional
    void testUpdateCarverItemsFromMap_ItemNotBelongingToMatrix() {
        // Create two matrices.
        CarverMatrix matrix1 = new CarverMatrix();
        matrix1.setName("Matrix One");
        CarverMatrix matrix1Saved = carverMatrixRepository.save(matrix1);

        CarverMatrix matrix2 = new CarverMatrix();
        matrix2.setName("Matrix Two");
        matrix2 = carverMatrixRepository.save(matrix2);

        // Create a CarverItem for matrix2.
        CarverItem item = new CarverItem();
        item.setItemName("Item In Matrix Two");
        item.setCarverMatrix(matrix2);
        item = carverItemRepository.save(item);

        // Prepare an update map for the item.
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("itemId", item.getItemId());
        updateMap.put("criticality", 4);
        List<Map<String, Object>> updates = new ArrayList<>();
        updates.add(updateMap);

        // Expect an exception because the item does not belong to matrix1.
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carverMatrixService.updateCarverItemsFromMap(matrix1Saved, updates, "tester@example.com"),
                "Expected updateCarverItemsFromMap to throw IllegalArgumentException when item does not belong to matrix");
        assertThat(ex.getMessage()).contains("does not belong to matrix");
    }

    // =========================================================================
    // ✅ 4. updateCarverItemsFromMap's Transactional and Integration Tests
    // =========================================================================
    // (The basic functionality test above serves as an integration test.)

    // =========================================================================
    // ✅ 5. updateCarverItemsFromMap's Caching and Performance Tests (if applicable)
    // =========================================================================
    // (Not applicable unless caching is implemented.)
}
