# Repository Testing Checklist

This checklist covers the essential repository tests. It follows the same structure as our test class for easy copy-paste and consistency.

---

```java
// =========================================================================
// ✅ 1. Basic CRUD Tests
// =========================================================================
```

### // ---------- Create Operation Tests ----------

- [ ] **Save new entity**  
       Verify that a new entity is correctly persisted.

### // ---------- Read Operation Tests ----------

- [ ] **Find by ID (positive case)**  
       Confirm retrieval by primary key returns the correct entity.
- [ ] **Find by ID (negative case)**  
       Confirm that querying a non-existent ID returns an empty result (e.g., `Optional.empty()`).
- [ ] **Find by unique field (positive case)**  
       Verify that querying by a unique field (e.g., `findByKeycloakId`) returns the correct entity.
- [ ] **Find by unique field (negative case)**  
       Verify that querying with a non-existent unique field value returns empty.
- [ ] **Find by unique field (negative case with null)**  
       Ensure that querying with a null unique field yields an empty result.

### // ---------- Update Operation Tests ----------

- [ ] **Update existing entity**  
       Verify that changes to an entity are correctly persisted.
- [ ] **Update non-existent entity**  
       Confirm that attempting to update an entity that doesn't exist returns an appropriate result or error.

### // ---------- Delete Operation Tests ----------

- [ ] **Delete existing entity**  
       Confirm that an entity is removed from the repository after deletion.
- [ ] **Delete non-existent entity**  
       Confirm that attempting to delete a non-persisted entity does not throw unexpected exceptions.

---

```java
// =========================================================================
// ✅ 2. Constraint Validation Tests
// =========================================================================
```

- [ ] **Unique Constraint Violation**  
       Ensure that inserting duplicate values for unique fields throws a `DataIntegrityViolationException`.
- [ ] **Non-null Constraint Violation**  
       Verify that null values for required fields are rejected.
- [ ] **Length Constraint Violation**  
       Confirm that values exceeding defined length limits are not persisted.

---

```java
// =========================================================================
// ✅ 3. Query Method Tests (including Custom Queries)
// =========================================================================
```

- [ ] **Custom Query Positive Case**  
       Verify that a custom query (e.g., one using `@Query`) returns the expected results when provided valid input.
- [ ] **Custom Query Negative Case**  
       Confirm that a custom query returns an empty result when given non-existent input.
- [ ] **Custom Query Null Parameter Handling**  
       Verify the behavior when null is passed as a parameter (if applicable).

---

```java
// =========================================================================
// ✅ 4. Bulk Operations Tests
// =========================================================================
```

- [ ] **SaveAll Operation**  
       Validate that multiple entities can be inserted in bulk.
- [ ] **DeleteAll Operation**  
       Ensure that bulk deletions remove entities as expected.
- [ ] **Bulk Operations Edge Cases**  
       Verify that calling bulk operations with an empty list behaves as expected (no exception, no changes).

---

```java
// =========================================================================
// ✅ 5. Transactional Tests
// =========================================================================
```

- [ ] **Transactional Rollback**  
       Simulate an exception within a transaction to verify that the repository rolls back the transaction, leaving the database state unchanged.
- [ ] **Update Rollback Verification**  
       Confirm that after a rollback, no partial updates persist in the database.

---

```java
// =========================================================================
// ✅ 6. Pagination and Sorting Tests
// =========================================================================
```

- [ ] **Pagination**  
       Validate that pagination correctly limits and offsets results.
- [ ] **Sorting (Ascending and Descending)**  
       Confirm that sorting returns results in the expected order (e.g., by username) for both ascending and descending directions.
- [ ] **Sorting on Alternate Fields**  
       Verify sorting behavior when sorting on fields other than the primary one.

---

```java
// =========================================================================
// ✅ 7. Auditing and Optional Field Tests
// =========================================================================
```

- [ ] **Creation Timestamp**  
       Verify that fields like `createdAt` are automatically populated upon entity creation.
- [ ] **Optional Field Handling**  
       Verify that entities handle optional fields (set to null) correctly without causing persistence issues.

---

```java
// =========================================================================
// ✅ 8. Edge Case and Exception Handling Tests
// =========================================================================
```

### // ---------- Edge Case Tests ----------

- [ ] **Max-length Strings**  
       Confirm that string fields respect maximum allowed lengths.
- [ ] **Empty Strings**  
       Validate that empty string values are handled correctly.
- [ ] **Special Characters and Unicode**  
       Confirm that special and Unicode characters are persisted correctly.
- [ ] **Boundary Value Test for Length**  
       Test values that are exactly at the allowed boundary lengths.

### // ---------- Exception Handling Tests ----------

- [ ] **Save Null Entity Exception Test**  
       Verify that passing a null entity to the save method throws an `InvalidDataAccessApiUsageException`.
- [ ] **Find by ID with Null Exception Test**  
       Verify that passing a null ID to `findById` throws an `InvalidDataAccessApiUsageException`.
- [ ] **Invalid Pagination Parameters**  
       Verify that negative pagination parameters throw an `IllegalArgumentException`.

---

## Summary (Quick Reference)

| Category                            | Test Examples                                               |
| ----------------------------------- | ----------------------------------------------------------- |
| **Basic CRUD Tests**                | Create, Read (ID & unique field), Update, Delete            |
| **Constraint Validation Tests**     | Unique, Non-null, Length                                    |
| **Query Method Tests**              | Derived and Custom queries                                  |
| **Bulk Operations Tests**           | saveAll, deleteAll, and handling empty lists                |
| **Transactional Tests**             | Rollback scenarios, verifying database state                |
| **Pagination/Sorting Tests**        | PageRequest, Sort (ascending, descending, alternate fields) |
| **Auditing & Optional Fields**      | Creation timestamps, null handling                          |
| **Edge & Exception Handling Tests** | Max length, empty strings, special characters, exceptions   |

---
