package com.training.java817.module1.concurrent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 1 – CompletableFuture")
class CompletableFutureExamplesTest {

    private CompletableFutureExamples ex;

    @BeforeEach
    void setUp() { ex = new CompletableFutureExamples(); }

    // ---- fetchFormattedSalary ------------------------------------------------

    @Test
    @DisplayName("fetchFormattedSalary: formats department and salary")
    void fetchFormattedSalary_containsDepartmentAndSalary() {
        String result = ex.fetchFormattedSalary("ENGINEERING").join();
        assertTrue(result.startsWith("ENGINEERING"), "should start with ENGINEERING");
        assertTrue(result.contains("85000"),          "should contain salary 85000");
    }

    @Test
    @DisplayName("fetchFormattedSalary: unknown department returns default salary")
    void fetchFormattedSalary_unknownDepartment_usesDefault() {
        String result = ex.fetchFormattedSalary("UNKNOWN").join();
        assertTrue(result.contains("70000"), "default salary should be 70000");
    }

    // ---- fetchEmployeeWithDepartment -----------------------------------------

    @Test
    @DisplayName("fetchEmployeeWithDepartment: result contains employee and dept info")
    void fetchEmployeeWithDepartment_containsEmployeeAndDept() {
        String result = ex.fetchEmployeeWithDepartment("E001").join();
        assertTrue(result.contains("EMPLOYEE:E001"), "should contain employee id");
        assertTrue(result.contains("DEPT:"),          "should contain department info");
    }

    // ---- fetchSalaryRange --------------------------------------------------------

    @Test
    @DisplayName("fetchSalaryRange: result contains MIN, MAX, and RANGE")
    void fetchSalaryRange_containsAllComponents() {
        String result = ex.fetchSalaryRange("ENGINEERING").join();
        assertTrue(result.contains("MIN="),    "should contain MIN");
        assertTrue(result.contains("MAX="),    "should contain MAX");
        assertTrue(result.contains("RANGE="),  "should contain RANGE");
    }

    // ---- enrichEmployeesBatch --------------------------------------------------

    @Test
    @DisplayName("enrichEmployeesBatch: all employees are enriched")
    void enrichEmployeesBatch_allEmployeesEnriched() {
        var ids    = List.of("E1", "E2", "E3");
        var result = ex.enrichEmployeesBatch(ids);
        assertEquals(3, result.size());
        assertTrue(result.contains("E1:ENRICHED"), "E1 should be enriched");
        assertTrue(result.contains("E2:ENRICHED"), "E2 should be enriched");
        assertTrue(result.contains("E3:ENRICHED"), "E3 should be enriched");
    }

    @Test
    @DisplayName("enrichEmployeesBatch: empty list returns empty result")
    void enrichEmployeesBatch_emptyList_returnsEmpty() {
        assertTrue(ex.enrichEmployeesBatch(List.of()).isEmpty());
    }

    // ---- fetchWithFallback --------------------------------------------------

    @Test
    @DisplayName("fetchWithFallback: valid id returns enriched result")
    void fetchWithFallback_validId_returnsEnriched() {
        String result = ex.fetchWithFallback("E001").join();
        assertEquals("ENRICHED:E001", result);
    }

    @Test
    @DisplayName("fetchWithFallback: blank id triggers fallback")
    void fetchWithFallback_blankId_returnsFallback() {
        String result = ex.fetchWithFallback("").join();
        assertTrue(result.startsWith("FALLBACK:"), "blank id should return fallback");
    }

    @Test
    @DisplayName("fetchWithFallback: null id triggers fallback")
    void fetchWithFallback_nullId_returnsFallback() {
        String result = ex.fetchWithFallback(null).join();
        assertTrue(result.startsWith("FALLBACK:"), "null id should return fallback");
    }

    // ---- fetchWithHandle ----------------------------------------------------

    @Test
    @DisplayName("fetchWithHandle: valid id returns OK result")
    void fetchWithHandle_validId_returnsOk() {
        String result = ex.fetchWithHandle("E001").join();
        assertEquals("OK[DATA:E001]", result);
    }

    @Test
    @DisplayName("fetchWithHandle: INVALID id returns ERROR result")
    void fetchWithHandle_invalidId_returnsError() {
        String result = ex.fetchWithHandle("INVALID").join();
        assertTrue(result.startsWith("ERROR["), "should return ERROR for INVALID id");
    }

    // ---- alreadyDone --------------------------------------------------------

    @Test
    @DisplayName("alreadyDone: already-completed future returns the value immediately")
    void alreadyDone_returnsValueImmediately() throws ExecutionException, InterruptedException {
        CompletableFuture<String> cf = ex.alreadyDone("DONE");
        assertTrue(cf.isDone());
        assertEquals("DONE", cf.get());
    }

    // ---- simulateSalaryFetch helper ------------------------------------------

    @Test
    @DisplayName("simulateSalaryFetch: returns deterministic salaries for known departments")
    void simulateSalaryFetch_deterministicForKnownDepartments() {
        assertEquals(85_000, ex.simulateSalaryFetch("ENGINEERING"), 0.001);
        assertEquals(75_000, ex.simulateSalaryFetch("MARKETING"),   0.001);
        assertEquals(80_000, ex.simulateSalaryFetch("SALES"),        0.001);
        assertEquals(70_000, ex.simulateSalaryFetch("UNKNOWN"),      0.001);
    }
}
