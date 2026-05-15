package com.training.java817.module3.before;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * =============================================================================
 * MODULE 3 – WORKSHOP: BEFORE STATE
 * Legacy EmployeeService – all four tasks (before)
 * =============================================================================
 *
 * This service intentionally uses:
 *  - String concatenation for SQL (Task 2)
 *  - Uncontrolled EmployeeEvent interface (Task 3)
 *  - instanceof + manual cast + if-else chain (Task 4)
 *  - Imperative loops instead of streams
 *
 * Workshop goal: refactor this into the modern after/ version.
 */
public class EmployeeService {

    // =========================================================================
    // Task 2 (BEFORE): SQL built by string concatenation
    // =========================================================================

    /** Fragile SQL construction – easy to introduce space/newline bugs. */
    public String buildSearchQuery(String name, String status, LocalDate fromDate) {
        return "SELECT e.employee_id, e.name, e.salary, e.department_id, " +
               "e.manager_id, e.location, e.job_title, e.join_date, " +
               "e.review_date, e.status " +
               "FROM employees e " +
               "JOIN departments d ON d.id = e.department_id " +
               "JOIN managers m ON m.id = e.manager_id " +
               "WHERE e.name = '" + name + "' " +
               "AND   e.status = '" + status + "' " +
               "AND   e.join_date >= '" + fromDate + "' " +
               "ORDER BY e.join_date DESC, e.salary DESC";
    }

    /** Aggregation query with concatenation. */
    public String buildAggregationQuery(String department) {
        return "SELECT e.department_id, " +
               "       COUNT(*) AS employee_count, " +
               "       SUM(e.salary) AS total_salary, " +
               "       AVG(e.salary) AS avg_salary " +
               "FROM employees e " +
               "WHERE e.department_id = '" + department + "' " +
               "  AND e.status NOT IN ('RESIGNED', 'TERMINATED') " +
               "GROUP BY e.department_id";
    }

    // =========================================================================
    // Task 3 (BEFORE): Uncontrolled event interface
    // =========================================================================

    /**
     * Any class can implement EmployeeEventLegacy.
     */
    public interface EmployeeEventLegacy {
        String getEmployeeId();
        String getEventType();
    }

    public static class EmployeeHiredLegacy implements EmployeeEventLegacy {
        private String employeeId; private String name;
        public EmployeeHiredLegacy(String employeeId, String name) {
            this.employeeId = employeeId; this.name = name;
        }
        @Override public String getEmployeeId() { return employeeId; }
        @Override public String getEventType()  { return "HIRED"; }
        public String getName()                 { return name; }
    }

    public static class EmployeePromotedLegacy implements EmployeeEventLegacy {
        private String employeeId; private double newSalary;
        public EmployeePromotedLegacy(String employeeId, double newSalary) {
            this.employeeId = employeeId; this.newSalary = newSalary;
        }
        @Override public String getEmployeeId() { return employeeId; }
        @Override public String getEventType()  { return "PROMOTED"; }
        public double getNewSalary()            { return newSalary; }
    }

    public static class EmployeeTerminatedLegacy implements EmployeeEventLegacy {
        private String employeeId; private String reason;
        public EmployeeTerminatedLegacy(String employeeId, String reason) {
            this.employeeId = employeeId; this.reason = reason;
        }
        @Override public String getEmployeeId() { return employeeId; }
        @Override public String getEventType()  { return "TERMINATED"; }
        public String getReason()               { return reason; }
    }

    // =========================================================================
    // Task 4 (BEFORE): if-else + manual casts + instanceof
    // =========================================================================

    public String processEventLegacy(EmployeeEventLegacy event) {
        String result;
        if (event instanceof EmployeeHiredLegacy) {
            EmployeeHiredLegacy hired = (EmployeeHiredLegacy) event;
            result = "Employee " + hired.getEmployeeId() + " hired: " + hired.getName();
        } else if (event instanceof EmployeePromotedLegacy) {
            EmployeePromotedLegacy promoted = (EmployeePromotedLegacy) event;
            result = "Employee " + promoted.getEmployeeId() + " promoted, new salary: " + promoted.getNewSalary();
        } else if (event instanceof EmployeeTerminatedLegacy) {
            EmployeeTerminatedLegacy terminated = (EmployeeTerminatedLegacy) event;
            result = "Employee " + terminated.getEmployeeId() + " terminated: " + terminated.getReason();
        } else {
            result = "Unknown event type: " + event.getEventType();
        }
        return result;
    }

    /**
     * Classify employee by salary – uses nested if-else chain.
     */
    public String classifyEmployee_Before(Employee employee) {
        String classification;
        if (employee.getSalary() >= 200_000) {
            classification = "TIER1_EXECUTIVE";
        } else if (employee.getSalary() >= 100_000) {
            classification = "TIER2_SENIOR";
        } else if (employee.getSalary() >= 50_000) {
            classification = "TIER3_MID";
        } else {
            classification = "TIER4_JUNIOR";
        }
        return classification;
    }

    /**
     * Get notice period days from job title – old switch statement.
     */
    public int getNoticePeriodDays_Before(String jobTitle) {
        int days;
        switch (jobTitle) {
            case "ENGINEER":
                days = 60;
                break;
            case "MANAGER":
                days = 90;
                break;
            case "DIRECTOR":
                days = 120;
                break;
            case "ANALYST":
                days = 30;
                break;
            default:
                days = 30;
        }
        return days;
    }

    // =========================================================================
    // Imperative data processing (BEFORE streams)
    // =========================================================================

    public List<Employee> filterByStatus(List<Employee> employees, String status) {
        List<Employee> result = new ArrayList<>();
        for (Employee e : employees) {
            if (status.equals(e.getStatus())) {
                result.add(e);
            }
        }
        return result;
    }

    public Map<String, Double> sumSalaryByDepartment(List<Employee> employees) {
        Map<String, Double> result = new HashMap<>();
        for (Employee e : employees) {
            result.merge(e.getDepartmentId(), e.getSalary(), Double::sum);
        }
        return result;
    }
}
