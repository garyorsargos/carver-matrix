package com.fmc.starterApp.services;

import com.fmc.starterApp.models.entity.CarverMatrix;
import com.fmc.starterApp.models.entity.CarverItem;
import com.fmc.starterApp.models.entity.MatrixImage;
import com.fmc.starterApp.repositories.CarverMatrixRepository;
import com.fmc.starterApp.repositories.CarverItemRepository;
import com.fmc.starterApp.repositories.MatrixImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

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

    public ImageService(MatrixImageRepository matrixImageRepository, CarverMatrixRepository carverMatrixRepository, S3Client s3Client) {
        this.matrixImageRepository = matrixImageRepository;
        this.carverMatrixRepository = carverMatrixRepository;
        this.s3Client = s3Client;
    }
    
    public String uploadImage(MultipartFile file, Long matrixId, Long itemId) throws IOException {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new IllegalArgumentException("Invalid file name.");
        }
        String removedInvalidCharacters = originalFileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

        String fileName = System.currentTimeMillis() + "_" + removedInvalidCharacters;

        //Upload to S3
        s3Client.putObject(PutObjectRequest.builder().bucket(bucketName).key(fileName).contentType(file.getContentType()).build(),RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        String fileUrl = String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);

        //Fetch CarverMatrix
        CarverMatrix carverMatrix = carverMatrixRepository.findById(matrixId).orElseThrow(() -> new IllegalArgumentException("Invalid matrixId: " + matrixId));

        MatrixImage matrixImage = new MatrixImage();
        matrixImage.setImageUrl(fileUrl);
        matrixImage.setUploadedAt(LocalDateTime.now());
        matrixImage.setCarverMatrix(carverMatrix);

        if (itemId != null) {
            CarverItem carverItem = carverItemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Invalid itemId: " + itemId));
            matrixImage.setCarverItem(carverItem);
        }

        matrixImageRepository.save(matrixImage);
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

        //Save metadata
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
