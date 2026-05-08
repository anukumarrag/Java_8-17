package com.training.bonus.patternswitch;

/**
 * =============================================================================
 * BONUS – PATTERN MATCHING FOR SWITCH (JEP 441, Java 21 GA)
 * =============================================================================
 *
 * THEORY
 * ------
 * Java 16 brought Pattern Matching for instanceof (test + bind in one step).
 * Java 21 extends the same idea to SWITCH expressions and statements.
 *
 * NEW CAPABILITIES
 * ----------------
 *   1. Type patterns in switch:  case Integer i  -> ...
 *   2. Guarded patterns:         case Integer i when i > 0  -> ...
 *   3. null handling:            case null  -> ... (no NullPointerException)
 *   4. Exhaustiveness:           compiler enforces all subtypes are covered
 *                                for sealed interfaces (no default needed)
 *   5. Dominance check:          compiler rejects unreachable, dominated cases
 *
 * PROBLEM SOLVED
 * --------------
 * 1. Replaces long if-else instanceof chains with readable switch expressions.
 * 2. Null-safety: switch can explicitly handle null without a try-catch.
 * 3. Exhaustiveness: adding a new sealed subtype causes a compile error at
 *    every switch that doesn't handle it – impossible to silently miss a case.
 * 4. Guards replace manual nested-if conditions inside case blocks.
 *
 * COMPARED TO JAVA 17
 * --------------------
 * Java 17 (sealed classes) required:
 *   if (event instanceof TradeCreated c) { ... }
 *   else if (event instanceof TradeExecuted e) { ... }
 *   ...
 * Java 21:
 *   switch (event) {
 *       case TradeCreated  c -> ...
 *       case TradeExecuted e -> ...
 *   }
 *
 * REQUIRES: Java 21
 */
public class PatternMatchingSwitchExamples {

    // =========================================================================
    // Domain model
    // =========================================================================

    public sealed interface TradeEvent
            permits TradeEvent.Created, TradeEvent.Updated,
                    TradeEvent.Executed, TradeEvent.Rejected, TradeEvent.Settled {

        record Created(String tradeId, String symbol, double notional) implements TradeEvent {}
        record Updated(String tradeId, double newNotional)             implements TradeEvent {}
        record Executed(String tradeId, String venue, double price)    implements TradeEvent {}
        record Rejected(String tradeId, String reason)                 implements TradeEvent {}
        record Settled(String tradeId, double settledAmount)           implements TradeEvent {}
    }

    // =========================================================================
    // BEFORE – if-else instanceof chain (Java 17)
    // =========================================================================

    public String describe_Before(TradeEvent event) {
        if (event instanceof TradeEvent.Created c) {
            return "CREATED " + c.tradeId() + " sym=" + c.symbol();
        } else if (event instanceof TradeEvent.Updated u) {
            return "UPDATED " + u.tradeId() + " newNotional=" + u.newNotional();
        } else if (event instanceof TradeEvent.Executed e) {
            return "EXECUTED " + e.tradeId() + " at " + e.price();
        } else if (event instanceof TradeEvent.Rejected r) {
            return "REJECTED " + r.tradeId() + " reason=" + r.reason();
        } else if (event instanceof TradeEvent.Settled s) {
            return "SETTLED " + s.tradeId() + " amount=" + s.settledAmount();
        }
        throw new IllegalStateException("unexpected event: " + event);
    }

    // =========================================================================
    // AFTER – Pattern matching switch (Java 21)
    // =========================================================================

    /**
     * Switch expression with type patterns.
     * Exhaustive: covers all sealed subtypes → no default required.
     * Compiler error if a new subtype is added to TradeEvent without updating here.
     */
    public String describe_After(TradeEvent event) {
        return switch (event) {
            case TradeEvent.Created  c -> "CREATED "  + c.tradeId() + " sym="        + c.symbol();
            case TradeEvent.Updated  u -> "UPDATED "  + u.tradeId() + " newNotional=" + u.newNotional();
            case TradeEvent.Executed e -> "EXECUTED " + e.tradeId() + " at "         + e.price();
            case TradeEvent.Rejected r -> "REJECTED " + r.tradeId() + " reason="     + r.reason();
            case TradeEvent.Settled  s -> "SETTLED "  + s.tradeId() + " amount="     + s.settledAmount();
        };
    }

    // =========================================================================
    // Guarded patterns  (when clause)
    // =========================================================================

    /**
     * Guarded pattern: case Type var when condition.
     * Refines what a case matches without a nested if.
     */
    public String classifyByNotional(TradeEvent event) {
        return switch (event) {
            case TradeEvent.Created  c when c.notional() > 10_000_000 -> "LARGE_CREATION";
            case TradeEvent.Created  c when c.notional() > 1_000_000  -> "MEDIUM_CREATION";
            case TradeEvent.Created  c                                 -> "SMALL_CREATION";
            case TradeEvent.Executed e when e.price() > 500            -> "PREMIUM_EXECUTION";
            case TradeEvent.Executed e                                 -> "STANDARD_EXECUTION";
            default                                                    -> "OTHER_EVENT";
        };
    }

    // =========================================================================
    // Null handling in switch (Java 21)
    // =========================================================================

    /**
     * Before Java 21, passing null to a switch always threw NullPointerException.
     * Now you can explicitly handle null as a case label.
     */
    public String handleWithNull(TradeEvent event) {
        return switch (event) {
            case null                -> "NULL_EVENT";
            case TradeEvent.Created  c -> "CREATED:"  + c.tradeId();
            case TradeEvent.Executed e -> "EXECUTED:" + e.tradeId();
            default                    -> "OTHER";
        };
    }

    // =========================================================================
    // Pattern switch on general Object (heterogeneous dispatch)
    // =========================================================================

    /**
     * Switch can now dispatch on Object, replacing complex visitor/instanceof chains
     * when processing heterogeneous data (e.g., from JSON or an event bus).
     */
    public String formatValue(Object value) {
        return switch (value) {
            case Integer  i when i < 0        -> "negative int: " + i;
            case Integer  i                   -> "positive int: " + i;
            case Double   d                   -> "double: "       + String.format("%.2f", d);
            case String   s when s.isBlank()  -> "blank string";
            case String   s                   -> "string: "       + s.toUpperCase();
            case null                         -> "null";
            default                           -> "other: "        + value.getClass().getSimpleName();
        };
    }

    // =========================================================================
    // Practical use: trade audit routing
    // =========================================================================

    public record AuditEntry(String tradeId, String category, String detail) {}

    public AuditEntry toAuditEntry(TradeEvent event) {
        return switch (event) {
            case TradeEvent.Created  c -> new AuditEntry(c.tradeId(), "LIFECYCLE", "Created: " + c.symbol());
            case TradeEvent.Updated  u -> new AuditEntry(u.tradeId(), "AMENDMENT", "New notional: " + u.newNotional());
            case TradeEvent.Executed e -> new AuditEntry(e.tradeId(), "EXECUTION", "Venue: " + e.venue());
            case TradeEvent.Rejected r -> new AuditEntry(r.tradeId(), "REJECTION", "Reason: " + r.reason());
            case TradeEvent.Settled  s -> new AuditEntry(s.tradeId(), "SETTLEMENT", "Amount: " + s.settledAmount());
        };
    }

    // demo main
    public static void main(String[] args) {
        PatternMatchingSwitchExamples ex = new PatternMatchingSwitchExamples();

        TradeEvent[] events = {
            new TradeEvent.Created("T001",  "AAPL", 500_000),
            new TradeEvent.Executed("T001", "NYSE", 182.50),
            new TradeEvent.Rejected("T002", "Insufficient balance"),
            new TradeEvent.Updated("T003",  750_000),
            new TradeEvent.Settled("T001",  500_000)
        };

        System.out.println("=== describe_After ===");
        for (var e : events) System.out.println(ex.describe_After(e));

        System.out.println("\n=== classifyByNotional ===");
        System.out.println(ex.classifyByNotional(new TradeEvent.Created("T004", "GOOG", 15_000_000)));
        System.out.println(ex.classifyByNotional(new TradeEvent.Created("T005", "TSLA", 500_000)));

        System.out.println("\n=== formatValue ===");
        for (Object v : new Object[]{42, -7, 3.14, "hello", "", null, true}) {
            System.out.println(ex.formatValue(v));
        }
    }
}
