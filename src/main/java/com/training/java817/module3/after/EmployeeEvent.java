package com.training.java817.module3.after;

/**
 * =============================================================================
 * MODULE 3 – WORKSHOP: AFTER STATE
 * Task 3: Sealed EmployeeEvent interface
 * =============================================================================
 *
 * What changed:
 *  - EmployeeEventLegacy interface is now `sealed`.
 *  - `permits` lists every allowed implementation – no surprise subclasses.
 *  - Implementations are records: final, immutable, no boilerplate.
 *  - The compiler can now prove exhaustiveness in pattern-matching switch.
 */
public sealed interface EmployeeEvent
        permits EmployeeHiredEvent, EmployeeUpdatedEvent,
                EmployeePromotedEvent, EmployeeTerminatedEvent {

    /** The employee this event relates to. */
    String employeeId();
}
