package com.training.java817.module2.patternmatching;

import java.util.List;

/**
 * =============================================================================
 * MODULE 2 – PATTERN MATCHING FOR instanceof (JEP 394, Java 16 GA)
 * =============================================================================
 *
 * THEORY
 * ------
 * Before Java 16, when you tested `obj instanceof Foo`, you still had to cast
 * manually to use the value:
 *
 *   if (obj instanceof Foo) {
 *       Foo foo = (Foo) obj;   // redundant cast
 *       foo.doFoo();
 *   }
 *
 * Pattern matching combines the test and the binding in one step:
 *
 *   if (obj instanceof Foo foo) {   // test + bind 'foo' in scope
 *       foo.doFoo();
 *   }
 *
 * The bound variable is in scope for the true branch and for any &&-chained
 * conditions on the same line.
 *
 * PROBLEM SOLVED
 * --------------
 * 1. Eliminates redundant, error-prone casts.
 * 2. Makes control flow significantly cleaner and shorter.
 * 3. Prevents the "right cast in wrong branch" bug.
 * 4. Pairs naturally with sealed classes: guaranteed exhaustive branches.
 *
 * FUTURE (Java 21)
 * ----------------
 * Pattern matching is extended to switch expressions, allowing `switch(obj)`
 * with type patterns as case labels – a full algebraic deconstruction.
 */
public class PatternMatchingExamples {

    // =========================================================================
    // Domain – heterogeneous event hierarchy
    // =========================================================================

    public sealed interface AuditEvent
            permits PatternMatchingExamples.LoginEvent,
                    PatternMatchingExamples.TradeEvent,
                    PatternMatchingExamples.AlertEvent {}

    public record LoginEvent(String userId, String ipAddress, boolean success) implements AuditEvent {}
    public record TradeEvent(String tradeId, String symbol, double amount)     implements AuditEvent {}
    public record AlertEvent(String alertCode, String severity, String message) implements AuditEvent {}

    // =========================================================================
    // BEFORE – Classic instanceof + cast
    // =========================================================================

    public String describeEvent_Before(Object event) {
        if (event instanceof LoginEvent) {
            LoginEvent le = (LoginEvent) event;      // redundant cast
            return "LOGIN | user=" + le.userId() + " success=" + le.success();
        } else if (event instanceof TradeEvent) {
            TradeEvent te = (TradeEvent) event;      // redundant cast
            return "TRADE | id=" + te.tradeId() + " amount=" + te.amount();
        } else if (event instanceof AlertEvent) {
            AlertEvent ae = (AlertEvent) event;      // redundant cast
            return "ALERT | " + ae.severity() + " : " + ae.message();
        } else {
            return "UNKNOWN EVENT";
        }
    }

    // =========================================================================
    // AFTER – Pattern matching for instanceof
    // =========================================================================

    public String describeEvent_After(Object event) {
        if (event instanceof LoginEvent le) {
            return "LOGIN | user=" + le.userId() + " success=" + le.success();
        } else if (event instanceof TradeEvent te) {
            return "TRADE | id=" + te.tradeId() + " amount=" + te.amount();
        } else if (event instanceof AlertEvent ae) {
            return "ALERT | " + ae.severity() + " : " + ae.message();
        } else {
            return "UNKNOWN EVENT";
        }
    }

    // =========================================================================
    // Guard conditions with && (the binding is in scope on the right of &&)
    // =========================================================================

    public String describeHighValueTrade(Object event) {
        if (event instanceof TradeEvent te && te.amount() > 1_000_000) {
            return "HIGH-VALUE TRADE: " + te.tradeId() + " = " + te.amount();
        } else if (event instanceof TradeEvent te) {
            return "Standard trade: " + te.tradeId();
        }
        return "Not a trade event";
    }

    // =========================================================================
    // Negation pattern – negate with !
    // =========================================================================

    public boolean isNotLoginEvent(Object event) {
        // The bound variable is NOT in scope when instanceof returns false
        return !(event instanceof LoginEvent);
    }

    // =========================================================================
    // Processing a heterogeneous list
    // =========================================================================

    public void processAuditLog(List<AuditEvent> events) {
        for (AuditEvent event : events) {
            if (event instanceof LoginEvent le && !le.success()) {
                System.out.println("SECURITY ALERT: Failed login from " + le.ipAddress());
            } else if (event instanceof TradeEvent te && te.amount() > 5_000_000) {
                System.out.println("COMPLIANCE ALERT: Large trade " + te.tradeId());
            } else if (event instanceof AlertEvent ae && "CRITICAL".equals(ae.severity())) {
                System.out.println("CRITICAL ALERT: " + ae.message());
            } else {
                System.out.println("INFO: " + event);
            }
        }
    }

    // =========================================================================
    // Combining with records for structural decomposition
    // =========================================================================

    public record Money(double amount, String currency) {}
    public record Payment(Money money, String recipient) {}

    public String describePayment(Object obj) {
        // Pattern bind to Payment, then access the Money record's fields directly.
        // A second instanceof is not needed because p.money() is already typed as Money.
        if (obj instanceof Payment p && p.money() != null
                && "USD".equals(p.money().currency())) {
            return "USD payment of " + p.money().amount() + " to " + p.recipient();
        }
        return "Non-USD or non-payment";
    }

    // demo main
    public static void main(String[] args) {
        PatternMatchingExamples ex = new PatternMatchingExamples();

        Object[] events = {
            new LoginEvent("alice", "10.0.0.1", true),
            new TradeEvent("T001", "AAPL", 2_500_000),
            new AlertEvent("A001", "CRITICAL", "Circuit breaker tripped"),
            "Not an event"
        };

        System.out.println("=== BEFORE ===");
        for (Object e : events) {
            System.out.println(ex.describeEvent_Before(e));
        }

        System.out.println("=== AFTER ===");
        for (Object e : events) {
            System.out.println(ex.describeEvent_After(e));
        }

        System.out.println("=== HIGH VALUE ===");
        System.out.println(ex.describeHighValueTrade(new TradeEvent("T002", "MSFT", 500_000)));
        System.out.println(ex.describeHighValueTrade(new TradeEvent("T003", "GOOG", 5_000_000)));
    }
}
