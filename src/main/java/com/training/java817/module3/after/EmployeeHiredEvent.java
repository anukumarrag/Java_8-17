package com.training.java817.module3.after;

/** Fired when a new employee joins the organisation. */
public record EmployeeHiredEvent(
        String employeeId,
        String name,
        double salary,
        String departmentId
) implements EmployeeEvent {}
