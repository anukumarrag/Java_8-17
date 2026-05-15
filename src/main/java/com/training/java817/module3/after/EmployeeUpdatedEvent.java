package com.training.java817.module3.after;

/** Fired when an employee's record is updated (e.g. salary adjustment). */
public record EmployeeUpdatedEvent(
        String employeeId,
        double newSalary,
        String updateReason
) implements EmployeeEvent {}
