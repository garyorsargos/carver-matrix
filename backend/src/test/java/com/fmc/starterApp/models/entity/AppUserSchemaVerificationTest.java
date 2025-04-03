package com.fmc.starterApp.models.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration tests for verifying the database schema for the AppUser entity.
 *
 * <p>This test class uses JdbcTemplate to query the H2 in‐memory database (in PostgreSQL mode)
 * to ensure that the AppUser entity mapping is correctly translated into the database table
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class AppUserSchemaVerificationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // =========================================================================
    // ✅ 1. Database Schema Verification Tests
    // =========================================================================

    // ---------- Table Creation Test ----------
    /**
     * **Verify Table Creation**  
     * Query the in-memory H2 database to list all table names in the public schema and assert that
     * the "USERS" table is present.
     */
    @Test
    void testListH2Tables() {
        List<String> tableNames = jdbcTemplate.queryForList(
            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC'",
            String.class
        );
        System.out.println("H2 Tables: " + tableNames);
        // In H2 (PostgreSQL mode), unquoted table names are stored in uppercase.
        assertThat(tableNames).contains("USERS");
    }

    // ---------- Column Mapping and Constraint Test ----------
    /**
     * **Verify Column Mappings for USERS**  
     * Query INFORMATION_SCHEMA.COLUMNS for the USERS table and assert that
     * critical columns exist with the expected names.
     */
    @Test
    void testColumnMappingForUsers() {
        List<String> columns = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_SCHEMA='PUBLIC' AND TABLE_NAME='USERS'",
            String.class
        );
        System.out.println("Columns in USERS: " + columns);
        // Verify that essential columns are present.
        assertThat(columns).contains("USER_ID", "USER_NAME", "EMAIL");
    }

    // ---------- Foreign Key and Index Test ----------
    /**
     * **Verify Foreign Keys in USERS**  
     * Since AppUser does not define foreign keys (it is the parent side of a one-to-many),
     * this test asserts that no referential constraints are present on the USERS table.
     */
    @Test
    void testNoForeignKeyInUsers() {
        List<String> fkConstraints = jdbcTemplate.queryForList(
            "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS " +
            "WHERE TABLE_SCHEMA='PUBLIC' AND TABLE_NAME='USERS' " +
            "AND CONSTRAINT_TYPE='REFERENTIAL'",
            String.class
        );
        System.out.println("Foreign Keys in USERS: " + fkConstraints);
        // We expect no foreign key constraints on the USERS table.
        assertTrue(fkConstraints.isEmpty(), "Expected no foreign key constraints in USERS table.");
    }
}
