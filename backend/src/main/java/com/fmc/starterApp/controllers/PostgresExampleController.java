package com.fmc.starterApp.controllers;

import com.fmc.starterApp.models.entity.PostgresExampleObject;
import com.fmc.starterApp.services.PostGresExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller class for handling REST API requests related to {@link PostgresExampleObject}.
 *
 * <p>This controller provides endpoints to create, retrieve, update, and delete instances of
 * {@link PostgresExampleObject} via HTTP requests. It interacts with the
 * {@link PostGresExampleService} to perform business operations and handles error scenarios by
 * catching exceptions and returning appropriate HTTP status codes along with error messages.
 *
 * <p><strong>Key Endpoints:</strong>
 * <ul>
 *   <li>{@link #saveDbObject(PostgresExampleObject)} - Creates a new database object.</li>
 *   <li>{@link #getDbObjects(Optional)} - Retrieves a single object by ID or all objects if no ID is provided.</li>
 *   <li>{@link #updateObject(PostgresExampleObject)} - Updates an existing database object.</li>
 *   <li>{@link #deleteObjectById(Long)} - Deletes an object by its ID.</li>
 * </ul>
 */
@Controller
@RequestMapping({"/api"})
public class PostgresExampleController {
    
    @Autowired
    PostGresExampleService postGresExampleService;

    /**
     * Persists a new {@link PostgresExampleObject} using the service layer.
     *
     * <p>This endpoint accepts a JSON representation of a {@link PostgresExampleObject} and delegates
     * to the {@link PostGresExampleService#insertExample(PostgresExampleObject)} method to save the object.
     * If the operation is successful, it returns the name of the saved object along with an HTTP OK status.
     * In case of any exception, it returns a BAD_REQUEST status with the error message.
     *
     * @param test the {@link PostgresExampleObject} to be saved; must not be null.
     * @return a {@link ResponseEntity} containing the name of the saved object and HTTP status OK if successful;
     *         otherwise, a BAD_REQUEST status with the error message.
     */
    @PostMapping("/db")
    public ResponseEntity<String> saveDbObject(@RequestBody PostgresExampleObject test) {
        try {
            return new ResponseEntity<>(postGresExampleService.insertExample(test), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retrieves {@link PostgresExampleObject} instances based on the provided ID.
     *
     * <p>If an ID is provided in the URL path, the method retrieves the corresponding object using
     * {@link PostGresExampleService#getObjectById(Long)}. If no ID is provided, it returns a list of all
     * available {@link PostgresExampleObject} instances via {@link PostGresExampleService#getAllPostGres()}.
     * In case of an exception, the endpoint returns a BAD_REQUEST status with the error message.
     *
     * @param id an optional {@link Long} representing the unique identifier of the object to retrieve.
     *           If empty, all objects are returned.
     * @return a {@link ResponseEntity} containing either the single object or the list of objects along with HTTP status OK;
     *         in case of an error, a BAD_REQUEST status with the error message.
     */
    @GetMapping(value = {"/db", "/db/{id}"})
    public ResponseEntity<?> getDbObjects(@PathVariable(required = false) Optional<Long> id) {
        try {
            if (id.isPresent()) {
                return new ResponseEntity<PostgresExampleObject>(postGresExampleService.getObjectById(id.get()), HttpStatus.OK);
            } else {
                return new ResponseEntity<List<PostgresExampleObject>>(postGresExampleService.getAllPostGres(), HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Updates an existing {@link PostgresExampleObject} or inserts it if it does not already exist.
     *
     * <p>This endpoint handles HTTP PUT requests by accepting a JSON representation of a
     * {@link PostgresExampleObject}. It delegates to the {@link PostGresExampleService#insertExample(PostgresExampleObject)}
     * method to perform the update operation, which may function as an upsert operation.
     * On success, it returns the name of the updated object with an HTTP OK status; otherwise,
     * it returns a BAD_REQUEST status with the error message.
     *
     * @param test the {@link PostgresExampleObject} to update; must not be null.
     * @return a {@link ResponseEntity} containing the name of the updated object and HTTP status OK if successful;
     *         otherwise, a BAD_REQUEST status with the error message.
     */
    @PutMapping("/db")
    public ResponseEntity<String> updateObject(@RequestBody PostgresExampleObject test) {
        try {
            return new ResponseEntity<>(postGresExampleService.insertExample(test), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Deletes the {@link PostgresExampleObject} with the specified ID.
     *
     * <p>This endpoint handles HTTP DELETE requests by delegating to the
     * {@link PostGresExampleService#deleteById(Long)} method to remove the object from the database.
     * If the deletion is successful, it returns the ID of the deleted object with an HTTP OK status.
     * In case of an exception, it returns a BAD_REQUEST status with the error message.
     *
     * @param id the unique identifier of the object to delete.
     * @return a {@link ResponseEntity} containing the ID of the deleted object and HTTP status OK if successful;
     *         otherwise, a BAD_REQUEST status with the error message.
     */
    @DeleteMapping("/db/{id}")
    public ResponseEntity<?> deleteObjectById(@PathVariable Long id) {
        try {
            return new ResponseEntity<>(postGresExampleService.deleteById(id), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
