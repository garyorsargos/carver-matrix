package com.fmc.starterApp.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

/**
 * REST controller for application health checks.
 *
 * <p>This controller provides an endpoint to verify the health status of the application.
 * The health check endpoint responds with a simple message confirming that the application is running
 * as expected. It is commonly used by monitoring tools and load balancers to check the application's availability.
 *
 * <p><strong>Endpoint:</strong>
 * <ul>
 *   <li>{@link #healthcheck()} - Returns a health status message along with HTTP status OK.</li>
 * </ul>
 */
@RestController
@AllArgsConstructor
@RequestMapping({"/api/health"})
public class Health {

    /**
     * Performs a health check for the application.
     *
     * <p>This method handles GET requests to "/api/health" and returns a response entity containing a
     * message that indicates the application is healthy. It returns an HTTP status of OK (200).
     *
     * @return a {@link ResponseEntity} containing the string "app is healthy" and an HTTP status of OK.
     */
    @GetMapping()
    public ResponseEntity<String> healthcheck() {
        return new ResponseEntity<>("app is healthy", HttpStatus.OK);
    }
}
