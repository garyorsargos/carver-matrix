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

public class User2Service {
    private final User2Repository user2Repository;

    public User2Service(User2Repository user2Repository) {
        this.user2Repository = user2Repository;
    }

    @Transactional
    public User2 insertNewUser(User2 user2) {
        user2Repository.save(user2);
        return user2;
    }

    public User2 upsertUser(Jwt jwt) {
        // Extract the keycloakId (using "sub" as the keycloak id), username, and email from the JWT.
        String keycloakId = jwt.getClaim("sub");
        String username = jwt.getClaim("preferred_username");
        String email = jwt.getClaim("email");
        String firstName = jwt.getClaim("given_name");
        String lastName = jwt.getClaim("family_name");
        String fullName = jwt.getClaim("name");

        Optional<User2> optionalUser = user2Repository.findByKeycloakId(keycloakId);

        if (optionalUser.isPresent()) {
            // User exists - update username and email if necessary.
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
            if (firstName != null && !existingUser.getFirstName().equals(firstName)) {
                existingUser.setFirstName(firstName);
                updated = true;
            }
            if (lastName != null && !existingUser.getLastName().equals(lastName)) {
                existingUser.setLastName(lastName);
                updated = true;
            }
            if (fullName != null && !existingUser.getFullName().equals(fullName)) {
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
    }

    public User2DTO getUserInfo() {
        List<User2> users = user2Repository.findAll();
        return User2DTO.builder()
                .users(users)
                .totalUsers(users.size())
                .build();
    }

    // Deprecated
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
            return jwt.getClaimAsString("sub");
        }
        return null;  // or throw an exception if not authenticated
    }

    // Deprecated
    public JWTInfoDTO extractJwtInfo(Jwt jwt) {
        // Extract basic claims
        String keycloakID = jwt.getClaim("sub");
        String sessionID = jwt.getClaim("sid"); // or "session_state" based on your JWT
        Boolean emailVerified = jwt.getClaim("email_verified");
        String username = jwt.getClaim("preferred_username");
        String firstName = jwt.getClaim("given_name");
        String lastName = jwt.getClaim("family_name");
        String fullName = jwt.getClaim("name");
        String email = jwt.getClaim("email");

        // Extract roles from the "resource_access" claim
        List<String> starterAppRole = new ArrayList<>();
        List<String> accountRole = new ArrayList<>();
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            // Extract roles for "starter-app"
            Map<String, Object> starterApp = (Map<String, Object>) resourceAccess.get("starter-app");
            if (starterApp != null && starterApp.get("roles") instanceof List) {
                starterAppRole = (List<String>) starterApp.get("roles");
            }
            // Extract roles for "account"
            Map<String, Object> account = (Map<String, Object>) resourceAccess.get("account");
            if (account != null && account.get("roles") instanceof List) {
                accountRole = (List<String>) account.get("roles");
            }
        }

        // Build and return the JWTInfoDTO
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
    }
}
