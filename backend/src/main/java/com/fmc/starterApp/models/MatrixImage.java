package com.fmc.starterApp.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "matrix_images")
public class MatrixImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @ManyToOne
    @JoinColumn(name = "matrix_id", nullable = false)
    private CarverMatrix carverMatrix;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "uploaded_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
