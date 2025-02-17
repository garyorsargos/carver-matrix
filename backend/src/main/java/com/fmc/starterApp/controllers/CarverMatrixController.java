package com.fmc.starterApp.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import com.fmc.starterApp.models.entity.CarverMatrix;
import com.fmc.starterApp.services.CarverMatrixService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/carvermatrices")
public class CarverMatrixController {

    @Autowired
    CarverMatrixService carverMatrixService;

    @GetMapping("/{matrixId}")
    public ResponseEntity<?> getCarverMatrixByCarverId(@PathVariable Long matrixId) {
        try {
            CarverMatrix matrix = carverMatrixService.getMatrixById(matrixId);
            return ResponseEntity.ok(matrix);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

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

    @GetMapping("/search")
    public ResponseEntity<?> searchCarverMatrices(@RequestParam Map<String, String> searchParams) {
        try {
            List<CarverMatrix> results = carverMatrixService.searchCarverMatrices(searchParams);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
