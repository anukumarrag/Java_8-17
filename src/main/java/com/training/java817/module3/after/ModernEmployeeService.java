package com.training.java817.module3.after;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * =============================================================================
 * MODULE 3 – WORKSHOP: AFTER STATE
 * ModernEmployeeService – all four tasks (after)
 * =============================================================================
 *
 * Changes applied:
 *  Task 1: Uses EmployeeRecord (record) instead of Employee POJO.
 *  Task 2: SQL queries use text blocks instead of string concatenation.
 *  Task 3: EmployeeEvent is now a sealed interface with record implementations.
 *  Task 4: instanceof pattern matching + switch expressions replace if-else chains.
 */
public class ModernEmployeeService {

    // =========================================================================
    // Task 2 (AFTER): Text block SQL queries
    // =========================================================================

    public String buildSearchQuery(String name, String status, LocalDate fromDate) {
        return """
                SELECT e.employee_id,
                       e.name,
                       e.salary,
                       e.department_id,
                       e.manager_id,
                       e.location,
                       e.job_title,
                       e.join_date,
                       e.review_date,
                       e.status
                FROM   employees e
                JOIN   departments d  ON d.id  = e.department_id
                JOIN   managers    m  ON m.id  = e.manager_id
                WHERE  e.name      = '%s'
                  AND  e.status    = '%s'
                  AND  e.join_date >= '%s'
                ORDER  BY e.join_date DESC,
                          e.salary   DESC
                """.formatted(name, status, fromDate);
    }

    public String buildAggregationQuery(String department) {
        return """
                SELECT e.department_id,
                       COUNT(*)      AS employee_count,
                       SUM(e.salary) AS total_salary,
                       AVG(e.salary) AS avg_salary
                FROM   employees e
                WHERE  e.department_id = '%s'
                  AND  e.status NOT IN ('RESIGNED', 'TERMINATED')
                GROUP  BY e.department_id
                """.formatted(department);
    }

    // =========================================================================
    // Task 3 + 4 (AFTER): Sealed events + pattern matching + switch expression
    // =========================================================================

    public String processEvent(EmployeeEvent event) {
        if (event instanceof EmployeeHiredEvent h) {
            return "Employee %s hired: name=%s, salary=%.2f, dept=%s"
                    .formatted(h.employeeId(), h.name(), h.salary(), h.departmentId());
        } else if (event instanceof EmployeeUpdatedEvent u) {
            return "Employee %s updated: new salary=%.2f, reason=%s"
                    .formatted(u.employeeId(), u.newSalary(), u.updateReason());
        } else if (event instanceof EmployeePromotedEvent p) {
            return "Employee %s promoted to %s at %.2f effective %s"
                    .formatted(p.employeeId(), p.newTitle(), p.newSalary(), p.effectiveDate());
        } else if (event instanceof EmployeeTerminatedEvent t) {
            return "Employee %s terminated [%s]: %s"
                    .formatted(t.employeeId(), t.terminationCode(), t.terminationReason());
        }
        throw new IllegalStateException("Unexpected event: " + event);
    }

    public String routeEvent(EmployeeEvent event) {
        if (event instanceof EmployeeHiredEvent)     return "employee-hired-topic";
        if (event instanceof EmployeeUpdatedEvent)   return "employee-update-topic";
        if (event instanceof EmployeePromotedEvent)  return "employee-promotion-topic";
        if (event instanceof EmployeeTerminatedEvent) return "employee-termination-topic";
        throw new IllegalStateException("Unexpected event type: " + event);
    }

    public String classifyEmployee(EmployeeRecord employee) {
        double s = employee.salary();
        return switch (employee.jobTitle()) {
            case "DIRECTOR"  -> s >= 200_000 ? "TIER1_EXECUTIVE"  : "TIER2_SENIOR";
            case "MANAGER"   -> s >= 100_000 ? "TIER2_SENIOR"     : "TIER3_MID";
            default          -> s >= 50_000  ? "TIER3_MID"        : "TIER4_JUNIOR";
        };
    }

    public int getNoticePeriodDays(String jobTitle) {
        return switch (jobTitle) {
            case "ENGINEER"  -> 60;
            case "MANAGER"   -> 90;
            case "DIRECTOR"  -> 120;
            case "ANALYST"   -> 30;
            default          -> 30;
        };
    }

    // =========================================================================
    // Stream-based data processing (replaces imperative loops)
    // =========================================================================

    public List<EmployeeRecord> filterByStatus(List<EmployeeRecord> employees, String status) {
        return employees.stream()
                .filter(e -> status.equals(e.status()))
                .collect(Collectors.toList());
    }

    public Map<String, Double> sumSalaryByDepartment(List<EmployeeRecord> employees) {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        EmployeeRecord::departmentId,
                        Collectors.summingDouble(EmployeeRecord::salary)));
    }

    public List<EmployeeRecord> topNBySalary(List<EmployeeRecord> employees, int n) {
        return employees.stream()
                .sorted((a, b) -> Double.compare(b.salary(), a.salary()))
                .limit(n)
                .collect(Collectors.toList());
    }

    // demo main
    public static void main(String[] args) {
        ModernEmployeeService svc = new ModernEmployeeService();

        System.out.println("=== Task 2: Text Block SQL ===");
        System.out.println(svc.buildSearchQuery("Alice", "ACTIVE", LocalDate.now().minusDays(30)));

        System.out.println("=== Task 3 + 4: Sealed Events ===");
        EmployeeEvent[] events = {
            new EmployeeHiredEvent("E001", "Alice", 85_000, "D01"),
            new EmployeePromotedEvent("E001", "SENIOR_ENGINEER", 100_000, "2024-06-01"),
            new EmployeeTerminatedEvent("E002", "VOLUNTARY", "Personal reasons"),
            new EmployeeUpdatedEvent("E003", 90_000, "Performance review")
        };
        for (EmployeeEvent e : events) {
            System.out.println("  " + svc.processEvent(e));
            System.out.println("  -> route to: " + svc.routeEvent(e));
        }

        System.out.println("=== Task 1: Record ===");
        EmployeeRecord emp = EmployeeRecord.pending(
                "E004", "Bob", 75_000, "D02", "M01", "NEW_YORK", "ENGINEER");
        System.out.println(emp);
        System.out.println("High salary: " + emp.isHighSalary());
        System.out.println("Classification: " + svc.classifyEmployee(emp));
    }
}
