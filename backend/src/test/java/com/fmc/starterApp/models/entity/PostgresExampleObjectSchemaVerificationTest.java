package com.fmc.starterApp.models.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration tests for verifying the database schema for the PostgresExampleObject entity.
 *
 * <p>This test class uses JdbcTemplate to query the H2 in-memory database (in PostgreSQL mode)
 * to ensure that the PostgresExampleObject entity mapping is correctly translated into the EXAMPLE table.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class PostgresExampleObjectSchemaVerificationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // =========================================================================
    // ✅ 1. Database Schema Verification Tests
    // =========================================================================

    // ---------- Table Creation Test ----------
    /**
     * Verify that the EXAMPLE table exists in the PUBLIC schema.
     */
    @Test
    void testExampleTableExists() {
        List<String> tableNames = jdbcTemplate.queryForList(
            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
            String.class
        );
        System.out.println("H2 Tables: " + tableNames);
        // In H2, unquoted table names are stored in uppercase.
        assertThat(tableNames).contains("EXAMPLE");
    }

    // ---------- Column Mapping and Constraint Test ----------
    /**
     * Verify that the EXAMPLE table contains the expected columns.
     */
    @Test
    void testColumnMappingForExample() {
        List<String> columns = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'EXAMPLE'",
            String.class
        );
        System.out.println("Columns in EXAMPLE: " + columns);
        // The expected columns based on the entity mapping are ID and NAME.
        assertThat(columns).contains("ID", "NAME");
    }
}
