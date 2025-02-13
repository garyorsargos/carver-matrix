package com.fmc.starterApp.models.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @OneToMany(mappedBy = "carverMatrix", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CarverItem> items;

    @Column(nullable = false)
    private double cMulti = 1.0;

    @Column(nullable = false)
    private double aMulti = 1.0;

    @Column(nullable = false)
    private double rMulti = 1.0;

    @Column(nullable = false)
    private double vMulti = 1.0;

    @Column(nullable = false)
    private double eMulti = 1.0;

    @Column(nullable = false)
    private double r2Multi = 1.0;

    private boolean randomAssignment;

    private boolean roleBased;

    private boolean fivePointScoring;

    public void addItem(CarverItem item) {
        items.add(item);
        item.setCarverMatrix(this);
    }

    public void removeItem(CarverItem item) {
        items.remove(item);
        item.setCarverMatrix(null);
    }
}
