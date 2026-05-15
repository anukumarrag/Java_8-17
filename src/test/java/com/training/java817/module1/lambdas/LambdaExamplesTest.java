package com.training.java817.module1.lambdas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 1 – Lambda Expressions")
class LambdaExamplesTest {

    private LambdaExamples ex;

    @BeforeEach
    void setUp() { ex = new LambdaExamples(); }

    @Test
    @DisplayName("Before: anonymous Comparator sorts correctly")
    void sortEmployees_Before_shouldSortAlphabetically() {
        List<String> sorted = ex.sortEmployees_Before(Arrays.asList("EMP-003", "EMP-001", "EMP-002"));
        assertEquals(List.of("EMP-001", "EMP-002", "EMP-003"), sorted);
    }

    @Test
    @DisplayName("After: lambda Comparator produces same result as before")
    void sortEmployees_After_shouldMatchBefore() {
        List<String> input  = Arrays.asList("EMP-003", "EMP-001", "EMP-002");
        assertEquals(ex.sortEmployees_Before(input), ex.sortEmployees_After(input));
    }

    @Test
    @DisplayName("Before: loop-based filter keeps ACTIVE entries only")
    void filterActiveEmployees_Before_keepsActiveOnly() {
        List<String> result = ex.filterActiveEmployees_Before(
                Arrays.asList("ACTIVE_1", "PENDING_2", "ACTIVE_3", "RESIGNED_4"));
        assertEquals(List.of("ACTIVE_1", "ACTIVE_3"), result);
    }

    @Test
    @DisplayName("After: stream-based filter produces same result as before")
    void filterActiveEmployees_After_matchesBefore() {
        List<String> input = Arrays.asList("ACTIVE_1", "PENDING_2", "ACTIVE_3");
        assertEquals(ex.filterActiveEmployees_Before(input), ex.filterActiveEmployees_After(input));
    }

    @Test
    @DisplayName("Predicate: isHighSalary returns true above 100K")
    void isHighSalary_aboveThreshold_returnsTrue() {
        assertTrue(ex.isHighSalary(150_000.0));
        assertFalse(ex.isHighSalary(50_000.0));
    }

    @Test
    @DisplayName("Function: formatEmployeeId pads with leading zeros")
    void formatEmployeeId_padsCorrectly() {
        assertEquals("EMP-000042", ex.formatEmployeeId(42));
        assertEquals("EMP-000001", ex.formatEmployeeId(1));
    }

    @Test
    @DisplayName("Supplier: getDefaultDepartment returns UNKNOWN_DEPT")
    void getDefaultDepartment_returnsDefault() {
        assertEquals("UNKNOWN_DEPT", ex.getDefaultDepartment());
    }

    @Test
    @DisplayName("BiFunction: buildEmployeeKey concatenates name and version")
    void buildEmployeeKey_formatsCorrectly() {
        assertEquals("Alice_v3", ex.buildEmployeeKey("Alice", 3));
    }

    @Test
    @DisplayName("Method reference: upperCaseNames converts to uppercase")
    void upperCaseNames_uppercasesAll() {
        List<String> result = ex.upperCaseNames(Arrays.asList("alice", "bob"));
        assertEquals(List.of("ALICE", "BOB"), result);
    }

    @Test
    @DisplayName("Function composition: sanitizeAndFormat trims and uppercases")
    void sanitizeAndFormat_trimsAndUppercases() {
        assertEquals("ALICE", ex.sanitizeAndFormat("  alice  "));
        assertEquals("BOB",   ex.sanitizeAndFormat("bob"));
    }

    @Test
    @DisplayName("Predicate composition: activeAndLongName requires both conditions")
    void activeAndLongName_requiresBothConditions() {
        Predicate<String> pred = ex.activeAndLongName();
        assertTrue(pred.test("ACTIVE_LONG_NAME"));       // starts with ACTIVE (16 chars > 10) ✓
        assertFalse(pred.test("ACTIVE_NO"));             // starts with ACTIVE but length=9 ≤ 10 ✗
        assertFalse(pred.test("PENDING_LONG_NAME_X"));   // length > 10 but doesn't start with ACTIVE ✗
    }

    @Test
    @DisplayName("Effectively final capture: filterByPrefix uses captured variable")
    void filterByPrefix_capturesVariable() {
        List<String> result = ex.filterByPrefix(
                Arrays.asList("ENG_1", "MKT_2", "ENG_3"), "ENG");
        assertEquals(List.of("ENG_1", "ENG_3"), result);
    }
}
