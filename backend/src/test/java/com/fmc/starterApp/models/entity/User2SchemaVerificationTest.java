package com.fmc.starterApp.models.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration tests for verifying the database schema for the User2 entity.
 *
 * <p>This test class uses JdbcTemplate to query the H2 in-memory database (in PostgreSQL mode)
 * to ensure that the User2 entity mapping is correctly translated into the USERS2 table.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class User2SchemaVerificationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // =========================================================================
    // ✅ 1. Database Schema Verification Tests
    // =========================================================================

    // ---------- Table Creation Test ----------
    /**
     * Verify that the USERS2 table exists in the PUBLIC schema.
     */
    @Test
    void testUser2TableExists() {
        List<String> tableNames = jdbcTemplate.queryForList(
            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
            String.class
        );
        System.out.println("H2 Tables: " + tableNames);
        // In H2, unquoted table names are stored in uppercase.
        assertThat(tableNames).contains("USERS2");
    }

    // ---------- Column Mapping and Constraint Test ----------
    /**
     * Verify that the USERS2 table contains the expected columns.
     */
    @Test
    void testColumnMappingForUser2() {
        List<String> columns = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'USERS2'",
            String.class
        );
        System.out.println("Columns in USERS2: " + columns);
        // Based on the entity mapping and naming strategy, the expected columns are:
        // USER_ID, KEYCLOAK_ID, FIRST_NAME, LAST_NAME, FULL_NAME, USERNAME, EMAIL, and CREATED_AT.
        assertThat(columns).contains(
            "USER_ID", 
            "KEYCLOAK_ID", 
            "FIRST_NAME", 
            "LAST_NAME", 
            "FULL_NAME", 
            "USERNAME", 
            "EMAIL", 
            "CREATED_AT"
        );
    }
}
