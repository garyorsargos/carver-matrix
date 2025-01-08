package com.fmc.starterApp.controllers;

import com.fmc.starterApp.models.CarverMatrix;
import com.fmc.starterApp.services.CarverMatrixService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/carvermatrices")
public class CarverMatrixController {

    @Autowired
    private CarverMatrixService carverMatrixService;

    //Endpoint to get matrices where the user is a host
    @GetMapping("/host")
    public ResponseEntity<?> getMatricesByHost(@RequestParam Long userId) {
        try {
            //Fetch matrices where the user is a host
            List<CarverMatrix> matrices = carverMatrixService.getMatricesByHost(userId);
            return new ResponseEntity<>(matrices, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); // Log the error for debugging
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Endpoint to get matrices where the user is a participant
    @GetMapping("/participant")
    public ResponseEntity<?> getMatricesByParticipant(@RequestParam Long userId) {
        try {
            return new ResponseEntity<>(carverMatrixService.getMatricesByParticipant(userId), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    //Endpoint to create a Carver Matrix
    @PostMapping("/create")
    public ResponseEntity<?> createCarverMatrix(@RequestBody CarverMatrix carverMatrix, @RequestParam Long userId) {
        try {
            CarverMatrix createdMatrix = carverMatrixService.createCarverMatrix(carverMatrix, userId);
            return new ResponseEntity<>(createdMatrix, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
