package com.fmc.starterApp.models.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration tests for verifying the database schema for the MatrixImage entity.
 *
 * <p>This test class uses JdbcTemplate to query the H2 in-memory database (in PostgreSQL mode)
 * to ensure that the MatrixImage entity mapping is correctly translated into the MATRIX_IMAGES table.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class MatrixImageSchemaVerificationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // =========================================================================
    // ✅ 1. Database Schema Verification Tests
    // =========================================================================

    // ---------- Table Creation Test ----------
    /**
     * Verify that the MATRIX_IMAGES table exists in the PUBLIC schema.
     */
    @Test
    void testMatrixImagesTableExists() {
        List<String> tableNames = jdbcTemplate.queryForList(
            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
            String.class
        );
        System.out.println("H2 Tables: " + tableNames);
        // In H2, unquoted table names are stored in uppercase.
        assertThat(tableNames).contains("MATRIX_IMAGES");
    }

    // ---------- Column Mapping and Constraint Test ----------
    /**
     * Verify that the MATRIX_IMAGES table contains the expected columns.
     */
    @Test
    void testColumnMappingForMatrixImages() {
        List<String> columns = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'MATRIX_IMAGES'",
            String.class
        );
        System.out.println("Columns in MATRIX_IMAGES: " + columns);
        // The expected columns based on the entity mapping are:
        // IMAGE_ID, MATRIX_ID, IMAGE_URL, and UPLOADED_AT.
        assertThat(columns).contains("IMAGE_ID", "MATRIX_ID", "IMAGE_URL", "UPLOADED_AT");
    }
}
