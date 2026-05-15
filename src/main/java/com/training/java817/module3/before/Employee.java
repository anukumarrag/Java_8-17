package com.training.java817.module3.before;

import java.time.LocalDate;
import java.util.Objects;

/**
 * =============================================================================
 * MODULE 3 – WORKSHOP: BEFORE STATE
 * Task 1: Legacy Employee POJO (to be refactored into a Record)
 * =============================================================================
 *
 * Problem: This 100-line POJO exists solely to carry data. Every field requires:
 *  - A private final field declaration
 *  - A constructor parameter + assignment
 *  - A getter method
 *  - Manual equals/hashCode/toString
 *
 * Java 17 Records solve this at the language level.
 */
public class Employee {

    private final String    employeeId;
    private final String    name;
    private final double    salary;
    private final String    departmentId;
    private final String    managerId;
    private final String    location;
    private final String    jobTitle;
    private final LocalDate joinDate;
    private final LocalDate reviewDate;
    private final String    status;

    public Employee(String employeeId,
                    String name,
                    double salary,
                    String departmentId,
                    String managerId,
                    String location,
                    String jobTitle,
                    LocalDate joinDate,
                    LocalDate reviewDate,
                    String status) {
        this.employeeId   = Objects.requireNonNull(employeeId,   "employeeId required");
        this.name         = Objects.requireNonNull(name,         "name required");
        this.salary       = salary;
        this.departmentId = Objects.requireNonNull(departmentId, "departmentId required");
        this.managerId    = Objects.requireNonNull(managerId,    "managerId required");
        this.location     = Objects.requireNonNull(location,     "location required");
        this.jobTitle     = Objects.requireNonNull(jobTitle,     "jobTitle required");
        this.joinDate     = Objects.requireNonNull(joinDate,     "joinDate required");
        this.reviewDate   = Objects.requireNonNull(reviewDate,   "reviewDate required");
        this.status       = Objects.requireNonNull(status,       "status required");
    }

    public String    getEmployeeId()   { return employeeId; }
    public String    getName()         { return name; }
    public double    getSalary()       { return salary; }
    public String    getDepartmentId() { return departmentId; }
    public String    getManagerId()    { return managerId; }
    public String    getLocation()     { return location; }
    public String    getJobTitle()     { return jobTitle; }
    public LocalDate getJoinDate()     { return joinDate; }
    public LocalDate getReviewDate()   { return reviewDate; }
    public String    getStatus()       { return status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee that = (Employee) o;
        return Double.compare(that.salary, salary) == 0
                && Objects.equals(employeeId,   that.employeeId)
                && Objects.equals(name,         that.name)
                && Objects.equals(departmentId, that.departmentId)
                && Objects.equals(managerId,    that.managerId)
                && Objects.equals(location,     that.location)
                && Objects.equals(jobTitle,     that.jobTitle)
                && Objects.equals(joinDate,     that.joinDate)
                && Objects.equals(reviewDate,   that.reviewDate)
                && Objects.equals(status,       that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, name, salary, departmentId, managerId,
                location, jobTitle, joinDate, reviewDate, status);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "employeeId='" + employeeId + '\'' +
                ", name='" + name + '\'' +
                ", salary=" + salary +
                ", departmentId='" + departmentId + '\'' +
                ", managerId='" + managerId + '\'' +
                ", location='" + location + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", joinDate=" + joinDate +
                ", reviewDate=" + reviewDate +
                ", status='" + status + '\'' +
                '}';
    }
}
