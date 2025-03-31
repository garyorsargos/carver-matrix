package com.fmc.starterApp.models.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration tests for verifying the database schema.
 *
 * <p>This test class uses JdbcTemplate to query the H2 in‐memory database (in PostgreSQL mode)
 * to ensure that the entity mappings are correctly translated into database tables
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class CarverMatrixSchemaVerificationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // =========================================================================
    // ✅ 1. Database Schema Verification Tests
    // =========================================================================

    // ---------- Table Creation Test ----------
    /**
     * **Verify Table Creation**  
     * Query the in-memory H2 database to list all table names in the public schema
     * and assert that the expected tables are present.
     */
    @Test
    void testListH2Tables() {
        List<String> tableNames = jdbcTemplate.queryForList(
            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC'",
            String.class
        );
        System.out.println("H2 Tables: " + tableNames);
        // In H2, unquoted table names are stored in uppercase.
        assertThat(tableNames).contains("CARVER_MATRICES", "USERS2", "CARVER_ITEMS", "MATRIX_IMAGES");
    }

    // ---------- Column Mapping and Constraint Test ----------
    /**
     * **Verify Column Mappings**  
     * Query the INFORMATION_SCHEMA.COLUMNS for CARVER_MATRICES and assert that
     * critical columns exist with the expected properties.
     */
    @Test
    void testColumnMappingForCarverMatrices() {
        List<String> columns = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'CARVER_MATRICES'",
            String.class
        );
        System.out.println("Columns in CARVER_MATRICES: " + columns);
        assertThat(columns).contains("MATRIX_ID", "NAME", "DESCRIPTION", "CREATED_AT", "HOSTS", "PARTICIPANTS");
    }
}
