package com.fmc.starterApp.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import com.fmc.starterApp.models.dto.JWTInfoDTO;
import com.fmc.starterApp.models.dto.User2DTO;
import com.fmc.starterApp.models.entity.User2;
import com.fmc.starterApp.repositories.User2Repository;

/**
 * Service class for managing application users.
 *
 * <p>This service provides methods to insert or update user information based on JWT claims,
 * retrieve user information, and extract user-related details from a JWT token.
 * The service interacts with the {@link User2Repository} for CRUD operations.
 *
 * <p><strong>Key Methods:</strong>
 * <ul>
 *   <li>{@link #insertNewUser(User2)}: Persists a new {@link User2} entity.</li>
 *   <li>{@link #upsertUser(Jwt)}: Updates an existing user or creates a new user based on JWT claims.</li>
 *   <li>{@link #getUserInfo()}: Retrieves user information and total count in a {@link User2DTO}.</li>
 *   <li>{@link #getCurrentUserId()}: (Deprecated) Retrieves the current authenticated user's Keycloak ID from the security context.</li>
 *   <li>{@link #extractJwtInfo(Jwt)}: (Deprecated) Extracts user details from a JWT into a {@link JWTInfoDTO}.</li>
 * </ul>
 */
public class User2Service {
    private final User2Repository user2Repository;

    /**
     * Constructs a User2Service with the specified User2Repository.
     *
     * @param user2Repository the repository for managing {@link User2} entities; must not be null.
     */
    public User2Service(User2Repository user2Repository) {
        this.user2Repository = user2Repository;
    }

    /**
     * Persists a new {@link User2} entity.
     *
     * <p>This method saves the provided user into the repository.
     * It is executed within a transaction.
     *
     * @param user2 the {@link User2} entity to persist; must not be null.
     * @return the persisted {@link User2} entity.
     * @throws IllegalArgumentException if the provided user2 is null.
     * @throws RuntimeException if the repository operation fails.
     */
    @Transactional
    public User2 insertNewUser(User2 user2) {
        if (user2 == null) {
            throw new IllegalArgumentException("User2 must not be null");
        }
        try {
            return user2Repository.save(user2);
        } catch (Exception e) {
            // Optionally log the error here
            throw new RuntimeException("Failed to insert new user", e);
        }
    }

    /**
     * Updates an existing {@link User2} entity or creates a new one based on JWT claims.
     *
     * <p>This method extracts user details from the provided JWT token and checks if a user with the given Keycloak ID exists.
     * If the user exists, their details are updated if they differ; otherwise, a new user is created.
     *
     * @param jwt the JWT token containing user claims; must not be null.
     * @return the updated or newly created {@link User2} entity.
     * @throws IllegalArgumentException if the provided JWT is null.
     * @throws RuntimeException if an error occurs during repository operations.
     */
    public User2 upsertUser(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalArgumentException("JWT must not be null");
        }
        try {
            // Extract user details from JWT claims.
            String keycloakId = jwt.getClaim("sub");
            String username = jwt.getClaim("preferred_username");
            String email = jwt.getClaim("email");
            String firstName = jwt.getClaim("given_name");
            String lastName = jwt.getClaim("family_name");
            String fullName = jwt.getClaim("name");

            Optional<User2> optionalUser = user2Repository.findByKeycloakId(keycloakId);

            if (optionalUser.isPresent()) {
                // User exists - update details if necessary.
                User2 existingUser = optionalUser.get();
                boolean updated = false;

                if (!existingUser.getUsername().equals(username)) {
                    existingUser.setUsername(username);
                    updated = true;
                }
                if (!existingUser.getEmail().equals(email)) {
                    existingUser.setEmail(email);
                    updated = true;
                }
                if (firstName != null && !firstName.equals(existingUser.getFirstName())) {
                    existingUser.setFirstName(firstName);
                    updated = true;
                }
                if (lastName != null && !lastName.equals(existingUser.getLastName())) {
                    existingUser.setLastName(lastName);
                    updated = true;
                }
                if (fullName != null && !fullName.equals(existingUser.getFullName())) {
                    existingUser.setFullName(fullName);
                    updated = true;
                }
                if (updated) {
                    return user2Repository.save(existingUser);
                } else {
                    return existingUser;
                }
            } else {
                // User does not exist - create a new user.
                User2 newUser = new User2();
                newUser.setKeycloakId(keycloakId);
                newUser.setUsername(username);
                newUser.setEmail(email);
                newUser.setFirstName(firstName);
                newUser.setLastName(lastName);
                newUser.setFullName(fullName);
                return user2Repository.save(newUser);
            }
        } catch (Exception e) {
            // Optionally log the error here
            throw new RuntimeException("Failed to upsert user based on JWT", e);
        }
    }

    /**
     * Retrieves administrative user information.
     *
     * <p>This method retrieves all users from the repository and builds a {@link User2DTO}
     * containing the list of users and the total number of users.
     *
     * @return a {@link User2DTO} with user information.
     * @throws RuntimeException if the repository query fails.
     */
    public User2DTO getUserInfo() {
        try {
            List<User2> users = user2Repository.findAll();
            return User2DTO.builder()
                    .users(users)
                    .totalUsers(users.size())
                    .build();
        } catch (Exception e) {
            // Optionally log the error here
            throw new RuntimeException("Failed to retrieve user information", e);
        }
    }

    /**
     * Retrieves the current authenticated user's Keycloak ID.
     *
     * <p>This deprecated method extracts the Keycloak ID (sub claim) from the JWT token in the security context.
     * If the authentication token is not an instance of {@link JwtAuthenticationToken}, it returns null.
     *
     * @return the current user's Keycloak ID, or null if not authenticated.
     * @deprecated Use a dedicated security service instead.
     */
    @Deprecated
    public String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken) {
                Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
                return jwt.getClaimAsString("sub");
            }
            return null;  // Optionally, throw an exception if not authenticated.
        } catch (Exception e) {
            // Optionally log the error here
            throw new RuntimeException("Failed to retrieve current user ID", e);
        }
    }

    /**
     * Extracts JWT information into a {@link JWTInfoDTO}.
     *
     * <p>This deprecated method extracts basic claims from the provided JWT token, including user identification and roles,
     * and builds a {@link JWTInfoDTO} containing this information.
     *
     * @param jwt the JWT token from which to extract information; must not be null.
     * @return a {@link JWTInfoDTO} populated with claims from the JWT.
     * @throws IllegalArgumentException if the provided JWT is null.
     * @deprecated Use a dedicated security service instead.
     */
    @Deprecated
    public JWTInfoDTO extractJwtInfo(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalArgumentException("JWT must not be null");
        }
        try {
            // Extract basic claims.
            String keycloakID = jwt.getClaim("sub");
            String sessionID = jwt.getClaim("sid");
            Boolean emailVerified = jwt.getClaim("email_verified");
            String username = jwt.getClaim("preferred_username");
            String firstName = jwt.getClaim("given_name");
            String lastName = jwt.getClaim("family_name");
            String fullName = jwt.getClaim("name");
            String email = jwt.getClaim("email");

            // Extract roles from the "resource_access" claim.
            List<String> starterAppRole = new ArrayList<>();
            List<String> accountRole = new ArrayList<>();
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                Map<String, Object> starterApp = (Map<String, Object>) resourceAccess.get("starter-app");
                if (starterApp != null && starterApp.get("roles") instanceof List) {
                    starterAppRole = (List<String>) starterApp.get("roles");
                }
                Map<String, Object> account = (Map<String, Object>) resourceAccess.get("account");
                if (account != null && account.get("roles") instanceof List) {
                    accountRole = (List<String>) account.get("roles");
                }
            }

            return JWTInfoDTO.builder()
                    .keycloakID(keycloakID)
                    .sessionID(sessionID)
                    .emailVerified(emailVerified)
                    .username(username)
                    .firstName(firstName)
                    .lastName(lastName)
                    .fullName(fullName)
                    .email(email)
                    .starterAppRole(starterAppRole)
                    .accountRole(accountRole)
                    .build();
        } catch (Exception e) {
            // Optionally log the error here
            throw new RuntimeException("Failed to extract JWT information", e);
        }
    }
}
