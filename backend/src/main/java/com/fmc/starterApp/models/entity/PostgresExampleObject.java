package com.fmc.starterApp.models.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents an example object stored in a PostgreSQL database.
 *
 * <p>This entity is mapped to the "example" table and serves as a simple model
 * containing an auto-generated unique identifier and a name field.
 *
 * <p><strong>Key Fields:</strong>
 * <ul>
 *   <li>{@code id} - The unique identifier for the object, automatically generated.</li>
 *   <li>{@code name} - A descriptive name for the object. This field is mandatory and is restricted to a maximum length of 100 characters.</li>
 * </ul>
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "example")
@ToString
public class PostgresExampleObject {

    /**
     * The unique identifier for the object.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name associated with this object.
     *
     * <p>This field is mandatory and has a maximum length of 100 characters.
     * </p>
     */
    @NonNull
    @Column(nullable = false, length = 100)
    private String name;
}
