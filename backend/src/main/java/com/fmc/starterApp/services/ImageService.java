package com.fmc.starterApp.services;

import com.fmc.starterApp.models.entity.CarverMatrix;
import com.fmc.starterApp.models.entity.MatrixImage;
import com.fmc.starterApp.repositories.CarverMatrixRepository;
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

@Service
public class ImageService {
    

    @Autowired
    private S3Client s3Client;

    @Autowired
    private MatrixImageRepository matrixImageRepository;

    @Autowired
    private CarverMatrixRepository carverMatrixRepository;

    @Value("${AWS_S3_BUCKET_NAME}")
    private String bucketName;

    public ImageService(MatrixImageRepository matrixImageRepository, CarverMatrixRepository carverMatrixRepository, S3Client s3Client) {
        this.matrixImageRepository = matrixImageRepository;
        this.carverMatrixRepository = carverMatrixRepository;
        this.s3Client = s3Client;
    }
    
    public String uploadImage(MultipartFile file, Long matrixId) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        //Upload to S3
        s3Client.putObject(PutObjectRequest.builder().bucket(bucketName).key(fileName).build(),RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        String fileUrl = String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);

        //Fetch CarverMatrix
        CarverMatrix carverMatrix = carverMatrixRepository.findById(matrixId).orElseThrow(() -> new IllegalArgumentException("Invalid matrixId: " + matrixId));

        //Save metadata to MatrixImage
        MatrixImage matrixImage = new MatrixImage();
        matrixImage.setImageUrl(fileUrl);
        matrixImage.setUploadedAt(LocalDateTime.now());
        matrixImage.setCarverMatrix(carverMatrix);
        matrixImageRepository.save(matrixImage);

        return fileUrl;
    }
}
