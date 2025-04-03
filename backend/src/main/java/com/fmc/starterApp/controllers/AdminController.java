package com.fmc.starterApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fmc.starterApp.models.entity.AppUser;
import com.fmc.starterApp.services.AdminService;

import lombok.AllArgsConstructor;

/**
 * REST controller for administration-related operations.
 *
 * <p>This controller provides endpoints for retrieving administrative data and for inserting new
 * users along with their corresponding roles. It leverages the {@link AdminService} to perform
 * the underlying business operations and handles exceptions by returning appropriate HTTP status
 * codes and error messages.
 *
 * <p><strong>Key Endpoints:</strong>
 * <ul>
 *   <li>{@link #usersAdminData()} - Retrieves administrative information including a list of users and the total user count.</li>
 *   <li>{@link #addKeyToRoles(AppUser)} - Inserts a new user and associates relevant roles, returning the created user entity.</li>
 * </ul>
 */
@RestController
@AllArgsConstructor
@RequestMapping({"/api/admin"})
public class AdminController {

    @Autowired
    AdminService adminService;

    /**
     * Retrieves administrative data for users.
     *
     * <p>This endpoint handles GET requests to "/api/admin/users" and delegates to the
     * {@link AdminService#getAdminInfo()} method to fetch administrative information, including the list
     * of all application users and the total user count. On successful retrieval, it returns the data
     * with an HTTP status of OK. In case of an error, it catches the exception and returns a BAD_REQUEST
     * status with the error message.
     *
     * @return a {@link ResponseEntity} containing an {@link com.fmc.starterApp.models.dto.AdminDTO} with user data
     *         and HTTP status OK if successful; otherwise, a BAD_REQUEST status with the error message.
     */
    @GetMapping("/users")
    public ResponseEntity<?> usersAdminData() {
        try {
            return new ResponseEntity<>(adminService.getAdminInfo(), HttpStatus.OK);
        } catch (Exception e) {
            // Optionally log the error here for debugging purposes.
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Inserts a new {@link AppUser} and associates the corresponding roles.
     *
     * <p>This endpoint handles POST requests to "/api/admin/users". It accepts a JSON representation
     * of an {@link AppUser} in the request body and delegates to the {@link AdminService#insertNewUser(AppUser)}
     * method to persist the user and create a corresponding login log. On successful insertion, it returns
     * the persisted user entity with an HTTP status of OK. In case of an error during the operation,
     * it catches the exception and returns a BAD_REQUEST status along with the error message.
     *
     * @param user the {@link AppUser} to be inserted; must not be null.
     * @return a {@link ResponseEntity} containing the persisted {@link AppUser} entity and HTTP status OK if successful;
     *         otherwise, a BAD_REQUEST status with the error message.
     */
    @PostMapping("/users")
    public ResponseEntity<?> addKeyToRoles(@RequestBody AppUser user) {
        try {
            return new ResponseEntity<>(adminService.insertNewUser(user), HttpStatus.OK);
        } catch (Exception e) {
            // Optionally log the error here for further analysis.
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
