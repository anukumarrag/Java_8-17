package com.training.java817.module3;

import com.training.java817.module3.after.*;
import com.training.java817.module3.before.Employee;
import com.training.java817.module3.before.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 3 – Workshop: Before vs After")
class WorkshopTest {

    private EmployeeService       legacySvc;
    private ModernEmployeeService modernSvc;

    private static final LocalDate TODAY       = LocalDate.of(2024, 6, 1);
    private static final LocalDate REVIEW_DATE = TODAY.plusDays(90);

    @BeforeEach
    void setUp() {
        legacySvc  = new EmployeeService();
        modernSvc  = new ModernEmployeeService();
    }

    // =========================================================================
    // Task 1: POJO vs Record
    // =========================================================================

    @Test
    @DisplayName("Task 1: Employee POJO has same data as Record")
    void task1_pojoAndRecord_containSameData() {
        Employee pojo = new Employee(
                "E001", "Alice Smith", 95_000.0, "D01", "M01",
                "LONDON", "ENGINEER", TODAY, REVIEW_DATE, "ACTIVE");

        EmployeeRecord record = new EmployeeRecord(
                "E001", "Alice Smith", 95_000.0, "D01", "M01",
                "LONDON", "ENGINEER", TODAY, REVIEW_DATE, "ACTIVE");

        assertEquals(pojo.getEmployeeId(),   record.employeeId());
        assertEquals(pojo.getName(),         record.name());
        assertEquals(pojo.getSalary(),       record.salary());
        assertEquals(pojo.getDepartmentId(), record.departmentId());
        assertEquals(pojo.getStatus(),       record.status());
    }

    @Test
    @DisplayName("Task 1: Record isHighSalary() returns false for 95K salary")
    void task1_record_isHighSalary_95K_false() {
        EmployeeRecord record = new EmployeeRecord(
                "E001", "Alice Smith", 95_000.0, "D01", "M01",
                "LONDON", "ENGINEER", TODAY, REVIEW_DATE, "ACTIVE");
        assertFalse(record.isHighSalary());
    }

    @Test
    @DisplayName("Task 1: Record isHighSalary() returns true for 150K salary")
    void task1_record_isHighSalary_150K_true() {
        EmployeeRecord record = new EmployeeRecord(
                "E001", "Alice Smith", 150_000.0, "D01", "M01",
                "LONDON", "ENGINEER", TODAY, REVIEW_DATE, "ACTIVE");
        assertTrue(record.isHighSalary());
    }

    @Test
    @DisplayName("Task 1: Record pending() factory sets ONBOARDING status")
    void task1_record_pendingFactory() {
        EmployeeRecord pending = EmployeeRecord.pending(
                "E002", "Bob", 80_000.0, "D02", "M02", "NEW_YORK", "ANALYST");
        assertEquals("ONBOARDING", pending.status());
    }

    @Test
    @DisplayName("Task 1: Record withStatus() returns new record with updated status")
    void task1_record_withStatus() {
        EmployeeRecord onboarding = EmployeeRecord.pending(
                "E003", "Charlie", 75_000.0, "D03", "M03", "BANGALORE", "ENGINEER");
        EmployeeRecord active = onboarding.withStatus("ACTIVE");
        assertEquals("ACTIVE", active.status());
        assertEquals(onboarding.employeeId(), active.employeeId());  // immutable copy
    }

    @Test
    @DisplayName("Task 1: Record negative salary throws IllegalArgumentException")
    void task1_record_negativeSalary_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new EmployeeRecord("E1", "Alice", -1.0, "D1", "M1",
                        "LONDON", "ENGINEER", TODAY, REVIEW_DATE, "ACTIVE"));
    }

    // =========================================================================
    // Task 2: SQL Text Block
    // =========================================================================

    @Test
    @DisplayName("Task 2: Before/After SQL contain same core clauses")
    void task2_sqlBeforeAfter_sameCoreClauses() {
        String before = legacySvc.buildSearchQuery("Alice", "ACTIVE", TODAY);
        String after  = modernSvc.buildSearchQuery("Alice", "ACTIVE", TODAY);

        for (String clause : new String[]{"SELECT", "FROM", "WHERE", "ORDER"}) {
            assertTrue(before.contains(clause), "before missing: " + clause);
            assertTrue(after.contains(clause),  "after missing: "  + clause);
        }
        assertTrue(before.contains("Alice"));
        assertTrue(after.contains("Alice"));
    }

    @Test
    @DisplayName("Task 2: Aggregation query Before/After both contain GROUP BY")
    void task2_aggregationQuery_containsGroupBy() {
        String before = legacySvc.buildAggregationQuery("ENGINEERING");
        String after  = modernSvc.buildAggregationQuery("ENGINEERING");
        assertTrue(before.contains("GROUP"));
        assertTrue(after.contains("GROUP"));
    }

    // =========================================================================
    // Task 3: Sealed Events
    // =========================================================================

    @Test
    @DisplayName("Task 3: EmployeeHiredEvent is-a EmployeeEvent")
    void task3_employeeHiredEvent_isEmployeeEvent() {
        EmployeeEvent event = new EmployeeHiredEvent("E001", "Alice", 85_000, "D01");
        assertInstanceOf(EmployeeEvent.class, event);
    }

    @Test
    @DisplayName("Task 3: processEvent describes hire correctly")
    void task3_processEvent_hire() {
        EmployeeEvent event = new EmployeeHiredEvent("E001", "Alice", 85_000, "D01");
        String desc = modernSvc.processEvent(event);
        assertTrue(desc.contains("E001"));
        assertTrue(desc.contains("Alice"));
    }

    @Test
    @DisplayName("Task 3: processEvent describes termination correctly")
    void task3_processEvent_termination() {
        EmployeeEvent event = new EmployeeTerminatedEvent("E002", "RISK_LIMIT", "Over daily limit");
        String desc = modernSvc.processEvent(event);
        assertTrue(desc.contains("E002"));
        assertTrue(desc.contains("Over daily limit"));
    }

    @Test
    @DisplayName("Task 3: processEvent describes promotion correctly")
    void task3_processEvent_promotion() {
        EmployeeEvent event = new EmployeePromotedEvent("E003", "SENIOR_ENGINEER", 120_000.0, "2024-06-01");
        String desc = modernSvc.processEvent(event);
        assertTrue(desc.contains("E003"));
        assertTrue(desc.contains("SENIOR_ENGINEER"));
    }

    @Test
    @DisplayName("Task 3: processEvent describes update correctly")
    void task3_processEvent_update() {
        EmployeeEvent event = new EmployeeUpdatedEvent("E004", 90_000, "Performance review");
        String desc = modernSvc.processEvent(event);
        assertTrue(desc.contains("E004"));
        assertTrue(desc.contains("Performance review"));
    }

    // =========================================================================
    // Task 4: Modern Control Flow
    // =========================================================================

    @Test
    @DisplayName("Task 4: routeEvent correctly routes each sealed event type")
    void task4_routeEvent_allTypes() {
        assertEquals("employee-hired-topic",
                modernSvc.routeEvent(new EmployeeHiredEvent("E1", "Alice", 85_000, "D1")));
        assertEquals("employee-update-topic",
                modernSvc.routeEvent(new EmployeeUpdatedEvent("E2", 90_000, "reason")));
        assertEquals("employee-promotion-topic",
                modernSvc.routeEvent(new EmployeePromotedEvent("E3", "LEAD", 100_000, "2024-01-01")));
        assertEquals("employee-termination-topic",
                modernSvc.routeEvent(new EmployeeTerminatedEvent("E4", "CODE", "reason")));
    }

    @Test
    @DisplayName("Task 4: getNoticePeriodDays Before/After match for all job titles")
    void task4_noticePeriodDays_beforeAndAfterMatch() {
        for (String jt : new String[]{"ENGINEER", "MANAGER", "DIRECTOR", "ANALYST", "UNKNOWN"}) {
            assertEquals(
                    legacySvc.getNoticePeriodDays_Before(jt),
                    modernSvc.getNoticePeriodDays(jt),
                    "Mismatch for job title: " + jt);
        }
    }

    // =========================================================================
    // Stream-based data processing
    // =========================================================================

    private List<EmployeeRecord> sampleRecords() {
        return Arrays.asList(
                new EmployeeRecord("E1", "Alice",   500_000,   "D01", "M01", "LONDON",    "ENGINEER", TODAY, REVIEW_DATE, "ACTIVE"),
                new EmployeeRecord("E2", "Bob",     1_200_000, "D02", "M02", "NEW_YORK",  "MANAGER",  TODAY, REVIEW_DATE, "ONBOARDING"),
                new EmployeeRecord("E3", "Charlie", 800_000,   "D01", "M01", "LONDON",    "ENGINEER", TODAY, REVIEW_DATE, "ACTIVE"),
                new EmployeeRecord("E4", "Diana",   3_000_000, "D03", "M03", "BANGALORE", "DIRECTOR", TODAY, REVIEW_DATE, "ACTIVE")
        );
    }

    @Test
    @DisplayName("filterByStatus: returns only ACTIVE employees")
    void filterByStatus_activeOnly() {
        List<EmployeeRecord> active = modernSvc.filterByStatus(sampleRecords(), "ACTIVE");
        assertEquals(3, active.size());
        assertTrue(active.stream().allMatch(e -> "ACTIVE".equals(e.status())));
    }

    @Test
    @DisplayName("sumSalaryByDepartment: D01 totals 1.3M")
    void sumSalaryByDepartment_d01Total() {
        Map<String, Double> sums = modernSvc.sumSalaryByDepartment(sampleRecords());
        assertEquals(1_300_000.0, sums.get("D01"), 1e-9);
    }

    @Test
    @DisplayName("topNBySalary: top 2 employees are Diana and Bob")
    void topNBySalary_top2() {
        List<EmployeeRecord> top2 = modernSvc.topNBySalary(sampleRecords(), 2);
        assertEquals(2, top2.size());
        assertEquals("Diana", top2.get(0).name());
        assertEquals("Bob",   top2.get(1).name());
    }
}
