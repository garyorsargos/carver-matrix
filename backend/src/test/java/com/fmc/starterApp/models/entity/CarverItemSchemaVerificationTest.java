package com.fmc.starterApp.models.entity;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration tests for verifying the database schema for the CarverItem entity.
 *
 * <p>This test class uses JdbcTemplate to query the H2 in-memory database (in PostgreSQL mode)
 * to ensure that the CarverItem entity mapping is correctly translated into the CARVER_ITEMS table.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class CarverItemSchemaVerificationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // =========================================================================
    // ✅ 1. Database Schema Verification Tests
    // =========================================================================

    // ---------- Table Creation Test ----------
    /**
     * Verify that the CARVER_ITEMS table exists in the PUBLIC schema.
     */
    @Test
    void testCarverItemsTableExists() {
        List<String> tableNames = jdbcTemplate.queryForList(
            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
            String.class
        );
        System.out.println("H2 Tables: " + tableNames);
        assertThat(tableNames).contains("CARVER_ITEMS");
    }

    // ---------- Column Mapping and Constraint Test ----------
    /**
     * Verify that the CARVER_ITEMS table contains the expected columns.
     */
    @Test
    void testColumnMappingForCarverItems() {
        List<String> columns = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'CARVER_ITEMS'",
            String.class
        );
        System.out.println("Columns in CARVER_ITEMS: " + columns);
        assertThat(columns).contains(
            "ITEM_ID", 
            "MATRIX_ID", 
            "ITEM_NAME", 
            "CRITICALITY", 
            "ACCESSIBILITY", 
            "RECOVERABILITY", 
            "VULNERABILITY", 
            "EFFECT", 
            "RECOGNIZABILITY", 
            "TARGET_USERS", 
            "CREATED_AT"
        );
    }
}
