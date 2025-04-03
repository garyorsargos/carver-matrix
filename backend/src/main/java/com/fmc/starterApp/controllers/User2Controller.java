package com.fmc.starterApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fmc.starterApp.models.dto.JWTInfoDTO;
import com.fmc.starterApp.models.entity.User2;
import com.fmc.starterApp.services.User2Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

/**
 * REST controller for managing User2-related operations.
 *
 * <p>This controller provides endpoints for retrieving user information, inserting new users,
 * extracting JWT-based user details, upserting user records based on JWT claims, and logging out.
 * It delegates business operations to the {@link User2Service} and handles exceptions by returning
 * appropriate HTTP status codes and error messages.
 *
 * <p><strong>Key Endpoints:</strong>
 * <ul>
 *   <li>{@link #user2Data()} - Retrieves user information and total user count.</li>
 *   <li>{@link #addUser2(User2)} - Inserts a new {@link User2} entity.</li>
 *   <li>{@link #whoAmI(Jwt)} - Extracts user details from a JWT token into a {@link JWTInfoDTO}.</li>
 *   <li>{@link #whoAmIUpsert(Jwt)} - Upserts a {@link User2} based on JWT claims and returns the updated entity.</li>
 *   <li>{@link #logout(HttpServletRequest, HttpServletResponse)} - Clears cookies and invalidates the session to log out the user.</li>
 * </ul>
 */
@RestController
@AllArgsConstructor
@RequestMapping({"/api/user2"})
public class User2Controller {

    @Autowired
    User2Service user2Service;

    /**
     * Retrieves administrative user information.
     *
     * <p>This endpoint handles GET requests to "/api/user2/users". It delegates to the
     * {@link User2Service#getUserInfo()} method to fetch a {@link com.fmc.starterApp.models.dto.User2DTO}
     * containing the list of users and the total number of users.
     *
     * <p><strong>Error Handling:</strong> In case of an exception during the operation, a BAD_REQUEST
     * status is returned along with the error message.
     *
     * @return a {@link ResponseEntity} containing the user information and HTTP status OK if successful;
     *         otherwise, a BAD_REQUEST status with the error message.
     */
    @GetMapping("/users")
    public ResponseEntity<?> user2Data() {
        try {
            return new ResponseEntity<>(user2Service.getUserInfo(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Inserts a new {@link User2} entity.
     *
     * <p>This endpoint handles POST requests to "/api/user2/users". It accepts a JSON representation
     * of a {@link User2} object in the request body and delegates to the
     * {@link User2Service#insertNewUser(User2)} method to persist the new user.
     *
     * <p><strong>Error Handling:</strong> If an exception occurs during insertion, a BAD_REQUEST status
     * with the error message is returned.
     *
     * @param user the {@link User2} entity to insert; must not be null.
     * @return a {@link ResponseEntity} containing the persisted {@link User2} entity and HTTP status OK if successful;
     *         otherwise, a BAD_REQUEST status with the error message.
     */
    @PostMapping("/users")
    public ResponseEntity<?> addUser2(@RequestBody User2 user) {
        try {
            return new ResponseEntity<>(user2Service.insertNewUser(user), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Extracts JWT information into a {@link JWTInfoDTO}.
     *
     * <p>This endpoint handles GET requests to "/api/user2/whoami". It extracts user details from the provided
     * JWT token (automatically injected by Spring Security) and delegates to the
     * {@link User2Service#extractJwtInfo(Jwt)} method to build a {@link JWTInfoDTO}.
     *
     * @param jwt the JWT token containing user claims; must not be null.
     * @return a {@link ResponseEntity} containing the {@link JWTInfoDTO} with user details and HTTP status OK.
     */
    @GetMapping("/whoami")
    public ResponseEntity<JWTInfoDTO> whoAmI(@AuthenticationPrincipal Jwt jwt) {
        JWTInfoDTO jwtInfo = user2Service.extractJwtInfo(jwt);
        return ResponseEntity.ok(jwtInfo);
    }

    /**
     * Upserts a {@link User2} entity based on JWT claims.
     *
     * <p>This endpoint handles GET requests to "/api/user2/whoami-upsert". It extracts user details from the provided
     * JWT token (automatically injected by Spring Security) and delegates to the
     * {@link User2Service#upsertUser(Jwt)} method. The method updates an existing user or creates a new user based on
     * the JWT claims.
     *
     * @param jwt the JWT token containing user claims; must not be null.
     * @return a {@link ResponseEntity} containing the updated or newly created {@link User2} entity and HTTP status OK.
     */
    @GetMapping("/whoami-upsert")
    public ResponseEntity<User2> whoAmIUpsert(@AuthenticationPrincipal Jwt jwt) {
        User2 user = user2Service.upsertUser(jwt);
        return ResponseEntity.ok(user);
    }

    /**
     * Logs out the current user by clearing cookies and invalidating the session.
     *
     * <p>This endpoint handles GET requests to "/api/user2/logout". It clears all cookies by setting their value
     * to an empty string, path to "/", and max age to zero. Additionally, it invalidates the current HTTP session.
     *
     * @param request  the {@link HttpServletRequest} containing the user's cookies.
     * @param response the {@link HttpServletResponse} to which the cleared cookies are added.
     * @return a {@link ResponseEntity} with HTTP status OK if the logout operation is successful.
     */
    @GetMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // Clear all cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }

        // Invalidate the current session
        request.getSession().invalidate();

        // Return success status
        return ResponseEntity.ok().build();
    }
}
