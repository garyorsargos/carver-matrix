# Entity Unit Testing Checklist

This checklist covers the essential tests for your entity classes. It follows the same structure as our test class for consistency and ease of copy-paste.

---

```java
// =========================================================================
// ✅ 1. Basic Instantiation and Field Tests
// =========================================================================
```

### // ---------- Instantiation Tests ----------

- [ ] **No-Args Constructor Test**  
       Verify that the entity can be instantiated using the no-args constructor and that initial field values (if any) are as expected.
- [ ] **All-Args Constructor Test**  
       Verify that the entity can be instantiated using the all-args constructor and that all fields are set correctly.
- [ ] **Getter/Setter Tests**  
       Verify that getters and setters work correctly by setting values and then retrieving them.

---

```java
// =========================================================================
// ✅ 2. Constraint Validation Tests
// =========================================================================
```

### // ---------- Non-null and Format Validation ----------

- [ ] **Non-null Constraint Test**  
       Verify that attempting to set a required field (annotated with `@NonNull`) to `null` via the setter or all-args constructor results in a `NullPointerException` (if applicable).
- [ ] **Email Format Validation Test**  
       Verify that the `email` field conforms to a proper email format (this may require invoking a Bean Validator).
- [ ] **Length Constraint Test**  
       Confirm that string fields (e.g., `username`, `firstName`, `lastName`) respect the maximum lengths defined in the entity.

---

```java
// =========================================================================
// ✅ 3. Default Values and Auditing Tests
// =========================================================================
```

- [ ] **Default Value Test for `createdAt`**  
       Verify that the `createdAt` field is automatically populated upon entity instantiation if not explicitly set.

---

```java
// =========================================================================
// ✅ 4. Equality and toString Tests
// =========================================================================
```

- [ ] **Equals and HashCode Test**  
       Verify that the `equals()` and `hashCode()` methods work as expected (if overridden).
- [ ] **toString Method Test**  
       Optionally, verify that the `toString()` method returns a meaningful representation of the entity.

---

```java
// =========================================================================
// ✅ 5. Edge Case Tests
// =========================================================================
```

- [ ] **Empty Strings Test**  
       Verify that fields can correctly handle empty string values where allowed.
- [ ] **Special Characters and Unicode Test**  
       Confirm that the entity handles special and Unicode characters correctly.
- [ ] **Boundary Value Test for Length**  
       Test values that are exactly at the allowed boundary lengths.

---

## Summary (Quick Reference)

| Category                                | Test Examples                                          |
| --------------------------------------- | ------------------------------------------------------ |
| **Basic Instantiation and Field Tests** | No-args/all-args constructors, getters/setters         |
| **Constraint Validation Tests**         | Non-null constraints, email format, length constraints |
| **Default Values and Auditing Tests**   | `createdAt` default value                              |
| **Equality and toString Tests**         | `equals()`, `hashCode()`, `toString()`                 |
| **Edge Case Tests**                     | Empty strings, special characters, boundary values     |

---
