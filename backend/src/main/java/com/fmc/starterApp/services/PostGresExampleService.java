package com.fmc.starterApp.services;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.fmc.starterApp.models.entity.PostgresExampleObject;
import com.fmc.starterApp.repositories.PostgresRepository;

/**
 * Service class for managing {@link PostgresExampleObject} entities.
 *
 * <p>This service provides methods to insert, retrieve, and delete
 * {@link PostgresExampleObject} entities using the {@link PostgresRepository}.
 * It handles errors gracefully by catching exceptions and wrapping them in
 * meaningful runtime exceptions.</p>
 *
 * <p><strong>Key Methods:</strong>
 * <ul>
 *   <li>{@link #insertExample(PostgresExampleObject)}: Persists a new example object and returns its name.</li>
 *   <li>{@link #getObjectById(Long)}: Retrieves an example object by its ID.</li>
 *   <li>{@link #getAllPostGres()}: Retrieves all example objects.</li>
 *   <li>{@link #deleteById(Long)}: Deletes an example object by its ID in a transactional context.</li>
 * </ul>
 * </p>
 */
public class PostGresExampleService {
    private final PostgresRepository postgresRepository;

    /**
     * Constructs a {@link PostGresExampleService} with the specified repository.
     *
     * @param postgresRepository the repository for managing {@link PostgresExampleObject} entities; must not be null.
     */
    public PostGresExampleService(final PostgresRepository postgresRepository) {
        this.postgresRepository = postgresRepository;
    }

    /**
     * Inserts a new {@link PostgresExampleObject} into the database.
     *
     * @param postgresExampleObject the object to be inserted; must not be null.
     * @return the name of the saved object.
     * @throws IllegalArgumentException if the provided object is null.
     * @throws RuntimeException         if the repository operation fails.
     */
    public String insertExample(PostgresExampleObject postgresExampleObject) {
        if (postgresExampleObject == null) {
            throw new IllegalArgumentException("PostgresExampleObject must not be null");
        }
        try {
            PostgresExampleObject savedObject = postgresRepository.save(postgresExampleObject);
            return savedObject.getName();
        } catch (Exception e) {
            // Optionally log the error here
            throw new RuntimeException("Failed to insert example object", e);
        }
    }

    /**
     * Retrieves a {@link PostgresExampleObject} by its ID.
     *
     * @param id the unique identifier of the object.
     * @return the {@link PostgresExampleObject} if found, or null if not.
     * @throws RuntimeException if the repository operation fails.
     */
    public PostgresExampleObject getObjectById(Long id) {
        try {
            return postgresRepository.findFirstById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve object by id: " + id, e);
        }
    }

    /**
     * Retrieves all {@link PostgresExampleObject} entities.
     *
     * @return a list of all {@link PostgresExampleObject} entities.
     * @throws RuntimeException if the repository operation fails.
     */
    public List<PostgresExampleObject> getAllPostGres() {
        try {
            return postgresRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve all PostgresExampleObjects", e);
        }
    }

    /**
     * Deletes the {@link PostgresExampleObject} with the specified ID.
     *
     * <p>This method is executed within a transaction to ensure that the deletion is rolled back
     * in case of an error.</p>
     *
     * @param id the unique identifier of the object to delete.
     * @return the ID of the deleted object.
     * @throws RuntimeException if the repository operation fails.
     */
    @Transactional
    public Long deleteById(Long id) {
        try {
            postgresRepository.deleteAllById(id);
            return id;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete object with id: " + id, e);
        }
    }
}
