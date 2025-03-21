package com.fmc.starterApp.models.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Represents a user within the application.
 *
 * <p>This entity is mapped to the "users2" table and is responsible for encapsulating user-related
 * business data. It stores critical information such as:
 * <ul>
 *   <li>{@code keycloakId} - The unique identifier linking this user record to a Keycloak account.</li>
 *   <li>{@code username} - A unique username for user identification (maximum 50 characters).</li>
 *   <li>{@code email} - The user's email address, which is validated to conform to a standard email format.</li>
 *   <li>Other profile information such as first name, last name, and full name.</li>
 * </ul>
 *
 * <p>Non-null constraints are enforced using Lombok's {@code @NonNull} annotation. Note that this
 * behavior applies when using setters or the all-arguments constructor: if a {@code null} value is
 * provided, a {@code NullPointerException} is thrown. <strong>However</strong>, using the no-arguments
 * constructor (i.e., {@code new User2()}) does not trigger these non-null checks, and the fields will
 * remain {@code null} until they are explicitly set.
 *
 * <p>The {@code createdAt} field is automatically initialized to the current timestamp when a new
 * instance is created.
 *
 * <p>This model primarily handles the basic representation of user data and is designed to be used
 * within both the business and persistence layers.
 *
 * @author
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users2")  // Changed the table name to "users2"
public class User2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @NonNull
    @Column(nullable = false, unique = true)
    private String keycloakId;

    @Column(length = 50)
    private String firstName;

    @Column(length = 50)
    private String lastName;

    @Column(length = 50)
    private String fullName;

    @NonNull
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NonNull
    @Email(message = "Invalid email address")
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();
}
