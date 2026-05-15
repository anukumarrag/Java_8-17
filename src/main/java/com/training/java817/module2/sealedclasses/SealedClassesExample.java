package com.training.java817.module2.sealedclasses;

/**
 * =============================================================================
 * MODULE 2 – SEALED CLASSES (JEP 409, Java 17 GA)
 * =============================================================================
 *
 * THEORY
 * ------
 * A sealed class (or interface) restricts which classes may directly extend /
 * implement it.  The permitted subtypes are declared explicitly with the
 * `permits` clause.  Each permitted subtype must be:
 *   - in the same package (or module), AND
 *   - declared as one of:
 *       • final     – no further subclassing allowed
 *       • sealed    – further restricted subclassing
 *       • non-sealed – opens the hierarchy again (opt-out)
 *
 * PROBLEM SOLVED
 * --------------
 * 1. Closed-world modelling: the compiler and IDE know EVERY possible subtype.
 * 2. Enables exhaustive switch expressions (no default needed when all cases
 *    are covered) – pairs perfectly with pattern matching (Java 21).
 * 3. Provides fine-grained DDD-style domain modelling.
 * 4. Replaces fragile ad-hoc enum + instanceof hacks.
 *
 * WHERE TO USE
 * ------------
 * - Financial transaction lifecycle states
 * - Result / Either types (Success | Failure)
 * - Command / Event hierarchies in CQRS / event-sourcing architectures
 */
public class SealedClassesExample {

    // =========================================================================
    // BEFORE – Open interface: any class can implement it (no control)
    // =========================================================================

    public interface EmployeeEvent_Before {
        String employeeId();
        String eventType();
    }

    // Nothing stops an external class from implementing EmployeeEvent_Before.
    // The switch statement below requires a default because the compiler
    // cannot know all implementations.

    public String handleEvent_Before(EmployeeEvent_Before event) {
        if (event instanceof EmployeeHired_Before) {
            return "Hired: " + event.employeeId();
        } else if (event instanceof EmployeePromoted_Before) {
            return "Promoted: " + event.employeeId();
        } else if (event instanceof EmployeeTerminated_Before) {
            return "Terminated: " + event.employeeId();
        } else {
            // Forced defensive default – might silently miss new event types!
            return "Unknown event";
        }
    }

    public static class EmployeeHired_Before implements EmployeeEvent_Before {
        private final String employeeId;
        public EmployeeHired_Before(String id) { this.employeeId = id; }
        @Override public String employeeId()  { return employeeId; }
        @Override public String eventType()   { return "HIRED"; }
    }

    public static class EmployeePromoted_Before implements EmployeeEvent_Before {
        private final String employeeId;
        public EmployeePromoted_Before(String id) { this.employeeId = id; }
        @Override public String employeeId()  { return employeeId; }
        @Override public String eventType()   { return "PROMOTED"; }
    }

    public static class EmployeeTerminated_Before implements EmployeeEvent_Before {
        private final String employeeId;
        private final String reason;
        public EmployeeTerminated_Before(String id, String reason) {
            this.employeeId = id; this.reason = reason;
        }
        @Override public String employeeId()  { return employeeId; }
        @Override public String eventType()   { return "TERMINATED"; }
        public String reason()                { return reason; }
    }

    // =========================================================================
    // AFTER – Sealed interface: only permitted implementations allowed
    // =========================================================================

    /**
     * The sealed keyword locks down the hierarchy.
     * `permits` explicitly lists every allowed implementation.
     *
     * Records are perfect permitted subtypes: they are final by default,
     * and they give us free equals/hashCode/toString.
     */
    public sealed interface EmployeeEvent
            permits EmployeeHired, EmployeeUpdated, EmployeePromoted, EmployeeTerminated {
        String employeeId();
    }

    /** final record – cannot be sub-classed further. */
    public record EmployeeHired(String employeeId, String name, double salary)
            implements EmployeeEvent {}

    public record EmployeeUpdated(String employeeId, double newSalary)
            implements EmployeeEvent {}

    public record EmployeePromoted(String employeeId, String newTitle, double newSalary)
            implements EmployeeEvent {}

    public record EmployeeTerminated(String employeeId, String terminationReason)
            implements EmployeeEvent {}

    // =========================================================================
    // Sealed class hierarchy (not interface)
    // =========================================================================

    /**
     * EmployeeState models the lifecycle of an employee as a sealed class tree.
     * Each state carries only the data relevant to that state.
     */
    public abstract sealed class EmployeeState
            permits EmployeeState.Applied, EmployeeState.Onboarding,
                    EmployeeState.Active, EmployeeState.Resigned {

        public abstract String stateLabel();

        public final class Applied extends EmployeeState {
            private final String name;
            public Applied(String name) { this.name = name; }
            @Override public String stateLabel() { return "APPLIED[" + name + "]"; }
            public String name() { return name; }
        }

        public final class Onboarding extends EmployeeState {
            private final String referenceCode;
            public Onboarding(String ref) { this.referenceCode = ref; }
            @Override public String stateLabel() { return "ONBOARDING[" + referenceCode + "]"; }
            public String referenceCode() { return referenceCode; }
        }

        public final class Active extends EmployeeState {
            private final double confirmedSalary;
            public Active(double salary) { this.confirmedSalary = salary; }
            @Override public String stateLabel() { return "ACTIVE[" + confirmedSalary + "]"; }
            public double confirmedSalary() { return confirmedSalary; }
        }

        public final class Resigned extends EmployeeState {
            private final String reason;
            public Resigned(String reason) { this.reason = reason; }
            @Override public String stateLabel() { return "RESIGNED[" + reason + "]"; }
            public String reason() { return reason; }
        }
    }

    // =========================================================================
    // Handling sealed types
    // =========================================================================

    /**
     * Java 17: use instanceof pattern matching with if-else.
     * Java 21 (future): switch expressions become fully exhaustive for sealed types.
     */
    public String describeEvent(EmployeeEvent event) {
        if (event instanceof EmployeeHired h) {
            return "New employee hired: name=%s, salary=%.2f"
                    .formatted(h.name(), h.salary());
        } else if (event instanceof EmployeeUpdated u) {
            return "Employee updated: new salary=%.2f".formatted(u.newSalary());
        } else if (event instanceof EmployeePromoted p) {
            return "Employee promoted to %s with salary=%.2f"
                    .formatted(p.newTitle(), p.newSalary());
        } else if (event instanceof EmployeeTerminated t) {
            return "Employee terminated: " + t.terminationReason();
        }
        // With a sealed interface the compiler guarantees we never reach here.
        throw new IllegalStateException("Unexpected event type: " + event);
    }

    // demo main
    public static void main(String[] args) {
        SealedClassesExample ex = new SealedClassesExample();

        EmployeeEvent[] events = {
            new EmployeeHired("E001", "Alice", 85_000),
            new EmployeePromoted("E001", "SENIOR_ENGINEER", 100_000),
            new EmployeeTerminated("E002", "Voluntary resignation"),
            new EmployeeUpdated("E003", 90_000)
        };

        for (EmployeeEvent e : events) {
            System.out.println(ex.describeEvent(e));
        }
    }
}
