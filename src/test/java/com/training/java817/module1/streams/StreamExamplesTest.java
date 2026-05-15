package com.training.java817.module1.streams;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 1 – Stream API")
class StreamExamplesTest {

    private StreamExamples ex;
    private List<StreamExamples.Employee> employees;

    @BeforeEach
    void setUp() {
        ex = new StreamExamples();
        employees = Arrays.asList(
                new StreamExamples.Employee("E001", "Alice",   "ENGINEERING", 500_000, "ACTIVE"),
                new StreamExamples.Employee("E002", "Bob",     "MARKETING", 1_200_000, "ONBOARDING"),
                new StreamExamples.Employee("E003", "Charlie", "ENGINEERING", 800_000, "ACTIVE"),
                new StreamExamples.Employee("E004", "Diana",   "SALES",    3_000_000, "ACTIVE"),
                new StreamExamples.Employee("E005", "Eve",     "MARKETING",  200_000, "RESIGNED")
        );
    }

    @Test
    @DisplayName("Before: imperative sum of ACTIVE salary")
    void sumActiveSalary_Before() {
        assertEquals(4_300_000.0, ex.sumActiveSalary_Before(employees));
    }

    @Test
    @DisplayName("After: stream sum matches imperative sum")
    void sumActiveSalary_After_matchesBefore() {
        assertEquals(
                ex.sumActiveSalary_Before(employees),
                ex.sumActiveSalary_After(employees));
    }

    @Test
    @DisplayName("Before: imperative distinct sorted departments")
    void distinctSortedDepartments_Before() {
        List<String> result = ex.distinctSortedDepartments_Before(employees);
        assertEquals(List.of("ENGINEERING", "MARKETING", "SALES"), result);
    }

    @Test
    @DisplayName("After: stream distinct sorted departments matches before")
    void distinctSortedDepartments_After_matchesBefore() {
        assertEquals(
                ex.distinctSortedDepartments_Before(employees),
                ex.distinctSortedDepartments_After(employees));
    }

    @Test
    @DisplayName("groupByDepartment returns correct group sizes")
    void groupByDepartment_After_correctGroupSizes() {
        Map<String, List<StreamExamples.Employee>> grouped = ex.groupByDepartment_After(employees);
        assertEquals(2, grouped.get("ENGINEERING").size());
        assertEquals(2, grouped.get("MARKETING").size());
        assertEquals(1, grouped.get("SALES").size());
    }

    @Test
    @DisplayName("flatMap flattens nested lists with distinct sorted result")
    void allSymbolsFromPortfolios_flattens() {
        List<List<String>> portfolios = Arrays.asList(
                Arrays.asList("AAPL", "MSFT"),
                Arrays.asList("GOOG", "AAPL")
        );
        List<String> result = ex.allSymbolsFromPortfolios(portfolios);
        assertEquals(List.of("AAPL", "GOOG", "MSFT"), result);
    }

    @Test
    @DisplayName("countInactive returns correct count")
    void countInactive_returnsOne() {
        assertEquals(1, ex.countInactive(employees));
    }

    @Test
    @DisplayName("anyMatch: hasHighSalary detects 15M salary")
    void hasHighSalary_detectsHighSalary() {
        List<StreamExamples.Employee> withHighSalary = Arrays.asList(
                new StreamExamples.Employee("E001", "Alice", "ENGINEERING",   500_000, "ACTIVE"),
                new StreamExamples.Employee("E004", "Diana", "SALES",      15_000_000, "ACTIVE") // >10M
        );
        assertTrue(ex.hasHighSalary(withHighSalary));
    }

    @Test
    @DisplayName("anyMatch: no high salary when all are small")
    void hasHighSalary_allSmall_returnsFalse() {
        List<StreamExamples.Employee> small = List.of(
                new StreamExamples.Employee("E1", "Alice", "ENGINEERING", 50_000, "ACTIVE"));
        assertFalse(ex.hasHighSalary(small));
    }

    @Test
    @DisplayName("allMatch: not all employees are active")
    void allEmployeesActive_returnsFalse_whenMixed() {
        assertFalse(ex.allEmployeesActive(employees));
    }

    @Test
    @DisplayName("allMatch: true when every employee is active")
    void allEmployeesActive_returnsTrue_whenAllActive() {
        List<StreamExamples.Employee> allActive = List.of(
                new StreamExamples.Employee("E1", "Alice", "ENGINEERING", 100_000, "ACTIVE"),
                new StreamExamples.Employee("E2", "Bob",   "MARKETING",   200_000, "ACTIVE")
        );
        assertTrue(ex.allEmployeesActive(allActive));
    }

    @Test
    @DisplayName("joining: employeeIdsCsv produces comma-separated IDs")
    void employeeIdsCsv_correctFormat() {
        String csv = ex.employeeIdsCsv(employees);
        assertTrue(csv.contains("E001"));
        assertTrue(csv.contains("E002"));
        assertTrue(csv.contains(", "));
    }

    @Test
    @DisplayName("toMap: indexById produces correct map")
    void indexById_correctMap() {
        Map<String, StreamExamples.Employee> map = ex.indexById(employees);
        assertEquals(5, map.size());
        assertEquals("ENGINEERING", map.get("E001").department());
        assertEquals("SALES",       map.get("E004").department());
    }

    @Test
    @DisplayName("totalSalary: sum of all salaries")
    void totalSalary_sumsAll() {
        assertEquals(5_700_000.0, ex.totalSalary(employees));
    }
}
