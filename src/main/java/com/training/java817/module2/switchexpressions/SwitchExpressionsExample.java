package com.training.java817.module2.switchexpressions;

/**
 * =============================================================================
 * MODULE 2 – SWITCH EXPRESSIONS (JEP 361, Java 14 GA)
 * =============================================================================
 *
 * THEORY
 * ------
 * Classic switch statements have two well-known pitfalls:
 *   1. Fall-through: execution continues past the matched case unless you
 *      write `break` – easy to forget.
 *   2. Statement-only: you cannot assign the result of a switch to a variable
 *      in a single expression.
 *
 * Java 14 introduced the arrow-case switch expression:
 *   • Arrow `->` replaces `:` and prevents fall-through.
 *   • Switch can now RETURN A VALUE (used as an expression).
 *   • Multiple labels per case: `case A, B, C ->`.
 *   • `yield` keyword for returning from a block arm.
 *
 * PROBLEM SOLVED
 * --------------
 * 1. No more missing-break fall-through bugs.
 * 2. Switch result can be used in assignments, return statements, or method args.
 * 3. Exhaustiveness: if the compiler can see all cases are covered (e.g. enum
 *    or sealed type), no default is required – missing a new enum value is a
 *    compile error.
 * 4. Reduces 20-line if-else chains to 3–4 readable lines.
 */
public class SwitchExpressionsExample {

    // =========================================================================
    // Domain enums
    // =========================================================================

    public enum EmployeeStatus { APPLIED, ONBOARDING, ACTIVE, ON_LEAVE, RESIGNED, TERMINATED }
    public enum Department     { ENGINEERING, MARKETING, SALES, FINANCE, HR }

    // =========================================================================
    // BEFORE – Traditional switch statement (statement form, fall-through risk)
    // =========================================================================

    /** Map employee status to an SLA in hours – old style. */
    public int getSlaHours_Before(EmployeeStatus status) {
        int hours;
        switch (status) {
            case APPLIED:
                hours = 48;
                break;
            case ONBOARDING:
                hours = 24;
                break;
            case ACTIVE:
                hours = 72;
                break;
            case ON_LEAVE:
                hours = 8;
                break;
            case RESIGNED:   // intentional fall-through … but easily accidental
            case TERMINATED:
                hours = 0;
                break;
            default:
                throw new IllegalArgumentException("Unknown status: " + status);
        }
        return hours;
    }

    /** Map department to a budget multiplier – old if-else chain. */
    public double getBudgetMultiplier_Before(Department dept) {
        if (dept == Department.ENGINEERING) {
            return 1.5;
        } else if (dept == Department.MARKETING) {
            return 1.2;
        } else if (dept == Department.SALES) {
            return 1.3;
        } else if (dept == Department.FINANCE) {
            return 1.0;
        } else if (dept == Department.HR) {
            return 0.8;
        } else {
            throw new IllegalArgumentException("Unknown department: " + dept);
        }
    }

    // =========================================================================
    // AFTER – Switch expressions (arrow form)
    // =========================================================================

    /** Map employee status to SLA – clean expression form. */
    public int getSlaHours_After(EmployeeStatus status) {
        return switch (status) {
            case APPLIED    -> 48;
            case ONBOARDING -> 24;
            case ACTIVE     -> 72;
            case ON_LEAVE   -> 8;
            case RESIGNED,
                 TERMINATED -> 0;   // multiple labels, no fall-through needed
        };
        // No default needed: enum is exhaustive; compiler error if a new value is added
    }

    /** Map department to budget multiplier – expression form. */
    public double getBudgetMultiplier_After(Department dept) {
        return switch (dept) {
            case ENGINEERING -> 1.5;
            case MARKETING   -> 1.2;
            case SALES       -> 1.3;
            case FINANCE     -> 1.0;
            case HR          -> 0.8;
        };
    }

    // =========================================================================
    // yield – multi-statement block arm that returns a value
    // =========================================================================

    public String formatStatusMessage_After(EmployeeStatus status) {
        return switch (status) {
            case APPLIED    -> "Employee application received";
            case ONBOARDING -> "Employee is currently onboarding";
            case ACTIVE     -> "Employee is active";
            case ON_LEAVE   -> "Employee is on leave";
            case RESIGNED   -> {
                // block arm: use yield to return
                String base = "Employee has RESIGNED";
                yield base + " – please review and resubmit if needed";
            }
            case TERMINATED -> {
                String base = "Employee was TERMINATED";
                yield base + " – contact HR operations";
            }
        };
    }

    // =========================================================================
    // Switch expression used inline in method arguments
    // =========================================================================

    public void processWithPriority(EmployeeStatus status) {
        // The switch result flows directly into the method call
        logAtLevel(
            switch (status) {
                case ACTIVE, ONBOARDING  -> "INFO";
                case RESIGNED, TERMINATED -> "WARN";
                case APPLIED, ON_LEAVE    -> "DEBUG";
            },
            "Processing employee with status: " + status
        );
    }

    private void logAtLevel(String level, String message) {
        System.out.println("[" + level + "] " + message);
    }

    // =========================================================================
    // Traditional statement form still works (backwards compatible)
    // =========================================================================

    public void printStatus_Statement(EmployeeStatus status) {
        switch (status) {
            case ACTIVE     -> System.out.println("Employee is active");
            case TERMINATED -> System.out.println("Employee terminated");
            default         -> System.out.println("Employee in state: " + status);
        }
    }

    // =========================================================================
    // Factory method using switch expression
    // =========================================================================

    public String buildNotificationChannel(Department dept) {
        String channel = switch (dept) {
            case ENGINEERING -> "engineering@company.com";
            case MARKETING   -> "marketing@company.com";
            case SALES       -> "sales@company.com";
            case FINANCE     -> "finance@company.com";
            case HR          -> "hr@company.com";
        };
        return "mailto:" + channel;
    }

    // demo main
    public static void main(String[] args) {
        SwitchExpressionsExample ex = new SwitchExpressionsExample();
        for (EmployeeStatus s : EmployeeStatus.values()) {
            System.out.printf("%-12s SLA=%2d h  msg=%s%n",
                    s, ex.getSlaHours_After(s), ex.formatStatusMessage_After(s));
        }
        for (Department d : Department.values()) {
            System.out.printf("%-15s multiplier=%.1f  channel=%s%n",
                    d, ex.getBudgetMultiplier_After(d), ex.buildNotificationChannel(d));
        }
    }
}
