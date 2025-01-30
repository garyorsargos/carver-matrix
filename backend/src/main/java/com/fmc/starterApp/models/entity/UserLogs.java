package com.fmc.starterApp.models.entity;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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



@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "userLogs")
public class UserLogs {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="userId", nullable=false)
    private AppUser appUser;

    @Getter
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date loginTime;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}
