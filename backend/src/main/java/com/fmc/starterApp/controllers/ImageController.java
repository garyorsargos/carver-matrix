package com.fmc.starterApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fmc.starterApp.models.entity.MatrixImage;
import com.fmc.starterApp.repositories.MatrixImageRepository;
import com.fmc.starterApp.services.ImageService;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * REST controller for handling image operations associated with Carver Matrices.
 *
 * <p>This controller provides endpoints to upload images, retrieve image metadata, and delete images.
 * It leverages the {@link ImageService} for handling file uploads to AWS S3 as well as storing image metadata
 * in the database via the {@link MatrixImageRepository}. It also utilizes an AWS S3 client to interact with S3.
 *
 * <p><strong>Key Endpoints:</strong>
 * <ul>
 *   <li>{@link #uploadImage(MultipartFile, Long)} - Uploads an image file and associates it with a given matrix.</li>
 *   <li>{@link #getImageById(Long)} - Retrieves image metadata by its unique identifier.</li>
 *   <li>{@link #deleteImageById(Long)} - Deletes an image from both AWS S3 and the database.</li>
 * </ul>
 */
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

    /**
     * Uploads an image file and associates it with a specific Carver Matrix.
     *
     * <p>This endpoint handles POST requests to "/api/images/upload". It expects a multipart file and a matrixId as request parameters.
     * The method validates the input, delegates the upload operation to {@link ImageService#uploadImage(MultipartFile, Long)},
     * and returns the public URL of the uploaded image. In case of input validation errors or exceptions during upload,
     * appropriate HTTP error statuses and messages are returned.
     *
     * @param file     the multipart file to be uploaded; must not be empty.
     * @param matrixId the ID of the Carver Matrix to associate with the image; must not be null.
     * @return a {@link ResponseEntity} containing the public URL of the uploaded image with HTTP status OK if successful;
     *         otherwise, a BAD_REQUEST or INTERNAL_SERVER_ERROR status with an error message.
     */
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Retrieves the image metadata for a given image ID.
     *
     * <p>This endpoint handles GET requests to "/api/images/{id}". It queries the {@link MatrixImageRepository}
     * to retrieve the {@link MatrixImage} entity corresponding to the provided ID. If the image is not found,
     * an error with HTTP status NOT_FOUND is returned.
     *
     * @param id the unique identifier of the image.
     * @return a {@link ResponseEntity} containing the {@link MatrixImage} and HTTP status OK if found;
     *         otherwise, an error message with HTTP status NOT_FOUND.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getImageById(@PathVariable Long id) {
        try {
            MatrixImage matrixImage = matrixImageRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Image not found with ID: " + id));
            return ResponseEntity.ok(matrixImage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Deletes an image from AWS S3 and removes its metadata from the database.
     *
     * <p>This endpoint handles DELETE requests to "/api/images/{id}". It first retrieves the {@link MatrixImage}
     * entity using the provided image ID. It then extracts the filename from the image URL and uses the AWS S3 client
     * to delete the file from the S3 bucket. Finally, it removes the image metadata from the database.
     * If any error occurs during these operations, an INTERNAL_SERVER_ERROR status is returned.
     *
     * @param id the unique identifier of the image to delete.
     * @return a {@link ResponseEntity} with HTTP status OK and a success message if deletion is successful;
     *         otherwise, an error message with HTTP status INTERNAL_SERVER_ERROR.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteImageById(@PathVariable Long id) {
        try {
            MatrixImage matrixImage = matrixImageRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Image not found with ID: " + id));

            String imageUrl = matrixImage.getImageUrl();
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

            // Delete the file from AWS S3.
            s3Client.deleteObject(b -> b.bucket(bucketName).key(fileName));
            System.out.println("Deleted image from S3: " + fileName);

            // Delete the image metadata from the database.
            matrixImageRepository.delete(matrixImage);
            System.out.println("Deleted image metadata from database: " + id);

            return ResponseEntity.ok("Image deleted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
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
