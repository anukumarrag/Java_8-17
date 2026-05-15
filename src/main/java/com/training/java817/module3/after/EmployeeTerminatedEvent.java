package com.training.java817.module3.after;

/** Fired when an employee's contract is terminated. */
public record EmployeeTerminatedEvent(
        String employeeId,
        String terminationCode,
        String terminationReason
) implements EmployeeEvent {}
