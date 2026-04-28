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

    public enum TradeStatus { DRAFT, PENDING, EXECUTED, SETTLED, REJECTED, CANCELLED }
    public enum AssetClass  { EQUITY, FIXED_INCOME, COMMODITY, FOREX, DERIVATIVE }

    // =========================================================================
    // BEFORE – Traditional switch statement (statement form, fall-through risk)
    // =========================================================================

    /** Map trade status to an SLA in hours – old style. */
    public int getSlaHours_Before(TradeStatus status) {
        int hours;
        switch (status) {
            case DRAFT:
                hours = 24;
                break;
            case PENDING:
                hours = 4;
                break;
            case EXECUTED:
                hours = 1;
                break;
            case SETTLED:
                hours = 0;
                break;
            case REJECTED:   // intentional fall-through … but easily accidental
            case CANCELLED:
                hours = 48;
                break;
            default:
                throw new IllegalArgumentException("Unknown status: " + status);
        }
        return hours;
    }

    /** Map asset class to a risk weight – old if-else chain. */
    public double getRiskWeight_Before(AssetClass ac) {
        if (ac == AssetClass.EQUITY) {
            return 1.0;
        } else if (ac == AssetClass.FIXED_INCOME) {
            return 0.5;
        } else if (ac == AssetClass.COMMODITY) {
            return 0.8;
        } else if (ac == AssetClass.FOREX) {
            return 0.6;
        } else if (ac == AssetClass.DERIVATIVE) {
            return 1.5;
        } else {
            throw new IllegalArgumentException("Unknown asset class: " + ac);
        }
    }

    // =========================================================================
    // AFTER – Switch expressions (arrow form)
    // =========================================================================

    /** Map trade status to SLA – clean expression form. */
    public int getSlaHours_After(TradeStatus status) {
        return switch (status) {
            case DRAFT      -> 24;
            case PENDING    -> 4;
            case EXECUTED   -> 1;
            case SETTLED    -> 0;
            case REJECTED,
                 CANCELLED  -> 48;   // multiple labels, no fall-through needed
        };
        // No default needed: enum is exhaustive; compiler error if a new value is added
    }

    /** Map asset class to risk weight – expression form. */
    public double getRiskWeight_After(AssetClass ac) {
        return switch (ac) {
            case EQUITY       -> 1.0;
            case FIXED_INCOME -> 0.5;
            case COMMODITY    -> 0.8;
            case FOREX        -> 0.6;
            case DERIVATIVE   -> 1.5;
        };
    }

    // =========================================================================
    // yield – multi-statement block arm that returns a value
    // =========================================================================

    public String formatStatusMessage_After(TradeStatus status) {
        return switch (status) {
            case DRAFT     -> "Trade is in draft state";
            case PENDING   -> "Trade is awaiting confirmation";
            case EXECUTED  -> "Trade has been executed";
            case SETTLED   -> "Trade has settled – all done";
            case REJECTED  -> {
                // block arm: use yield to return
                String base = "Trade was REJECTED";
                yield base + " – please review and resubmit";
            }
            case CANCELLED -> {
                String base = "Trade was CANCELLED";
                yield base + " – contact operations";
            }
        };
    }

    // =========================================================================
    // Switch expression used inline in method arguments
    // =========================================================================

    public void processWithPriority(TradeStatus status) {
        // The switch result flows directly into the method call
        logAtLevel(
            switch (status) {
                case EXECUTED, SETTLED -> "INFO";
                case REJECTED, CANCELLED -> "WARN";
                case DRAFT, PENDING    -> "DEBUG";
            },
            "Processing trade with status: " + status
        );
    }

    private void logAtLevel(String level, String message) {
        System.out.println("[" + level + "] " + message);
    }

    // =========================================================================
    // Traditional statement form still works (backwards compatible)
    // =========================================================================

    public void printStatus_Statement(TradeStatus status) {
        switch (status) {
            case EXECUTED -> System.out.println("Trade executed successfully");
            case REJECTED -> System.out.println("Trade rejected");
            default       -> System.out.println("Trade in state: " + status);
        }
    }

    // =========================================================================
    // Factory method using switch expression
    // =========================================================================

    public String buildNotificationChannel(AssetClass ac) {
        String channel = switch (ac) {
            case EQUITY, DERIVATIVE       -> "equity-desk@bank.com";
            case FIXED_INCOME             -> "rates-desk@bank.com";
            case COMMODITY                -> "commodity-desk@bank.com";
            case FOREX                    -> "fx-desk@bank.com";
        };
        return "mailto:" + channel;
    }

    // demo main
    public static void main(String[] args) {
        SwitchExpressionsExample ex = new SwitchExpressionsExample();
        for (TradeStatus s : TradeStatus.values()) {
            System.out.printf("%-12s SLA=%2d h  msg=%s%n",
                    s, ex.getSlaHours_After(s), ex.formatStatusMessage_After(s));
        }
        for (AssetClass ac : AssetClass.values()) {
            System.out.printf("%-15s weight=%.1f  channel=%s%n",
                    ac, ex.getRiskWeight_After(ac), ex.buildNotificationChannel(ac));
        }
    }
}
