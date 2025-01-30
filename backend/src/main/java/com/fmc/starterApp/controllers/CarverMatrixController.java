package com.fmc.starterApp.controllers;

import com.fmc.starterApp.models.entity.CarverMatrix;
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

    @GetMapping("/participant")
    public ResponseEntity<?> getMatricesByParticipant(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(carverMatrixService.getMatricesByParticipant(userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createCarverMatrix(@RequestBody CarverMatrix carverMatrix, @RequestParam Long userId) {
        try {
            CarverMatrix createdMatrix = carverMatrixService.createCarverMatrix(carverMatrix, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMatrix);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

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
}
