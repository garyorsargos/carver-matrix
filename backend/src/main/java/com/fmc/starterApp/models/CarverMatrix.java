package com.fmc.starterApp.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "carver_matrices")
public class CarverMatrix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long matrixId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User2 user;  // Updated to reference User2 entity

    private String name;

    private String description;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT[]")
    private String[] hosts;

    @Column(columnDefinition = "TEXT[]")
    private String[] participants;
}
