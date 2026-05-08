package com.training.bonus.unnamedvariables;

import java.util.List;

/**
 * =============================================================================
 * BONUS – UNNAMED VARIABLES AND PATTERNS (JEP 456, Java 22)
 * =============================================================================
 *
 * THEORY
 * ------
 * In Java code it is common to encounter situations where a variable or
 * catch block parameter is required by the language syntax but is NOT USED
 * in the body.  Writing a dummy name like `ignored`, `e`, or `unused` is noisy
 * and can be misleading.
 *
 * Java 22 (JEP 456, building on preview in Java 21 via JEP 443) introduces
 * the UNNAMED VARIABLE `_` (single underscore):
 *
 *   • Signals "this binding is intentionally not used".
 *   • Can appear multiple times in the same scope (unlike normal variables).
 *   • Supported in:
 *       – Local variable declarations
 *       – Catch clauses
 *       – For-each loops
 *       – try-with-resources
 *       – Lambda parameters
 *       – Pattern match bindings (instanceof and switch)
 *       – Record patterns (component wildcards)
 *
 * COMPANION: UNNAMED PATTERNS
 * ----------------------------
 * An unnamed pattern `_` in a case label matches any value of any type:
 *   case _  -> ...  // matches everything (like default, but for patterns)
 *
 * PROBLEM SOLVED
 * --------------
 * 1. Removes the cognitive overhead of meaningless variable names.
 * 2. Makes code intent clear: "I need to iterate but don't use the element".
 * 3. Improves readability of catch blocks that only log or rethrow.
 * 4. Allows compiler to optimise away unused bindings.
 *
 * STATUS
 * ------
 * • Preview: Java 21 (JEP 443)
 * • Finalized: Java 22 (JEP 456)
 *
 * REQUIRES: Java 22 (or Java 21 with --enable-preview)
 */
public class UnnamedVariablesExamples {

    // =========================================================================
    // Domain
    // =========================================================================

    public record Trade(String id, String symbol, double notional, String status) {}
    public sealed interface TradeEvent permits TradeEvent.Created, TradeEvent.Rejected {
        record Created(String id, String symbol, double notional) implements TradeEvent {}
        record Rejected(String id, String reason)                 implements TradeEvent {}
    }

    // =========================================================================
    // BEFORE – Named but unused variables (Java 21)
    // =========================================================================

    /** Count trades – we don't care about the element, only the count. */
    public int countTrades_Before(List<Trade> trades) {
        int count = 0;
        for (Trade ignored : trades) {   // 'ignored' is a dummy name
            count++;
        }
        return count;
    }

    /** Catch clause – only propagating, not using the exception object. */
    public boolean isValidJson_Before(String json) {
        try {
            parseJson(json);
            return true;
        } catch (Exception e) {   // 'e' is never used
            return false;
        }
    }

    // =========================================================================
    // AFTER – Unnamed variables with _ (Java 22)
    // =========================================================================

    /** _ in for-each: explicitly signals the element is not used. */
    public int countTrades_After(List<Trade> trades) {
        int count = 0;
        for (var _ : trades) {   // unnamed variable – intent is clear
            count++;
        }
        return count;
    }

    /** _ in catch: exception is intentionally swallowed. */
    public boolean isValidJson_After(String json) {
        try {
            parseJson(json);
            return true;
        } catch (Exception _) {   // unnamed catch – we only care about success/failure
            return false;
        }
    }

    /** _ in try-with-resources when the resource itself is not used. */
    public boolean resourceObtainable(String resourceId) {
        try (var _ = acquireResource(resourceId)) {   // we only need the side-effect of opening
            return true;
        } catch (Exception _) {
            return false;
        }
    }

    /** Multiple _ in a pattern match: ignore the components we don't need. */
    public String extractId(Object obj) {
        if (obj instanceof Trade(String id, _, _, _)) {
            return id;
        }
        return "N/A";
    }

    /** _ in switch record pattern: destructure only what you need. */
    public String processEvent(TradeEvent event) {
        return switch (event) {
            case TradeEvent.Created(String id, String symbol, _)
                    -> "CREATED: " + id + " " + symbol;
            case TradeEvent.Rejected(String id, _)
                    -> "REJECTED: " + id;
        };
    }

    /** _ as an unnamed lambda parameter. */
    public List<String> mapToIds(List<Trade> trades) {
        return trades.stream()
                .map(t -> t.id())   // before: t is named
                .toList();
    }

    /** Multiple _ variables in the same block (impossible with regular variables). */
    public int countMatches(List<Object> items) {
        int count = 0;
        for (var item : items) {
            if (item instanceof String _) count++;      // _ used for type check only
            else if (item instanceof Integer _) count++;// _ reused – perfectly legal
        }
        return count;
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void parseJson(String json) {
        if (json == null || (!json.startsWith("{") && !json.startsWith("["))) {
            throw new IllegalArgumentException("Not valid JSON: " + json);
        }
    }

    private AutoCloseable acquireResource(String id) {
        return () -> {};  // no-op release
    }

    // demo main
    public static void main(String[] args) {
        UnnamedVariablesExamples ex = new UnnamedVariablesExamples();

        var trades = List.of(
                new Trade("T1", "AAPL", 100_000, "EXECUTED"),
                new Trade("T2", "MSFT", 200_000, "PENDING")
        );

        System.out.println("Count           : " + ex.countTrades_After(trades));
        System.out.println("Valid JSON {}   : " + ex.isValidJson_After("{}"));
        System.out.println("Invalid JSON    : " + ex.isValidJson_After("not json"));
        System.out.println("extractId       : " + ex.extractId(trades.get(0)));

        var event1 = new TradeEvent.Created("T3", "GOOG", 500_000);
        var event2 = new TradeEvent.Rejected("T4", "Insufficient funds");
        System.out.println("processEvent    : " + ex.processEvent(event1));
        System.out.println("processEvent    : " + ex.processEvent(event2));

        var mixed = List.of("hello", 42, "world", 3.14, 99);
        System.out.println("countMatches    : " + ex.countMatches(mixed));
    }
}
