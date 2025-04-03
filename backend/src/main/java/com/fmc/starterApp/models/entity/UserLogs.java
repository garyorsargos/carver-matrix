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
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a log entry recording a user's login event.
 * 
 * <p>This entity is mapped to the "userLogs" table and stores critical information related to
 * user login events. Each log entry is associated with an {@link AppUser} and includes the login time.
 * 
 * <p><strong>Key Fields:</strong>
 * <ul>
 *   <li>{@code appUser} - The user who logged in (must not be null).</li>
 *   <li>{@code loginTime} - The timestamp when the login occurred.</li>
 *   <li>{@code id} - The primary key identifier for the log entry.</li>
 * </ul>
 * </p>
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "userLogs")
public class UserLogs {

    /**
     * The user associated with this login event.
     * <p>This field must not be null.
     */
    @NonNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", nullable = false)
    private AppUser appUser;

    /**
     * The timestamp when the login event occurred.
     * <p>This field is formatted in ISO date-time format.
     */
    @NonNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date loginTime;

    /**
     * The unique identifier for this log entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
