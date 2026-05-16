package com.training.java817.module1.var;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 1 – Local Variable Type Inference (var)")
class LocalVarInferenceExamplesTest {

    private LocalVarInferenceExamples ex;

    @BeforeEach
    void setUp() { ex = new LocalVarInferenceExamples(); }

    private List<LocalVarInferenceExamples.Employee> sampleEmployees() {
        return List.of(
                new LocalVarInferenceExamples.Employee("E1", "Alice",   "ENGINEERING", 50_000,  "ACTIVE"),
                new LocalVarInferenceExamples.Employee("E2", "Bob",     "ENGINEERING", 150_000, "ACTIVE"),
                new LocalVarInferenceExamples.Employee("E3", "Charlie", "MARKETING",   80_000,  "ONBOARDING")
        );
    }

    // ---- groupByDepartment ---------------------------------------------------

    @Test
    @DisplayName("groupByDepartment: before and after produce identical results")
    void groupByDepartment_beforeAndAfterMatch() {
        var employees = sampleEmployees();
        assertEquals(ex.groupByDepartment_Before(employees).keySet(),
                     ex.groupByDepartment_After(employees).keySet());
    }

    @Test
    @DisplayName("groupByDepartment_After: groups correctly using var")
    void groupByDepartment_After_groupsCorrectly() {
        var grouped = ex.groupByDepartment_After(sampleEmployees());
        assertEquals(2, grouped.size());
        assertEquals(2, grouped.get("ENGINEERING").size());
        assertEquals(1, grouped.get("MARKETING").size());
    }

    // ---- sumHighSalaryEmployees ----------------------------------------------

    @Test
    @DisplayName("sumHighSalaryEmployees: only sums employees above 100k salary")
    void sumHighSalaryEmployees_onlySumsAbove100k() {
        double sum = ex.sumHighSalaryEmployees(sampleEmployees());
        assertEquals(150_000.0, sum, 0.001,
                "only E2 (150k) should be counted; E1 (50k) and E3 (80k) are below");
    }

    @Test
    @DisplayName("sumHighSalaryEmployees: returns 0 when no employees above threshold")
    void sumHighSalaryEmployees_noneAboveThreshold_returnsZero() {
        var lowEmployees = List.of(
                new LocalVarInferenceExamples.Employee("E1", "Alice", "ENGINEERING", 50_000, "ACTIVE")
        );
        assertEquals(0.0, ex.sumHighSalaryEmployees(lowEmployees), 0.001);
    }

    // ---- readLines -----------------------------------------------------------

    @Test
    @DisplayName("readLines: reads all lines from a multi-line string")
    void readLines_readsAllLines() throws Exception {
        var lines = ex.readLines("line1\nline2\nline3");
        assertEquals(3, lines.size());
        assertEquals("line1", lines.get(0));
        assertEquals("line3", lines.get(2));
    }

    @Test
    @DisplayName("readLines: single line returns one entry")
    void readLines_singleLine_returnsOneEntry() throws Exception {
        var lines = ex.readLines("hello");
        assertEquals(1, lines.size());
        assertEquals("hello", lines.get(0));
    }

    // ---- processNames (var in lambda) ----------------------------------------

    @Test
    @DisplayName("processNames: strips, uppercases, and filters blank/null")
    void processNames_stripsAndUppercases() {
        var mutable = new ArrayList<String>();
        mutable.add("alice");
        mutable.add(" bob ");
        mutable.add(null);
        mutable.add("");
        var result = ex.processNames(mutable);
        assertEquals(List.of("ALICE", "BOB"), result);
    }

    // ---- buildNestedMap ------------------------------------------------------

    @Test
    @DisplayName("buildNestedMap: produces correct deeply nested structure")
    void buildNestedMap_correctStructure() {
        var map = ex.buildNestedMap();
        assertNotNull(map.get("europe"));
        assertEquals(List.of("ENGINEERING", "MARKETING"), map.get("europe").get("departments"));
    }

    // ---- formatEmployee ------------------------------------------------------

    @Test
    @DisplayName("formatEmployee: produces formatted string")
    void formatEmployee_producesFormattedString() {
        String result = ex.formatEmployee("E001", 150_000.0);
        assertEquals("EMP[E001]=150000.0", result);
    }
}
