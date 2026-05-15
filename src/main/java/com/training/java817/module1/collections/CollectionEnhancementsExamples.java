package com.training.java817.module1.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * =============================================================================
 * MODULE 1 – COLLECTION ENHANCEMENTS (Java 8 Map API + Java 9 Factory Methods)
 * =============================================================================
 *
 * THEORY
 * ------
 * Collections are used in virtually every Java program.  Two major improvements
 * were made across Java 8 and Java 9:
 *
 * JAVA 8 – NEW Map METHODS
 * -------------------------
 *   getOrDefault(key, default)              – avoids null check after get()
 *   putIfAbsent(key, value)                 – safe initialisation (no overwrite)
 *   computeIfAbsent(key, mappingFn)         – lazy value creation, ideal for multimap
 *   computeIfPresent(key, remappingFn)      – update only if key already exists
 *   compute(key, remappingFn)               – unconditional remap (insert/update/delete)
 *   merge(key, value, remappingFn)          – accumulate / combine existing value
 *   forEach(BiConsumer)                     – iterate without entrySet boilerplate
 *   replaceAll(BiFunction)                  – transform all values in-place
 *   Map.Entry.comparingByKey/Value()        – sort entries fluently
 *
 * JAVA 9 – IMMUTABLE COLLECTION FACTORY METHODS
 * -----------------------------------------------
 *   List.of(...)    – immutable list, null elements NOT allowed
 *   Set.of(...)     – immutable set, null elements and duplicates NOT allowed
 *   Map.of(k,v,...) – immutable map, null keys/values NOT allowed (≤10 pairs)
 *   Map.ofEntries(Map.entry(k,v),...) – for >10 pairs
 *   Map.copyOf(existingMap)           – immutable copy of any map
 *   List.copyOf / Set.copyOf          – immutable copies
 *
 * PROBLEM SOLVED
 * --------------
 * 1. Removes repetitive null-check boilerplate (getOrDefault, computeIfAbsent).
 * 2. Dramatically simplifies multimap / frequency-count patterns.
 * 3. Factory methods replace verbose Arrays.asList / Collections.unmodifiableXxx.
 * 4. Immutable collections prevent accidental mutation of shared data.
 */
public class CollectionEnhancementsExamples {

    public record Employee(String id, String name, String department, double salary, String status) {}

    // =========================================================================
    // BEFORE – Java 7 Map patterns
    // =========================================================================

    /** getOrDefault – before: manual null check. */
    public int getEmployeeCount_Before(Map<String, Integer> counts, String department) {
        Integer count = counts.get(department);
        return count != null ? count : 0;
    }

    /** computeIfAbsent (multimap) – before: manual check + new list creation. */
    public Map<String, List<Employee>> groupByDepartment_Before(List<Employee> employees) {
        Map<String, List<Employee>> map = new HashMap<>();
        for (Employee e : employees) {
            if (!map.containsKey(e.department())) {
                map.put(e.department(), new ArrayList<>());
            }
            map.get(e.department()).add(e);
        }
        return map;
    }

    /** merge (frequency count) – before: verbose null-check increment. */
    public Map<String, Integer> countByStatus_Before(List<Employee> employees) {
        Map<String, Integer> counts = new HashMap<>();
        for (Employee e : employees) {
            Integer existing = counts.get(e.status());
            counts.put(e.status(), existing == null ? 1 : existing + 1);
        }
        return counts;
    }

    // =========================================================================
    // AFTER – Java 8 Map methods
    // =========================================================================

    /** getOrDefault: clean one-liner, no null check needed. */
    public int getEmployeeCount_After(Map<String, Integer> counts, String department) {
        return counts.getOrDefault(department, 0);
    }

    /**
     * computeIfAbsent: creates the list the first time the key is seen.
     * Perfect for building multimap / group-by structures.
     */
    public Map<String, List<Employee>> groupByDepartment_After(List<Employee> employees) {
        Map<String, List<Employee>> map = new HashMap<>();
        for (Employee e : employees) {
            map.computeIfAbsent(e.department(), k -> new ArrayList<>()).add(e);
        }
        return map;
    }

    /**
     * merge: if key absent, inserts value; if present, applies remapping function.
     * Ideal for frequency counting and accumulation.
     */
    public Map<String, Integer> countByStatus_After(List<Employee> employees) {
        Map<String, Integer> counts = new HashMap<>();
        for (Employee e : employees) {
            counts.merge(e.status(), 1, Integer::sum);
        }
        return counts;
    }

    /** forEach: iterate a map without the entrySet boilerplate. */
    public void printCounts(Map<String, Integer> counts) {
        counts.forEach((status, count) ->
                System.out.println(status + " : " + count));
    }

    /** putIfAbsent: safe default initialisation, won't overwrite existing value. */
    public Map<String, String> registerDefaultDepartments(Map<String, String> departments) {
        departments.putIfAbsent("ENGINEERING", "engineering@company.com");
        departments.putIfAbsent("MARKETING",   "marketing@company.com");
        departments.putIfAbsent("HR",          "hr@company.com");
        return departments;
    }

    /** computeIfPresent: update a value only when the key already exists. */
    public Map<String, Integer> doubleIfPresent(Map<String, Integer> scores, String key) {
        scores.computeIfPresent(key, (k, v) -> v * 2);
        return scores;
    }

    /** replaceAll: transform every value in-place. */
    public Map<String, String> normaliseNames(Map<String, String> names) {
        names.replaceAll((key, value) -> value.trim().toUpperCase());
        return names;
    }

    // =========================================================================
    // JAVA 9 – Immutable factory methods
    // =========================================================================

    /** List.of: concise immutable list (replaces Arrays.asList + unmodifiableList). */
    public List<String> supportedLocations() {
        return List.of("LONDON", "NEW_YORK", "BANGALORE", "SINGAPORE", "BERLIN");
    }

    /** Set.of: concise immutable set – duplicate elements cause IllegalArgumentException. */
    public Set<String> validStatuses() {
        return Set.of("APPLIED", "ONBOARDING", "ACTIVE", "ON_LEAVE", "RESIGNED", "TERMINATED");
    }

    /**
     * Map.of: concise immutable map (up to 10 key-value pairs).
     * Arguments are alternating key, value, key, value, ...
     */
    public Map<String, Integer> slaByStatus() {
        return Map.of(
                "APPLIED",    48,
                "ONBOARDING", 24,
                "ACTIVE",     72,
                "RESIGNED",   0
        );
    }

    /**
     * Map.ofEntries: for maps with more than 10 pairs.
     */
    public Map<String, String> departmentEmailTable() {
        return Map.ofEntries(
                Map.entry("ENGINEERING", "engineering@company.com"),
                Map.entry("MARKETING",   "marketing@company.com"),
                Map.entry("SALES",       "sales@company.com"),
                Map.entry("FINANCE",     "finance@company.com"),
                Map.entry("HR",          "hr@company.com"),
                Map.entry("LEGAL",       "legal@company.com"),
                Map.entry("OPERATIONS",  "operations@company.com"),
                Map.entry("IT",          "it@company.com"),
                Map.entry("DESIGN",      "design@company.com"),
                Map.entry("PRODUCT",     "product@company.com"),
                Map.entry("DATA",        "data@company.com")  // >10 pairs – requires ofEntries
        );
    }

    /** List.copyOf / Map.copyOf: produce immutable snapshots of mutable collections. */
    public List<String> snapshotNames(List<String> mutableList) {
        return List.copyOf(mutableList);   // immutable; reflects state at copy time
    }

    public Map<String, Integer> snapshotCounts(Map<String, Integer> mutable) {
        return Map.copyOf(mutable);
    }

    // demo main
    public static void main(String[] args) {
        CollectionEnhancementsExamples ex = new CollectionEnhancementsExamples();

        Map<String, Integer> counts = new HashMap<>();
        counts.put("ENGINEERING", 5);
        System.out.println("getOrDefault ENGINEERING : " + ex.getEmployeeCount_After(counts, "ENGINEERING"));
        System.out.println("getOrDefault MARKETING   : " + ex.getEmployeeCount_After(counts, "MARKETING"));

        List<Employee> employees = List.of(
                new Employee("E1", "Alice",   "ENGINEERING", 100_000, "ACTIVE"),
                new Employee("E2", "Bob",     "ENGINEERING", 200_000, "ONBOARDING"),
                new Employee("E3", "Charlie", "MARKETING",   300_000, "ACTIVE")
        );
        System.out.println("Group by dept   : " + ex.groupByDepartment_After(employees).keySet());

        Map<String, Integer> freq = ex.countByStatus_After(employees);
        System.out.println("Count ACTIVE    : " + freq.get("ACTIVE"));

        System.out.println("Locations       : " + ex.supportedLocations());
        System.out.println("Statuses        : " + ex.validStatuses());
        System.out.println("SLA by status   : " + ex.slaByStatus());
    }
}
