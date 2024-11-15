package com.fmc.starterApp.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "carver_items")
public class CarverItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @ManyToOne
    @JoinColumn(name = "matrix_id", nullable = false)
    private CarverMatrix carverMatrix;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    private Integer criticality;

    private Integer accessibility;

    private Integer recoverability;

    private Integer vulnerability;

    private Integer effect;

    private Integer recognizability;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();
}
