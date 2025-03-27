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
 * Integration tests for verifying the database schema for the UserLogs entity.
 *
 * <p>This test class uses JdbcTemplate to query the H2 in‐memory database (configured in PostgreSQL mode)
 * to ensure that the UserLogs entity mapping is correctly translated into a database table
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class UserLogsSchemaVerificationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // =========================================================================
    // ✅ 1. Database Schema Verification Tests
    // =========================================================================

    // ---------- Table Creation Test ----------
    /**
     * **Verify Table Creation**  
     * Query the in-memory H2 database to list all table names in the PUBLIC schema and assert that
     * the expected table for UserLogs is present.
     */
    @Test
    void testListH2Tables() {
        List<String> tableNames = jdbcTemplate.queryForList(
            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC'",
            String.class
        );
        System.out.println("H2 Tables: " + tableNames);
        // In H2 (PostgreSQL mode) unquoted table names are stored in uppercase.
        assertThat(tableNames).contains("USER_LOGS");
    }

    // ---------- Column Mapping and Constraint Test ----------
    /**
     * **Verify Column Mappings for USERLOGS**  
     * Query the INFORMATION_SCHEMA.COLUMNS for the USERLOGS table and assert that
     * critical columns exist with the expected names.
     */
    @Test
    void testColumnMappingForUserLogs() {
        List<String> columns = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_SCHEMA='PUBLIC' AND TABLE_NAME='USER_LOGS'",
            String.class
        );
        System.out.println("Columns in USERLOGS: " + columns);
        // Verify that essential columns are present (columns will be in uppercase).
        assertThat(columns).contains("USER_ID", "LOGIN_TIME", "ID");
    }
}
