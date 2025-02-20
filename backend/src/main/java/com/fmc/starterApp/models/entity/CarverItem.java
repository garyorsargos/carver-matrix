package com.fmc.starterApp.models.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    @JsonIgnore  // Ignore the field during serialization
    private CarverMatrix carverMatrix;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    private Integer criticality;

    private Integer accessibility;

    private Integer recoverability;

    private Integer vulnerability;

    private Integer effect;

    private Integer recognizability;

    @Column(name = "c_user")
    @JsonProperty("cUser")
    private String cUser;

    @Column(name = "a_user")
    @JsonProperty("aUser")
    private String aUser;

    @Column(name = "r_user")
    @JsonProperty("rUser")
    private String rUser;

    @Column(name = "v_user")
    @JsonProperty("vUser")
    private String vUser;

    @Column(name = "e_user")
    @JsonProperty("eUser")
    private String eUser;

    @Column(name = "r2_user")
    @JsonProperty("r2User")
    private String r2User;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();

    public void setCarverMatrix(CarverMatrix carverMatrix) {
        this.carverMatrix = carverMatrix;
    }

}
