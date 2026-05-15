package com.training.java817.module1.optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 1 – Optional")
class OptionalExamplesTest {

    private OptionalExamples ex;

    @BeforeEach
    void setUp() { ex = new OptionalExamples(); }

    // --- getEmployeeCity ---

    @Test
    @DisplayName("Before/After: city from full chain returns correct city")
    void getEmployeeCity_fullChain_returnsCity() {
        OptionalExamples.Employee employee = new OptionalExamples.Employee("E1",
                new OptionalExamples.Department("D1", "Engineering",
                        new OptionalExamples.Address("10 Tech Park", "Bangalore")));
        assertEquals("Bangalore", ex.getEmployeeCity_Before(employee));
        assertEquals("Bangalore", ex.getEmployeeCity_After(employee));
    }

    @Test
    @DisplayName("Before/After: null department returns UNKNOWN")
    void getEmployeeCity_nullDept_returnsUnknown() {
        OptionalExamples.Employee employee = new OptionalExamples.Employee("E2", null);
        assertEquals("UNKNOWN", ex.getEmployeeCity_Before(employee));
        assertEquals("UNKNOWN", ex.getEmployeeCity_After(employee));
    }

    @Test
    @DisplayName("Before/After: null employee returns UNKNOWN")
    void getEmployeeCity_nullEmployee_returnsUnknown() {
        assertEquals("UNKNOWN", ex.getEmployeeCity_Before(null));
        assertEquals("UNKNOWN", ex.getEmployeeCity_After(null));
    }

    @Test
    @DisplayName("Before/After: null address returns UNKNOWN")
    void getEmployeeCity_nullAddress_returnsUnknown() {
        OptionalExamples.Employee employee = new OptionalExamples.Employee("E3",
                new OptionalExamples.Department("D2", "HR", null));
        assertEquals("UNKNOWN", ex.getEmployeeCity_Before(employee));
        assertEquals("UNKNOWN", ex.getEmployeeCity_After(employee));
    }

    // --- findDepartmentName ---

    @Test
    @DisplayName("Before/After: known ID returns department name")
    void findDepartmentName_knownId_returnsName() {
        assertEquals("Engineering", ex.findDepartmentName_Before("D001"));
        assertEquals("Engineering", ex.findDepartmentName_After("D001"));
    }

    @Test
    @DisplayName("Before/After: unknown ID returns NOT_FOUND")
    void findDepartmentName_unknownId_returnsDefault() {
        assertEquals("NOT_FOUND", ex.findDepartmentName_Before("UNKNOWN"));
        assertEquals("NOT_FOUND", ex.findDepartmentName_After("UNKNOWN"));
    }

    // --- resolveName ---

    @Test
    @DisplayName("resolveName: null returns DEFAULT_NAME")
    void resolveName_null_returnsDefault() {
        assertEquals("DEFAULT_NAME", ex.resolveName(null));
    }

    @Test
    @DisplayName("resolveName: blank returns DEFAULT_NAME")
    void resolveName_blank_returnsDefault() {
        assertEquals("DEFAULT_NAME", ex.resolveName("   "));
    }

    @Test
    @DisplayName("resolveName: non-blank returns the name")
    void resolveName_nonBlank_returnsName() {
        assertEquals("Alice", ex.resolveName("Alice"));
    }

    // --- requireDepartment ---

    @Test
    @DisplayName("requireDepartment: known ID returns department")
    void requireDepartment_knownId_returnsDepartment() {
        assertNotNull(ex.requireDepartment("D001"));
    }

    @Test
    @DisplayName("requireDepartment: unknown ID throws IllegalArgumentException")
    void requireDepartment_unknownId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ex.requireDepartment("MISSING"));
    }

    // --- Optional chaining ---

    @Test
    @DisplayName("getDepartmentCity: returns city in Optional when present")
    void getDepartmentCity_presentCity_returnsOptional() {
        OptionalExamples.Employee employee = new OptionalExamples.Employee("E1",
                new OptionalExamples.Department("D1", "Engineering",
                        new OptionalExamples.Address("10 Tech Park", "Bangalore")));
        Optional<String> city = ex.getDepartmentCity(Optional.of(employee));
        assertTrue(city.isPresent());
        assertEquals("Bangalore", city.get());
    }

    @Test
    @DisplayName("getDepartmentCity: returns empty when department is null")
    void getDepartmentCity_nullDept_returnsEmpty() {
        OptionalExamples.Employee employee = new OptionalExamples.Employee("E2", null);
        assertTrue(ex.getDepartmentCity(Optional.of(employee)).isEmpty());
    }

    @Test
    @DisplayName("resolveDepartment: returns primary when present")
    void resolveDepartment_primaryPresent_returnsPrimary() {
        Optional<OptionalExamples.Department> result =
                ex.resolveDepartment("D001", "FALLBACK");
        assertTrue(result.isPresent());
        assertEquals("D001", result.get().id());
    }

    @Test
    @DisplayName("resolveDepartment: falls back to second when primary absent")
    void resolveDepartment_primaryAbsent_returnsFallback() {
        Optional<OptionalExamples.Department> result =
                ex.resolveDepartment("MISSING", "D001");
        assertTrue(result.isPresent());
        assertEquals("D001", result.get().id());
    }

    @Test
    @DisplayName("resolveDepartment: returns empty when both absent")
    void resolveDepartment_bothAbsent_returnsEmpty() {
        assertTrue(ex.resolveDepartment("MISSING", "ALSO_MISSING").isEmpty());
    }
}
