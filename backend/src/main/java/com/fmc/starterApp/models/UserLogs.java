package com.fmc.starterApp.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;



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
