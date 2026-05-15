package com.training.java817.module1.streams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * =============================================================================
 * MODULE 1 – THE STREAM API (Java 8)
 * =============================================================================
 *
 * THEORY
 * ------
 * A Stream<T> is a sequence of elements that supports sequential and parallel
 * aggregate operations.  It is NOT a data structure – it does not store data.
 * You create a stream FROM a source (collection, array, file…) and then chain
 * INTERMEDIATE operations (lazy, return a stream) and a TERMINAL operation
 * (eager, returns a result or side effect).
 *
 * PROBLEM SOLVED
 * --------------
 * 1. Replaces verbose imperative for-loops with declarative, readable pipelines.
 * 2. Makes the intent of each step crystal clear (filter, map, reduce).
 * 3. Parallel processing via parallelStream() with zero thread management.
 * 4. Dramatically reduces the chance of mutation bugs caused by shared mutable
 *    state inside loops.
 *
 * STREAM PIPELINE
 *  source  ──►  [intermediate ops]  ──►  terminal op
 *  (lazy evaluation: nothing runs until the terminal op is reached)
 */
public class StreamExamples {

    // =========================================================================
    // Domain model used in examples
    // =========================================================================

    public record Employee(String id, String name, String department, double salary, String status) {}

    // =========================================================================
    // BEFORE Java 8 – Imperative style
    // =========================================================================

    /** Sum salary of all ACTIVE employees – imperative version. */
    public double sumActiveSalary_Before(List<Employee> employees) {
        double total = 0;
        for (Employee e : employees) {
            if ("ACTIVE".equals(e.status())) {
                total += e.salary();
            }
        }
        return total;
    }

    /** Get distinct departments sorted alphabetically – imperative version. */
    public List<String> distinctSortedDepartments_Before(List<Employee> employees) {
        List<String> departments = new ArrayList<>();
        for (Employee e : employees) {
            if (!departments.contains(e.department())) {
                departments.add(e.department());
            }
        }
        Collections.sort(departments);
        return departments;
    }

    /** Group employees by department – imperative version. */
    public Map<String, List<Employee>> groupByDepartment_Before(List<Employee> employees) {
        Map<String, List<Employee>> map = new HashMap<>();
        for (Employee e : employees) {
            map.computeIfAbsent(e.department(), k -> new ArrayList<>()).add(e);
        }
        return map;
    }

    // =========================================================================
    // AFTER Java 8 – Stream API
    // =========================================================================

    /** Sum salary of all ACTIVE employees – stream version. */
    public double sumActiveSalary_After(List<Employee> employees) {
        return employees.stream()
                .filter(e -> "ACTIVE".equals(e.status()))
                .mapToDouble(Employee::salary)
                .sum();
    }

    /** Get distinct departments sorted alphabetically – stream version. */
    public List<String> distinctSortedDepartments_After(List<Employee> employees) {
        return employees.stream()
                .map(Employee::department)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /** Group employees by department – stream version. */
    public Map<String, List<Employee>> groupByDepartment_After(List<Employee> employees) {
        return employees.stream()
                .collect(Collectors.groupingBy(Employee::department));
    }

    // =========================================================================
    // More intermediate operations
    // =========================================================================

    /** flatMap – flatten a list of lists. */
    public List<String> allSymbolsFromPortfolios(List<List<String>> portfolios) {
        return portfolios.stream()
                .flatMap(List::stream)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /** peek – non-consuming side-effect useful for debugging. */
    public List<Employee> topThreeBySalary(List<Employee> employees) {
        return employees.stream()
                .sorted(Comparator.comparingDouble(Employee::salary).reversed())
                .peek(e -> System.out.println("  Considering: " + e.id()))
                .limit(3)
                .collect(Collectors.toList());
    }

    /** reduce – custom aggregation (compute total salary). */
    public double totalSalary(List<Employee> employees) {
        return employees.stream()
                .mapToDouble(Employee::salary)
                .reduce(0.0, Double::sum);
    }

    /** count – count ONBOARDING employees. */
    public long countInactive(List<Employee> employees) {
        return employees.stream()
                .filter(e -> "ONBOARDING".equals(e.status()))
                .count();
    }

    /** anyMatch / allMatch / noneMatch – short-circuit predicates. */
    public boolean hasHighSalary(List<Employee> employees) {
        return employees.stream()
                .anyMatch(e -> e.salary() > 10_000_000.0);
    }

    public boolean allEmployeesActive(List<Employee> employees) {
        return employees.stream()
                .allMatch(e -> "ACTIVE".equals(e.status()));
    }

    /** Collectors.joining – build a CSV of employee IDs. */
    public String employeeIdsCsv(List<Employee> employees) {
        return employees.stream()
                .map(Employee::id)
                .collect(Collectors.joining(", "));
    }

    /** Collectors.toMap – index by employee ID. */
    public Map<String, Employee> indexById(List<Employee> employees) {
        return employees.stream()
                .collect(Collectors.toMap(Employee::id, e -> e));
    }

    /** Parallel stream – use with caution; best for CPU-bound, stateless ops. */
    public double parallelSalarySum(List<Employee> employees) {
        return employees.parallelStream()
                .mapToDouble(Employee::salary)
                .sum();
    }

    // demo main
    public static void main(String[] args) {
        List<Employee> employees = Arrays.asList(
                new Employee("E001", "Alice",   "ENGINEERING", 500_000, "ACTIVE"),
                new Employee("E002", "Bob",     "MARKETING",   1_200_000, "ONBOARDING"),
                new Employee("E003", "Charlie", "ENGINEERING", 800_000, "ACTIVE"),
                new Employee("E004", "Diana",   "SALES",       3_000_000, "ACTIVE"),
                new Employee("E005", "Eve",     "MARKETING",   200_000, "RESIGNED")
        );

        StreamExamples ex = new StreamExamples();
        System.out.println("Sum ACTIVE   : " + ex.sumActiveSalary_After(employees));
        System.out.println("Departments  : " + ex.distinctSortedDepartments_After(employees));
        System.out.println("IDs CSV      : " + ex.employeeIdsCsv(employees));
        System.out.println("Has high sal : " + ex.hasHighSalary(employees));
        System.out.println("All active   : " + ex.allEmployeesActive(employees));
    }
}
