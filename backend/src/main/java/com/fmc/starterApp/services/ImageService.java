package com.fmc.starterApp.services;

import com.fmc.starterApp.models.entity.CarverMatrix;
import com.fmc.starterApp.models.entity.CarverItem;
import com.fmc.starterApp.models.entity.MatrixImage;
import com.fmc.starterApp.repositories.CarverMatrixRepository;
import com.fmc.starterApp.repositories.CarverItemRepository;
import com.fmc.starterApp.repositories.MatrixImageRepository;
import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fmc.starterApp.models.entity.CarverMatrix;
import com.fmc.starterApp.models.entity.MatrixImage;
import com.fmc.starterApp.repositories.CarverMatrixRepository;
import com.fmc.starterApp.repositories.MatrixImageRepository;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;


/**
 * Service class for managing image uploads associated with Carver Matrices.
 *
 * <p>This service provides methods to upload an image file to an AWS S3 bucket and to persist its metadata
 * in the database, associating the image with a specific {@link CarverMatrix}. It performs input validation,
 * error handling, and returns the public URL of the uploaded image.
 *
 * <p><strong>Key Methods:</strong>
 * <ul>
 *   <li>{@link #uploadImage(MultipartFile, Long)}: Uploads an image file, stores its metadata, and returns its URL.</li>
 * </ul>
 */
@Service
public class ImageService {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private MatrixImageRepository matrixImageRepository;

    @Autowired
    private CarverMatrixRepository carverMatrixRepository;

    @Autowired
    private CarverItemRepository carverItemRepository;

    @Value("${AWS_S3_BUCKET_NAME}")
    private String bucketName;

    /**
     * Constructs an ImageService with the specified dependencies.
     *
     * @param matrixImageRepository the repository for managing {@link MatrixImage} entities; must not be null.
     * @param carverMatrixRepository the repository for managing {@link CarverMatrix} entities; must not be null.
     * @param s3Client the AWS S3 client used for file uploads; must not be null.
     */
    public ImageService(MatrixImageRepository matrixImageRepository, CarverMatrixRepository carverMatrixRepository, S3Client s3Client) {
        this.matrixImageRepository = matrixImageRepository;
        this.carverMatrixRepository = carverMatrixRepository;
        this.s3Client = s3Client;
    }

    /**
     * Uploads an image file to AWS S3 and persists its metadata in the database.
     *
     * <p>This method performs the following steps:
     * <ol>
     *   <li>Validates the provided {@link MultipartFile} and ensures its original filename is not null or empty.</li>
     *   <li>Cleans the filename by replacing invalid characters with underscores and generates a unique filename.</li>
     *   <li>Uploads the file to the configured S3 bucket.</li>
     *   <li>Constructs the public URL for the uploaded file.</li>
     *   <li>Retrieves the associated {@link CarverMatrix} using the provided matrixId.</li>
     *   <li>Creates and saves a new {@link MatrixImage} entity with the image URL and current timestamp.</li>
     * </ol>
     *
     * @param file the image file to upload; must not be null.
     * @param matrixId the ID of the {@link CarverMatrix} to associate with the image; must not be null.
     * @return the public URL of the uploaded image.
     * @throws IllegalArgumentException if the file or matrixId is null, or if the file's original filename is invalid.
     * @throws IOException if an error occurs while reading the file's input stream.
     * @throws RuntimeException if the S3 upload or database operation fails.
     */
    public String uploadImage(MultipartFile file, Long matrixId, Long itemId) throws IOException {
        // Validate input file and matrixId.
        if (file == null) {
            throw new IllegalArgumentException("MultipartFile must not be null");
        }
        if (matrixId == null) {
            throw new IllegalArgumentException("MatrixId must not be null");
        }

        // Validate and clean the original file name.
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new IllegalArgumentException("Invalid file name.");
        }
        String cleanedFileName = originalFileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

        // Generate a unique filename.
        String fileName = System.currentTimeMillis() + "_" + cleanedFileName;

        try {
            // Upload the file to S3.
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (Exception e) {
            // Wrap and propagate S3 upload errors.
            throw new RuntimeException("Failed to upload file to S3", e);
        }

        // Build the public URL for the uploaded file.
        String fileUrl = String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);

        // Retrieve the CarverMatrix; if not found, throw an exception.
        CarverMatrix carverMatrix = carverMatrixRepository.findById(matrixId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid matrixId: " + matrixId));

        // Create a new MatrixImage object to store metadata.
        MatrixImage matrixImage = new MatrixImage();
        matrixImage.setImageUrl(fileUrl);
        matrixImage.setUploadedAt(LocalDateTime.now());
        matrixImage.setCarverMatrix(carverMatrix);

        if (itemId != null) {
            CarverItem carverItem = carverItemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Invalid itemId: " + itemId));
            matrixImage.setCarverItem(carverItem);
        }

        try {
            // Save the MatrixImage metadata to the database.
            matrixImageRepository.save(matrixImage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to persist image metadata in the database", e);
        }
        return fileUrl;
    }
    
    public void uploadBase64Image(String base64String, Long matrixId, Long itemId) throws IOException {
        String[] parts = base64String.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid base64 image format.");
        }

        String metadata = parts[0];
        String base64Data = parts[1];

        String extension = "png";
        if (metadata.contains("image/jpeg")) {
            extension = "jpg";
        } else if (metadata.contains("image/gif")) {
            extension = "gif";
        }

        byte[] imageBytes = Base64.getDecoder().decode(base64Data);
        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + "." + extension;

        //Upload to S3
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType("image/" + extension)
                .build(),
            RequestBody.fromBytes(imageBytes)
        );

        String fileUrl = String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);

        CarverMatrix matrix = carverMatrixRepository.findById(matrixId).orElseThrow(() -> new IllegalArgumentException("Invalid matrixId: " + matrixId));

        CarverItem item = carverItemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Invalid itemId: " + itemId));

        MatrixImage matrixImage = new MatrixImage();
        matrixImage.setImageUrl(fileUrl);
        matrixImage.setUploadedAt(LocalDateTime.now());
        matrixImage.setCarverMatrix(matrix);
        matrixImage.setCarverItem(item);

        matrixImageRepository.save(matrixImage);
    }
}
