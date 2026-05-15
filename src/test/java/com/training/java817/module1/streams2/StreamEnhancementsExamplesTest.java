package com.training.java817.module1.streams2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 1 – Stream API Enhancements (Java 9–16)")
class StreamEnhancementsExamplesTest {

    private StreamEnhancementsExamples ex;

    @BeforeEach
    void setUp() { ex = new StreamEnhancementsExamples(); }

    private List<StreamEnhancementsExamples.Employee> sampleEmployees() {
        return List.of(
                new StreamEnhancementsExamples.Employee("T1", "Alice",   "ENGINEERING", 100_000, "ONBOARDING"),
                new StreamEnhancementsExamples.Employee("T2", "Bob",     "MARKETING",   200_000, "ONBOARDING"),
                new StreamEnhancementsExamples.Employee("T3", "Charlie", "SALES",       300_000, "ACTIVE"),
                new StreamEnhancementsExamples.Employee("T4", "Diana",   "HR",           50_000, "RESIGNED")
        );
    }

    // ---- takeWhile ----------------------------------------------------------

    @Test
    @DisplayName("takeWhile: stops taking once salary exceeds ceiling")
    void salariesBelowThreshold_stopsAtCeiling() {
        var result = ex.salariesBelowThreshold(
                List.of(100.0, 150.0, 180.0, 210.0, 250.0), 200.0);
        assertEquals(List.of(100.0, 150.0, 180.0), result);
    }

    @Test
    @DisplayName("takeWhile: all elements below ceiling – returns all")
    void salariesBelowThreshold_allBelowCeiling_returnsAll() {
        var result = ex.salariesBelowThreshold(List.of(10.0, 20.0, 30.0), 100.0);
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("takeWhile: first element exceeds ceiling – returns empty")
    void salariesBelowThreshold_firstExceedsCeiling_returnsEmpty() {
        var result = ex.salariesBelowThreshold(List.of(300.0, 10.0, 20.0), 200.0);
        assertTrue(result.isEmpty(), "takeWhile stops on first failing element");
    }

    // ---- dropWhile ----------------------------------------------------------

    @Test
    @DisplayName("dropWhile: drops all leading ONBOARDING employees")
    void skipLeadingOnboarding_dropsLeadingOnboarding() {
        var result = ex.skipLeadingOnboarding(sampleEmployees());
        assertEquals(2, result.size(),                   "two non-ONBOARDING employees remain");
        assertEquals("ACTIVE", result.get(0).status(), "first remaining is ACTIVE");
    }

    @Test
    @DisplayName("dropWhile: no leading ONBOARDING – returns all")
    void skipLeadingOnboarding_noOnboarding_returnsAll() {
        var employees = List.of(
                new StreamEnhancementsExamples.Employee("T1", "Alice", "ENGINEERING", 100_000, "ACTIVE"),
                new StreamEnhancementsExamples.Employee("T2", "Bob",   "MARKETING",   200_000, "ONBOARDING")
        );
        assertEquals(2, ex.skipLeadingOnboarding(employees).size());
    }

    // ---- iterate with predicate ---------------------------------------------

    @Test
    @DisplayName("pageOffsets: produces correct page offsets")
    void pageOffsets_correctOffsets() {
        var offsets = ex.pageOffsets(100, 350);
        assertEquals(List.of(0, 100, 200, 300), offsets);
    }

    @Test
    @DisplayName("pageOffsets: total equals page size – single page")
    void pageOffsets_singlePage() {
        var offsets = ex.pageOffsets(100, 100);
        assertEquals(List.of(0), offsets);
    }

    @Test
    @DisplayName("pageOffsets: zero total – empty list")
    void pageOffsets_zeroTotal_emptyList() {
        var offsets = ex.pageOffsets(100, 0);
        assertTrue(offsets.isEmpty());
    }

    // ---- ofNullable ---------------------------------------------------------

    @Test
    @DisplayName("collectNotes: only includes notes that exist in the map")
    void collectNotes_onlyExistingNotes() {
        var employees = sampleEmployees();
        var notes  = Map.of("T1", "Important employee", "T3", "High performer");
        var result = ex.collectNotes(employees, notes);
        assertEquals(2, result.size());
        assertTrue(result.contains("Important employee"));
        assertTrue(result.contains("High performer"));
    }

    @Test
    @DisplayName("countNonNull: counts only non-null values in list")
    void countNonNull_countsOnlyNonNull() {
        var values = new java.util.ArrayList<String>();
        values.add("a");
        values.add(null);
        values.add("b");
        values.add(null);
        assertEquals(2L, ex.countNonNull(values));
    }

    // ---- Collectors.teeing --------------------------------------------------

    @Test
    @DisplayName("computeStats: sum and count are correct")
    void computeStats_correctSumAndCount() {
        var stats = ex.computeStats(sampleEmployees());
        assertEquals(650_000.0, stats.sum(),  0.001);
        assertEquals(4L,        stats.count());
    }

    @Test
    @DisplayName("partitionEmployees: correctly separates active from others")
    void partitionEmployees_separatesActiveFromOthers() {
        var result = ex.partitionEmployees(sampleEmployees());
        assertEquals(1, result.active().size());
        assertEquals(3, result.others().size());
        assertEquals("T3", result.active().get(0).id());
    }

    // ---- Stream.toList() ----------------------------------------------------

    @Test
    @DisplayName("activeDepartments: returns distinct active departments as unmodifiable list")
    void activeDepartments_returnsCorrectDepartments() {
        var departments = ex.activeDepartments(sampleEmployees());
        assertEquals(List.of("SALES"), departments);
        assertThrows(UnsupportedOperationException.class,
                () -> departments.add("EXTRA"), "toList() returns unmodifiable list");
    }

    // ---- eligibleDepartments (combined) -----------------------------------------

    @Test
    @DisplayName("eligibleDepartments: drops DRAFT prefix and respects salary ceiling")
    void eligibleDepartments_dropsDraftAndRespectsCeiling() {
        var employees = List.of(
                new StreamEnhancementsExamples.Employee("T0", "Draft",   "DRAFT_DEPT", 50_000,  "DRAFT"),
                new StreamEnhancementsExamples.Employee("T1", "Alice",   "ENGINEERING", 100_000, "ACTIVE"),
                new StreamEnhancementsExamples.Employee("T2", "Bob",     "MARKETING",   200_000, "ACTIVE"),
                new StreamEnhancementsExamples.Employee("T3", "Charlie", "SALES",       400_000, "ACTIVE")  // above ceiling
        );
        var result = ex.eligibleDepartments(employees, 300_000);
        assertTrue(result.contains("ENGINEERING"), "ENGINEERING is eligible");
        assertTrue(result.contains("MARKETING"),   "MARKETING is eligible");
        assertFalse(result.contains("DRAFT_DEPT"), "DRAFT should be skipped");
        assertFalse(result.contains("SALES"),      "SALES exceeds salary ceiling");
    }
}
