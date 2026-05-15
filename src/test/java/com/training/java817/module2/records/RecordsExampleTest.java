package com.training.java817.module2.records;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 2 – Records (JEP 395)")
class RecordsExampleTest {

    private static final LocalDate TODAY = LocalDate.of(2024, 6, 1);

    private RecordsExample.EmployeeRecord sample() {
        return new RecordsExample.EmployeeRecord(
                "E001", "Alice", 2_000_000.0, "D01", TODAY, "ACTIVE");
    }

    // --- Auto-generated accessors ---

    @Test
    @DisplayName("Accessors: record accessors return correct values")
    void accessors_returnCorrectValues() {
        RecordsExample.EmployeeRecord r = sample();
        assertEquals("E001",       r.id());
        assertEquals("Alice",      r.name());
        assertEquals(2_000_000.0,  r.salary());
        assertEquals("D01",        r.departmentId());
        assertEquals(TODAY,        r.hireDate());
        assertEquals("ACTIVE",     r.status());
    }

    // --- Auto-generated equals/hashCode ---

    @Test
    @DisplayName("Equality: two records with same data are equal")
    void equality_sameData_areEqual() {
        assertEquals(sample(), sample());
    }

    @Test
    @DisplayName("Equality: records with different salary are not equal")
    void equality_differentSalary_notEqual() {
        RecordsExample.EmployeeRecord r2 = new RecordsExample.EmployeeRecord(
                "E001", "Alice", 3_000_000.0, "D01", TODAY, "ACTIVE");
        assertNotEquals(sample(), r2);
    }

    @Test
    @DisplayName("HashCode: equal records have same hash code")
    void hashCode_equalRecords_sameHash() {
        assertEquals(sample().hashCode(), sample().hashCode());
    }

    // --- Auto-generated toString ---

    @Test
    @DisplayName("toString: contains field values")
    void toString_containsFieldValues() {
        String str = sample().toString();
        assertTrue(str.contains("E001"));
        assertTrue(str.contains("Alice"));
        assertTrue(str.contains("2000000.0"));
    }

    // --- Compact constructor validation ---

    @Test
    @DisplayName("Compact constructor: null id throws NullPointerException")
    void compactConstructor_nullId_throws() {
        assertThrows(NullPointerException.class, () ->
                new RecordsExample.EmployeeRecord(
                        null, "Alice", 50_000.0, "D01", TODAY, "ACTIVE"));
    }

    @Test
    @DisplayName("Compact constructor: negative salary throws IllegalArgumentException")
    void compactConstructor_negativeSalary_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new RecordsExample.EmployeeRecord(
                        "E001", "Alice", -1.0, "D01", TODAY, "ACTIVE"));
    }

    // --- Custom method ---

    @Test
    @DisplayName("isHighSalary: returns true above 100K")
    void isHighSalary_aboveThreshold_returnsTrue() {
        assertTrue(sample().isHighSalary());
    }

    @Test
    @DisplayName("isHighSalary: returns false at or below 100K")
    void isHighSalary_belowThreshold_returnsFalse() {
        RecordsExample.EmployeeRecord r = new RecordsExample.EmployeeRecord(
                "E002", "Bob", 50_000.0, "D02", TODAY, "ACTIVE");
        assertFalse(r.isHighSalary());
    }

    // --- Static factory ---

    @Test
    @DisplayName("pending factory: sets status to ONBOARDING and hireDate to today")
    void pendingFactory_setsCorrectDefaults() {
        RecordsExample.EmployeeRecord pending =
                RecordsExample.EmployeeRecord.pending("E003", "Charlie", 80_000, "D03");
        assertEquals("ONBOARDING", pending.status());
        assertEquals(LocalDate.now(), pending.hireDate());
    }

    // --- POJO comparison (same logical data) ---

    @Test
    @DisplayName("POJO and record contain the same data for the same inputs")
    void pojoAndRecord_containSameData() {
        RecordsExample.EmployeePojo pojo = new RecordsExample.EmployeePojo(
                "E001", "Alice", 2_000_000.0, "D01", TODAY, "ACTIVE");
        RecordsExample.EmployeeRecord record = sample();

        assertEquals(pojo.getId(),           record.id());
        assertEquals(pojo.getName(),         record.name());
        assertEquals(pojo.getSalary(),       record.salary());
        assertEquals(pojo.getDepartmentId(), record.departmentId());
        assertEquals(pojo.getStatus(),       record.status());
    }

    // --- AuditedEmployee implements interface ---

    @Test
    @DisplayName("AuditedEmployee: auditSummary contains employeeId, action, and performer")
    void auditedEmployee_summaryContainsDetails() {
        RecordsExample.AuditedEmployee audit =
                new RecordsExample.AuditedEmployee("E001", "PROMOTE", "john.doe");
        String summary = audit.auditSummary();
        assertTrue(summary.contains("E001"));
        assertTrue(summary.contains("PROMOTE"));
        assertTrue(summary.contains("john.doe"));
    }
}
