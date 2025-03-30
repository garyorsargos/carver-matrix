package com.fmc.starterApp.controllers;

import com.fmc.starterApp.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fmc.starterApp.models.entity.MatrixImage;
import com.fmc.starterApp.repositories.MatrixImageRepository;

import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import java.time.LocalDateTime;
import com.fmc.starterApp.services.ImageService;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private MatrixImageRepository matrixImageRepository;

    @Value("${AWS_S3_BUCKET_NAME}")
    private String bucketName;

    @Autowired
    private ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file,@RequestParam("matrixId") Long matrixId,@RequestParam(value = "itemId", required = false) Long itemId) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is required.");
        }

        if (matrixId == null) {
            return ResponseEntity.badRequest().body("Matrix ID is required.");
        }

        try {
            String fileUrl = imageService.uploadImage(file, matrixId, itemId);
            return ResponseEntity.ok(fileUrl);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getImageById(@PathVariable Long id) {
        try {
            MatrixImage matrixImage = matrixImageRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Image not found with ID: " + id));
            return ResponseEntity.ok(matrixImage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteImageById(@PathVariable Long id) {
        try {
            MatrixImage matrixImage = matrixImageRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Image not found with ID: " + id));

            String imageUrl = matrixImage.getImageUrl();
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

            //Delete from S3
            s3Client.deleteObject(b -> b.bucket(bucketName).key(fileName));
            System.out.println("Deleted image from S3: " + fileName);

            //Delete from database
            matrixImageRepository.delete(matrixImage);
            System.out.println("Deleted image metadata from database: " + id);

            return ResponseEntity.ok("Image deleted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/matrix/{matrixId}")
    public ResponseEntity<?> getImagesByMatrixId(@PathVariable Long matrixId) {
        try {
            List<Map<String, Object>> images = matrixImageRepository.findAll().stream().filter(img -> img.getCarverMatrix() != null && img.getCarverMatrix().getMatrixId().equals(matrixId)).map(img -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("imageId", img.getImageId());
                    map.put("imageUrl", img.getImageUrl());
                    map.put("itemId", img.getCarverItem() != null ? img.getCarverItem().getItemId() : null);
                    return map;
                }).toList();
            Map<String, Object> response = new HashMap<>();
            response.put("images", images);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

}

