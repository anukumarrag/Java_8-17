package com.training.java817.module1.collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 1 – Collection Enhancements")
class CollectionEnhancementsExamplesTest {

    private CollectionEnhancementsExamples ex;

    @BeforeEach
    void setUp() { ex = new CollectionEnhancementsExamples(); }

    private List<CollectionEnhancementsExamples.Employee> sampleEmployees() {
        return List.of(
                new CollectionEnhancementsExamples.Employee("E1", "Alice",   "ENGINEERING", 100_000, "ACTIVE"),
                new CollectionEnhancementsExamples.Employee("E2", "Bob",     "ENGINEERING", 200_000, "ONBOARDING"),
                new CollectionEnhancementsExamples.Employee("E3", "Charlie", "MARKETING",   300_000, "ACTIVE"),
                new CollectionEnhancementsExamples.Employee("E4", "Diana",   "MARKETING",   150_000, "RESIGNED")
        );
    }

    // ---- getOrDefault -------------------------------------------------------

    @Test
    @DisplayName("getOrDefault: returns value when key present")
    void getOrDefault_keyPresent_returnsValue() {
        Map<String, Integer> counts = new HashMap<>(Map.of("ENGINEERING", 5));
        assertEquals(5, ex.getEmployeeCount_After(counts, "ENGINEERING"));
    }

    @Test
    @DisplayName("getOrDefault: returns 0 when key absent")
    void getOrDefault_keyAbsent_returnsZero() {
        assertEquals(0, ex.getEmployeeCount_After(new HashMap<>(), "MARKETING"));
    }

    // ---- computeIfAbsent (groupByDepartment) ------------------------------------

    @Test
    @DisplayName("groupByDepartment: groups employees under correct department keys")
    void groupByDepartment_producesCorrectGroups() {
        var grouped = ex.groupByDepartment_After(sampleEmployees());
        assertEquals(2, grouped.size());
        assertEquals(2, grouped.get("ENGINEERING").size());
        assertEquals(2, grouped.get("MARKETING").size());
    }

    @Test
    @DisplayName("groupByDepartment: before and after produce same result")
    void groupByDepartment_beforeAndAfterMatchKeys() {
        var before = ex.groupByDepartment_Before(sampleEmployees());
        var after  = ex.groupByDepartment_After(sampleEmployees());
        assertEquals(before.keySet(), after.keySet());
    }

    // ---- merge (countByStatus) ----------------------------------------------

    @Test
    @DisplayName("countByStatus: counts each status correctly")
    void countByStatus_countsCorrectly() {
        var counts = ex.countByStatus_After(sampleEmployees());
        assertEquals(2, counts.get("ACTIVE"));
        assertEquals(1, counts.get("ONBOARDING"));
        assertEquals(1, counts.get("RESIGNED"));
    }

    @Test
    @DisplayName("countByStatus: before and after produce same counts")
    void countByStatus_beforeAndAfterMatch() {
        assertEquals(ex.countByStatus_Before(sampleEmployees()),
                     ex.countByStatus_After(sampleEmployees()));
    }

    // ---- putIfAbsent --------------------------------------------------------

    @Test
    @DisplayName("registerDefaultDepartments: adds missing departments without overwriting existing")
    void registerDefaultDepartments_addsDefaultsWithoutOverwriting() {
        Map<String, String> departments = new HashMap<>(Map.of("ENGINEERING", "custom@company.com"));
        ex.registerDefaultDepartments(departments);
        assertEquals("custom@company.com",       departments.get("ENGINEERING"), "existing value preserved");
        assertEquals("marketing@company.com",    departments.get("MARKETING"),   "missing value added");
        assertEquals("hr@company.com",           departments.get("HR"),          "missing value added");
    }

    // ---- computeIfPresent ---------------------------------------------------

    @Test
    @DisplayName("doubleIfPresent: doubles the value when key exists")
    void doubleIfPresent_doublesExistingValue() {
        Map<String, Integer> scores = new HashMap<>(Map.of("risk", 10));
        ex.doubleIfPresent(scores, "risk");
        assertEquals(20, scores.get("risk"));
    }

    @Test
    @DisplayName("doubleIfPresent: does nothing when key absent")
    void doubleIfPresent_doesNothingForAbsentKey() {
        Map<String, Integer> scores = new HashMap<>();
        ex.doubleIfPresent(scores, "risk");
        assertFalse(scores.containsKey("risk"));
    }

    // ---- replaceAll ---------------------------------------------------------

    @Test
    @DisplayName("normaliseNames: trims and uppercases all values")
    void normaliseNames_trimsAndUppercases() {
        Map<String, String> names = new HashMap<>(Map.of("k1", "  alice  ", "k2", "bob"));
        ex.normaliseNames(names);
        assertEquals("ALICE", names.get("k1"));
        assertEquals("BOB",   names.get("k2"));
    }

    // ---- Java 9 factory methods ---------------------------------------------

    @Test
    @DisplayName("supportedLocations: returns immutable list with 5 entries")
    void supportedLocations_returnsImmutableList() {
        List<String> locations = ex.supportedLocations();
        assertEquals(5, locations.size());
        assertThrows(UnsupportedOperationException.class,
                () -> locations.add("TOKYO"), "factory list should be immutable");
    }

    @Test
    @DisplayName("validStatuses: returns immutable set with correct values")
    void validStatuses_returnsImmutableSet() {
        Set<String> statuses = ex.validStatuses();
        assertTrue(statuses.contains("ACTIVE"));
        assertThrows(UnsupportedOperationException.class,
                () -> statuses.add("UNKNOWN"), "factory set should be immutable");
    }

    @Test
    @DisplayName("slaByStatus: Map.of returns correct SLA hours")
    void slaByStatus_returnsCorrectValues() {
        Map<String, Integer> sla = ex.slaByStatus();
        assertEquals(48, sla.get("APPLIED"));
        assertEquals(24, sla.get("ONBOARDING"));
        assertEquals(72, sla.get("ACTIVE"));
        assertEquals(0,  sla.get("RESIGNED"));
        assertThrows(UnsupportedOperationException.class,
                () -> sla.put("NEW", 99), "factory map should be immutable");
    }

    @Test
    @DisplayName("departmentEmailTable: Map.ofEntries supports more than 10 pairs")
    void departmentEmailTable_supportsMoreThan10Pairs() {
        Map<String, String> routing = ex.departmentEmailTable();
        assertTrue(routing.size() >= 11, "ofEntries supports > 10 pairs");
        assertEquals("engineering@company.com", routing.get("ENGINEERING"));
        assertEquals("data@company.com",        routing.get("DATA"));
    }

    // ---- copyOf -------------------------------------------------------------

    @Test
    @DisplayName("snapshotNames: immutable copy does not reflect later mutations")
    void snapshotNames_doesNotReflectMutations() {
        var mutable = new java.util.ArrayList<>(List.of("Alice", "Bob"));
        var snapshot = ex.snapshotNames(mutable);
        mutable.add("Charlie");
        assertEquals(2, snapshot.size(), "snapshot should not reflect post-copy mutation");
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.add("Diana"), "copyOf list should be immutable");
    }
}
