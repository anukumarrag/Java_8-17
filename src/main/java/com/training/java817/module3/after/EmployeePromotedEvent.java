package com.training.java817.module3.after;

/** Fired when an employee is promoted to a new title/salary band. */
public record EmployeePromotedEvent(
        String employeeId,
        String newTitle,
        double newSalary,
        String effectiveDate
) implements EmployeeEvent {}
