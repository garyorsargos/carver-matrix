package com.fmc.starterApp.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fmc.starterApp.models.entity.CarverItem;
import com.fmc.starterApp.models.entity.CarverMatrix;
import com.fmc.starterApp.services.CarverMatrixService;

import lombok.AllArgsConstructor;

/**
 * REST controller for managing CarverMatrix entities and their associated CarverItems.
 *
 * <p>This controller provides endpoints to retrieve, create, update, and search CarverMatrix objects,
 * as well as update CarverItem entities. It delegates business logic to the {@link CarverMatrixService}
 * and handles errors by returning appropriate HTTP status codes and messages.
 *
 * <p><strong>Key Endpoints:</strong>
 * <ul>
 *   <li>{@link #getCarverMatrixByCarverId(Long)} - Retrieves a CarverMatrix by its unique ID.</li>
 *   <li>{@link #getMatricesByHost(Long)} - Retrieves all matrices where a given user is a host.</li>
 *   <li>{@link #getMatricesByParticipant(Long)} - Retrieves all matrices where a given user is a participant.</li>
 *   <li>{@link #createCarverMatrix(CarverMatrix, Long)} - Creates a new CarverMatrix for a specified user.</li>
 *   <li>{@link #updateCarverMatrix(Long, CarverMatrix)} - Updates an existing CarverMatrix.</li>
 *   <li>{@link #searchCarverMatrices(Map, Jwt)} - Searches for matrices based on provided parameters and the authenticated user's email.</li>
 *   <li>{@link #updateCarverItems(Long, List, Jwt)} - Updates CarverItem entities for a given matrix based on provided update maps.</li>
 * </ul>
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/carvermatrices")
public class CarverMatrixController {

    @Autowired
    CarverMatrixService carverMatrixService;

    /**
     * Retrieves a CarverMatrix by its unique matrix ID.
     *
     * <p>This endpoint handles GET requests to "/api/carvermatrices/{matrixId}".
     * It delegates to {@link CarverMatrixService#getMatrixById(Long)} to retrieve the matrix.
     * On success, it returns the matrix with an HTTP status of OK.
     * In case of failure, it returns an INTERNAL_SERVER_ERROR status with an error message.
     *
     * @param matrixId the unique identifier of the CarverMatrix to retrieve.
     * @return a {@link ResponseEntity} containing the CarverMatrix and HTTP status OK if found;
     *         otherwise, an error message with HTTP status INTERNAL_SERVER_ERROR.
     */
    @GetMapping("/{matrixId}")
    public ResponseEntity<?> getCarverMatrixByCarverId(@PathVariable Long matrixId) {
        try {
            CarverMatrix matrix = carverMatrixService.getMatrixById(matrixId);
            return ResponseEntity.ok(matrix);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Retrieves all CarverMatrix objects where the specified user is a host.
     *
     * <p>This endpoint handles GET requests to "/api/carvermatrices/host" and accepts a userId as a request parameter.
     * It delegates to {@link CarverMatrixService#getMatricesByHost(Long)} to retrieve the list of matrices.
     *
     * @param userId the ID of the user who is hosting the matrices.
     * @return a {@link ResponseEntity} containing a list of CarverMatrix objects with HTTP status OK if successful;
     *         otherwise, an error message with HTTP status INTERNAL_SERVER_ERROR.
     */
    @GetMapping("/host")
    public ResponseEntity<?> getMatricesByHost(@RequestParam Long userId) {
        try {
            List<CarverMatrix> matrices = carverMatrixService.getMatricesByHost(userId);
            return ResponseEntity.ok(matrices);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Retrieves all CarverMatrix objects where the specified user is a participant.
     *
     * <p>This endpoint handles GET requests to "/api/carvermatrices/participant" and accepts a userId as a request parameter.
     * It delegates to {@link CarverMatrixService#getMatricesByParticipant(Long)} to retrieve the list of matrices.
     *
     * @param userId the ID of the user who is a participant in the matrices.
     * @return a {@link ResponseEntity} containing a list of CarverMatrix objects with HTTP status OK if successful;
     *         otherwise, an error message with HTTP status INTERNAL_SERVER_ERROR.
     */
    @GetMapping("/participant")
    public ResponseEntity<?> getMatricesByParticipant(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(carverMatrixService.getMatricesByParticipant(userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Creates a new CarverMatrix for the specified user.
     *
     * <p>This endpoint handles POST requests to "/api/carvermatrices/create". It accepts a JSON representation
     * of a CarverMatrix in the request body and a userId as a request parameter.
     * The matrix is created by delegating to {@link CarverMatrixService#createCarverMatrix(CarverMatrix, Long)}.
     * On success, it returns the created matrix with an HTTP status of CREATED.
     *
     * @param carverMatrix the CarverMatrix object to create; must not be null.
     * @param userId       the ID of the user creating the matrix; must not be null.
     * @return a {@link ResponseEntity} containing the created CarverMatrix and HTTP status CREATED if successful;
     *         otherwise, an error message with HTTP status INTERNAL_SERVER_ERROR.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createCarverMatrix(@RequestBody CarverMatrix carverMatrix, @RequestParam Long userId) {
        try {
            CarverMatrix createdMatrix = carverMatrixService.createCarverMatrix(carverMatrix, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMatrix);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Updates an existing CarverMatrix with new values.
     *
     * <p>This endpoint handles PUT requests to "/api/carvermatrices/{matrixId}/update". It accepts a matrixId as a path variable
     * and a JSON representation of the updated CarverMatrix in the request body.
     * It delegates to {@link CarverMatrixService#updateCarverMatrix(Long, CarverMatrix)} to perform the update.
     * If the matrix is not found, it returns a NOT_FOUND status; for other errors, it returns an INTERNAL_SERVER_ERROR.
     *
     * @param matrixId      the unique identifier of the CarverMatrix to update.
     * @param updatedMatrix the CarverMatrix object containing updated values; must not be null.
     * @return a {@link ResponseEntity} containing the updated CarverMatrix and HTTP status OK if successful;
     *         otherwise, an error message with HTTP status NOT_FOUND or INTERNAL_SERVER_ERROR.
     */
    @PutMapping("/{matrixId}/update")
    public ResponseEntity<?> updateCarverMatrix(@PathVariable Long matrixId, @RequestBody CarverMatrix updatedMatrix) {
        try {
            CarverMatrix updated = carverMatrixService.updateCarverMatrix(matrixId, updatedMatrix);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Searches for CarverMatrix objects based on provided search parameters.
     *
     * <p>This endpoint handles GET requests to "/api/carvermatrices/search". It accepts a map of search parameters as request parameters
     * and extracts the authenticated user's email from the provided JWT token.
     * The user's email is added to the search parameters before delegating to {@link CarverMatrixService#searchCarverMatrices(Map)}.
     *
     * @param searchParams a map of search parameters; must not be null.
     * @param jwt          the JWT token containing authenticated user information; must not be null.
     * @return a {@link ResponseEntity} containing a list of CarverMatrix objects matching the search criteria with HTTP status OK if successful;
     *         otherwise, an error message with HTTP status INTERNAL_SERVER_ERROR.
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchCarverMatrices(@RequestParam Map<String, String> searchParams, @AuthenticationPrincipal Jwt jwt) {
        try {
            // Get user's email from JWT token
            String userEmail = jwt.getClaim("email").toString().trim().toLowerCase();
            if (userEmail == null || userEmail.isEmpty()) {
                return ResponseEntity.badRequest().body("User email not found in token");
            }
            
            // Add user's email to search params
            searchParams.put("userEmail", userEmail);
            
            List<CarverMatrix> results = carverMatrixService.searchCarverMatrices(searchParams);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    /**
     * Updates CarverItem objects for a specified CarverMatrix based on provided update maps.
     *
     * <p>This endpoint handles PUT requests to "/api/carvermatrices/{matrixId}/carveritems/update".
     * It accepts a matrixId as a path variable, a list of update maps in the request body, and extracts the authenticated user's email from the provided JWT token.
     * It first retrieves the CarverMatrix by ID; if not found, it returns a NOT_FOUND status.
     * Then, it delegates to {@link CarverMatrixService#updateCarverItemsFromMap(CarverMatrix, List, String)} to update the CarverItems.
     *
     * @param matrixId the unique identifier of the CarverMatrix whose items are to be updated.
     * @param updates  a list of maps containing update data for CarverItem objects; must not be null.
     * @param jwt      the JWT token containing authenticated user information; must not be null.
     * @return a {@link ResponseEntity} containing the list of updated CarverItem objects with HTTP status OK if successful;
     *         otherwise, an error message with an appropriate HTTP status.
     */
    @PutMapping("/{matrixId}/carveritems/update")
    public ResponseEntity<?> updateCarverItems(@PathVariable Long matrixId, @RequestBody List<Map<String, Object>> updates, @AuthenticationPrincipal Jwt jwt) {
        try {
            CarverMatrix matrix = carverMatrixService.getMatrixById(matrixId);
            if (matrix == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("CarverMatrix not found with ID: " + matrixId);
            }

            String userEmail = jwt.getClaimAsString("email");
            List<CarverItem> updatedItems = carverMatrixService.updateCarverItemsFromMap(matrix, updates, userEmail);
            return ResponseEntity.ok(updatedItems);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * DELETE endpoint for deleting a CarverMatrix by its matrixId.
     * <p>
     * On successful deletion, this endpoint returns a 200 OK status with a success message.
     * If the CarverMatrix is not found, a 404 Not Found status is returned.
     * For any other errors, a 500 Internal Server Error status is returned.
     * </p>
     *
     * @param matrixId the unique identifier of the CarverMatrix to be deleted
     * @return a ResponseEntity indicating the outcome of the deletion operation
     */
    @DeleteMapping("/{matrixId}")
    public ResponseEntity<?> deleteCarverMatrix(@PathVariable Long matrixId) {
        try {
            carverMatrixService.deleteCarverMatrix(matrixId);
            return new ResponseEntity("CarverMatrix deleted successfully", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
