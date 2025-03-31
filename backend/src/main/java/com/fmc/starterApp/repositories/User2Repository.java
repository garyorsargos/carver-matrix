package com.fmc.starterApp.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fmc.starterApp.models.entity.User2;

/**
 * Repository interface for the {@link User2} entity.
 *
 * <p>
 * This interface extends {@link JpaRepository} to provide CRUD operations and additional JPA functionalities
 * for the {@link User2} entity. It serves as the data access layer in our application architecture,
 * abstracting database interactions for the user domain.
 * </p>
 *
 * <p>
 * <strong>Usage in the application:</strong>
 * <ul>
 *   <li><em>Model:</em> The {@link User2} class represents user data.</li>
 *   <li><em>Repository:</em> This interface handles all database interactions related to {@code User2}.</li>
 *   <li><em>Service:</em> The service layer uses this repository to perform business operations on user data.</li>
 *   <li><em>Controller:</em> REST controllers (or other controllers) call the service layer to handle HTTP requests.</li>
 * </ul>
 * </p>
 *
 * @see JpaRepository
 * @see User2
 */
public interface User2Repository extends JpaRepository<User2, Long> {
    /**
     * Finds a {@link User2} entity by its Keycloak identifier.
     *
     * <p>
     * This method returns an {@link Optional} containing the {@link User2} entity if a matching record is found;
     * otherwise, it returns an empty {@link Optional}. This is useful for linking the application's user data
     * with the Keycloak identity provider.
     * </p>
     *
     * @param keycloakId the unique Keycloak identifier of the user.
     * @return an {@link Optional} containing the {@link User2} entity if found, or an empty {@link Optional} if not.
     */
    Optional<User2> findByKeycloakId(String keycloakId);
}
