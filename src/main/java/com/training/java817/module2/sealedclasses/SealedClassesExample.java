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

    public interface TradeEvent_Before {
        String tradeId();
        String eventType();
    }

    // Nothing stops an external class from implementing TradeEvent_Before.
    // The switch statement below requires a default because the compiler
    // cannot know all implementations.

    public String handleEvent_Before(TradeEvent_Before event) {
        if (event instanceof TradeCreated_Before) {
            return "Created: " + event.tradeId();
        } else if (event instanceof TradeExecuted_Before) {
            return "Executed: " + event.tradeId();
        } else if (event instanceof TradeRejected_Before) {
            return "Rejected: " + event.tradeId();
        } else {
            // Forced defensive default – might silently miss new event types!
            return "Unknown event";
        }
    }

    public static class TradeCreated_Before  implements TradeEvent_Before {
        private final String tradeId;
        public TradeCreated_Before(String id) { this.tradeId = id; }
        @Override public String tradeId()    { return tradeId; }
        @Override public String eventType()  { return "CREATED"; }
    }

    public static class TradeExecuted_Before implements TradeEvent_Before {
        private final String tradeId;
        public TradeExecuted_Before(String id) { this.tradeId = id; }
        @Override public String tradeId()    { return tradeId; }
        @Override public String eventType()  { return "EXECUTED"; }
    }

    public static class TradeRejected_Before implements TradeEvent_Before {
        private final String tradeId;
        private final String reason;
        public TradeRejected_Before(String id, String reason) {
            this.tradeId = id; this.reason = reason;
        }
        @Override public String tradeId()    { return tradeId; }
        @Override public String eventType()  { return "REJECTED"; }
        public String reason()               { return reason; }
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
    public sealed interface TradeEvent
            permits TradeCreated, TradeUpdated, TradeExecuted, TradeRejected {
        String tradeId();
    }

    /** final record – cannot be sub-classed further. */
    public record TradeCreated(String tradeId, String symbol, double notional)
            implements TradeEvent {}

    public record TradeUpdated(String tradeId, double newNotional)
            implements TradeEvent {}

    public record TradeExecuted(String tradeId, String executionVenue, double executedPrice)
            implements TradeEvent {}

    public record TradeRejected(String tradeId, String rejectionReason)
            implements TradeEvent {}

    // =========================================================================
    // Sealed class hierarchy (not interface)
    // =========================================================================

    /**
     * TradeState models the lifecycle of a trade as a sealed class tree.
     * Each state carries only the data relevant to that state.
     */
    public abstract sealed class TradeState
            permits TradeState.Draft, TradeState.Pending, TradeState.Settled,
                    TradeState.Cancelled {

        public abstract String stateLabel();

        public final class Draft extends TradeState {
            private final String symbol;
            public Draft(String symbol) { this.symbol = symbol; }
            @Override public String stateLabel() { return "DRAFT[" + symbol + "]"; }
            public String symbol() { return symbol; }
        }

        public final class Pending extends TradeState {
            private final String confirmationRef;
            public Pending(String ref) { this.confirmationRef = ref; }
            @Override public String stateLabel() { return "PENDING[" + confirmationRef + "]"; }
            public String confirmationRef() { return confirmationRef; }
        }

        public final class Settled extends TradeState {
            private final double settledAmount;
            public Settled(double amount) { this.settledAmount = amount; }
            @Override public String stateLabel() { return "SETTLED[" + settledAmount + "]"; }
            public double settledAmount() { return settledAmount; }
        }

        public final class Cancelled extends TradeState {
            private final String reason;
            public Cancelled(String reason) { this.reason = reason; }
            @Override public String stateLabel() { return "CANCELLED[" + reason + "]"; }
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
    public String describeEvent(TradeEvent event) {
        if (event instanceof TradeCreated c) {
            return "New trade created: symbol=%s, notional=%.2f"
                    .formatted(c.symbol(), c.notional());
        } else if (event instanceof TradeUpdated u) {
            return "Trade updated: new notional=%.2f".formatted(u.newNotional());
        } else if (event instanceof TradeExecuted e) {
            return "Trade executed at %s on %s".formatted(e.executedPrice(), e.executionVenue());
        } else if (event instanceof TradeRejected r) {
            return "Trade rejected: " + r.rejectionReason();
        }
        // With a sealed interface the compiler guarantees we never reach here.
        throw new IllegalStateException("Unexpected event type: " + event);
    }

    // demo main
    public static void main(String[] args) {
        SealedClassesExample ex = new SealedClassesExample();

        TradeEvent[] events = {
            new TradeCreated("T001", "AAPL", 500_000),
            new TradeExecuted("T001", "NYSE", 182.50),
            new TradeRejected("T002", "Insufficient funds"),
            new TradeUpdated("T003", 750_000)
        };

        for (TradeEvent e : events) {
            System.out.println(ex.describeEvent(e));
        }
    }
}
