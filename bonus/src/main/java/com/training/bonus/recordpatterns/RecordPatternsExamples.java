package com.training.bonus.recordpatterns;

/**
 * =============================================================================
 * BONUS – RECORD PATTERNS (JEP 440, Java 21 GA)
 * =============================================================================
 *
 * THEORY
 * ------
 * Java 16 gave us Records (immutable data carriers with auto-generated
 * accessor methods). Java 16 also gave us Pattern Matching for instanceof
 * (bind a type pattern variable).
 *
 * Java 21 (JEP 440) combines the two: you can DECONSTRUCT a record inline
 * inside instanceof or switch, binding its components directly as variables.
 *
 * SYNTAX
 * ------
 *   if (obj instanceof Point(int x, int y)) {
 *       // x and y are in scope; no need to call obj.x() / obj.y()
 *   }
 *
 * NESTED DECONSTRUCTION
 * ---------------------
 *   if (shape instanceof Circle(Point(int cx, int cy), int r)) {
 *       // cx, cy, r are all in scope
 *   }
 *
 * PROBLEM SOLVED
 * --------------
 * 1. Eliminates repetitive accessor calls after an instanceof check.
 * 2. Enables deep, nested destructuring in a single expression.
 * 3. Pairs with switch patterns for elegant algebraic-data-type handling.
 * 4. Reduces local variables (no intermediate p.x() calls everywhere).
 *
 * REQUIRES: Java 21
 */
public class RecordPatternsExamples {

    // =========================================================================
    // Domain model – nested records
    // =========================================================================

    public record Money(double amount, String currency) {}
    public record Address(String street, String city, String country) {}
    public record Counterparty(String id, String name, Address address) {}
    public record Trade(String id, String symbol, Money notional, Counterparty counterparty) {}

    // =========================================================================
    // BEFORE – Manual accessor calls (Java 17)
    // =========================================================================

    public String describeTradeCity_Before(Object obj) {
        if (obj instanceof Trade t) {
            Counterparty cp = t.counterparty();
            if (cp != null) {
                Address addr = cp.address();
                if (addr != null) {
                    return "Trade " + t.id() + " is in " + addr.city();
                }
            }
        }
        return "Unknown";
    }

    // =========================================================================
    // AFTER – Record patterns (Java 21)
    // =========================================================================

    /**
     * Record pattern in instanceof: deconstruct Trade and access id directly.
     */
    public String describeTrade(Object obj) {
        if (obj instanceof Trade(String id, String symbol, Money notional, Counterparty cp)) {
            return "Trade %s: %s @ %.2f %s".formatted(id, symbol, notional.amount(), notional.currency());
        }
        return "Not a trade";
    }

    /**
     * Nested record pattern: deconstruct Trade AND its nested Money record.
     * city is bound without a single explicit accessor call.
     */
    public String describeTradeCity_After(Object obj) {
        if (obj instanceof Trade(String id, _, _, Counterparty(_, _, Address(_, String city, _)))) {
            return "Trade " + id + " is in " + city;
        }
        return "Unknown";
    }

    /**
     * Record pattern in switch: clean, exhaustive handling of tagged union.
     */
    public sealed interface Shape permits Shape.Circle, Shape.Rectangle, Shape.Triangle {
        record Circle(double radius)                  implements Shape {}
        record Rectangle(double width, double height) implements Shape {}
        record Triangle(double base, double height)   implements Shape {}
    }

    public double area(Shape shape) {
        return switch (shape) {
            case Shape.Circle(double r)         -> Math.PI * r * r;
            case Shape.Rectangle(double w, double h) -> w * h;
            case Shape.Triangle(double b, double h)  -> 0.5 * b * h;
        };
    }

    // =========================================================================
    // Guarded record pattern
    // =========================================================================

    public String classifyPayment(Object obj) {
        return switch (obj) {
            case Money(double amt, String cur) when amt > 1_000_000 && "USD".equals(cur)
                    -> "Large USD payment: " + amt;
            case Money(double amt, String cur) when amt > 0
                    -> "Standard %s payment: %.2f".formatted(cur, amt);
            case Money(_, _)
                    -> "Zero or negative payment";
            default -> "Not a Money object";
        };
    }

    // =========================================================================
    // Unnamed pattern variables  (_)  – Java 21+
    // =========================================================================

    /**
     * Use _ to ignore components you don't need.
     * In Java 21 _ is a preview feature (JEP 443); GA in Java 22 (JEP 456).
     */
    public String extractSymbol(Object obj) {
        // _ ignores components we don't care about
        if (obj instanceof Trade(String id, String symbol, _, _)) {
            return id + ":" + symbol;
        }
        return "N/A";
    }

    // =========================================================================
    // Real-world: event processing with record patterns
    // =========================================================================

    public sealed interface TradeEvent permits TradeEvent.Created, TradeEvent.Priced {
        record Created(String tradeId, String symbol, double quantity) implements TradeEvent {}
        record Priced(String tradeId, Money price)                     implements TradeEvent {}
    }

    public String processEvent(TradeEvent event) {
        return switch (event) {
            case TradeEvent.Created(String id, String sym, double qty)
                    -> "New trade %s: %s x %.0f".formatted(id, sym, qty);
            case TradeEvent.Priced(String id, Money(double amt, String cur))
                    -> "Trade %s priced at %.2f %s".formatted(id, amt, cur);
        };
    }

    // demo main
    public static void main(String[] args) {
        RecordPatternsExamples ex = new RecordPatternsExamples();

        var addr  = new Address("123 Wall St", "New York", "US");
        var cp    = new Counterparty("CP001", "Acme Corp", addr);
        var trade = new Trade("T001", "AAPL", new Money(500_000, "USD"), cp);

        System.out.println("describeTrade    : " + ex.describeTrade(trade));
        System.out.println("describeCity     : " + ex.describeTradeCity_After(trade));
        System.out.println("extractSymbol    : " + ex.extractSymbol(trade));

        System.out.printf("Circle area   : %.4f%n", ex.area(new Shape.Circle(5)));
        System.out.printf("Rectangle area: %.2f%n", ex.area(new Shape.Rectangle(4, 6)));
        System.out.printf("Triangle area : %.2f%n", ex.area(new Shape.Triangle(3, 8)));

        System.out.println("classifyPayment large : " + ex.classifyPayment(new Money(2_000_000, "USD")));
        System.out.println("classifyPayment small : " + ex.classifyPayment(new Money(5_000, "EUR")));

        System.out.println("processEvent created : " +
                ex.processEvent(new TradeEvent.Created("T002", "MSFT", 100)));
        System.out.println("processEvent priced  : " +
                ex.processEvent(new TradeEvent.Priced("T002", new Money(415.0, "USD"))));
    }
}
