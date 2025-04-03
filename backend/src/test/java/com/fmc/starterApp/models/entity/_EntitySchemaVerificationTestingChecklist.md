# Repository Schema Verification Checklist

This section covers tests to confirm that your entity mappings are correctly translated into database tables and that the schema is as expected. These tests help ensure that the integrated environment is properly setting up the database structure.

---

```java
// =========================================================================
// ✅ 1. Database Schema Verification Tests
// =========================================================================
```

### // ---------- Table Creation Test ----------

- [ ] **Verify Table Creation**  
       Query the in-memory database (e.g., H2) using JdbcTemplate to list all table names in the public schema and verify that all expected tables are present (e.g., `"CARVER_MATRICES"`, `"USERS2"`, `"CARVER_ITEMS"`, `"MATRIX_IMAGES"`).

### // ---------- Column Mapping and Constraint Test ----------

- [ ] **Verify Column Mappings**  
       Optionally, query the INFORMATION_SCHEMA to ensure that specific columns exist with the correct data types and constraints (such as length restrictions or default values).

---

## Summary (Quick Reference)

| Category                | Test Examples                                                                              |
| ----------------------- | ------------------------------------------------------------------------------------------ |
| **Table Creation Test** | Verify that all expected tables (e.g., CARVER_MATRICES, USERS2, etc.) exist in the schema. |
| **Column Mapping Test** | Verify columns, data types, length constraints, and default values.                        |

---

This section will help you ensure that your entities are being translated into the correct schema before you run further integration and functional tests.
