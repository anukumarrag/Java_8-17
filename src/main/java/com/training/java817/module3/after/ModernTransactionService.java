package com.training.java817.module3.after;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * =============================================================================
 * MODULE 3 – WORKSHOP: AFTER STATE
 * ModernTransactionService – all four tasks (after)
 * =============================================================================
 *
 * Changes applied:
 *  Task 1: Uses TradeTransactionRecord (record) instead of TradeTransaction POJO.
 *  Task 2: SQL queries use text blocks instead of string concatenation.
 *  Task 3: TradeEvent is now a sealed interface with record implementations.
 *  Task 4: instanceof pattern matching + switch expressions replace if-else chains.
 */
public class ModernTransactionService {

    // =========================================================================
    // Task 2 (AFTER): Text block SQL queries
    // =========================================================================

    /**
     * Clean, formatted SQL using a text block.
     * Indentation is stripped automatically; %s placeholders are safe.
     */
    public String buildSearchQuery(String symbol, String status, LocalDate fromDate) {
        return """
                SELECT t.trade_id,
                       t.symbol,
                       t.notional,
                       t.counterparty_id,
                       t.trader_id,
                       t.currency,
                       t.asset_class,
                       t.trade_date,
                       t.settlement_date,
                       t.status
                FROM   trades t
                JOIN   counterparties c  ON c.id  = t.counterparty_id
                JOIN   traders        tr ON tr.id = t.trader_id
                WHERE  t.symbol     = '%s'
                  AND  t.status     = '%s'
                  AND  t.trade_date >= '%s'
                ORDER  BY t.trade_date DESC,
                          t.notional   DESC
                """.formatted(symbol, status, fromDate);
    }

    /**
     * Aggregation query – text block keeps complex SQL readable without noise.
     */
    public String buildAggregationQuery(String assetClass) {
        return """
                SELECT t.asset_class,
                       COUNT(*)       AS trade_count,
                       SUM(t.notional) AS total_notional,
                       AVG(t.notional) AS avg_notional
                FROM   trades t
                WHERE  t.asset_class = '%s'
                  AND  t.status NOT IN ('REJECTED', 'CANCELLED')
                GROUP  BY t.asset_class
                """.formatted(assetClass);
    }

    // =========================================================================
    // Task 3 + 4 (AFTER): Sealed events + pattern matching + switch expression
    // =========================================================================

    /**
     * Handle a sealed TradeEvent using pattern matching for instanceof.
     *
     * Key improvements over the legacy version:
     *  1. No redundant casts – the binding variable (c, u, e, r) is typed.
     *  2. Compiler can prove exhaustiveness – no silent "Unknown event" default.
     *  3. Each branch carries only the relevant data from the record.
     */
    public String processEvent(TradeEvent event) {
        if (event instanceof TradeCreatedEvent c) {
            return "Trade %s created: symbol=%s, notional=%.2f"
                    .formatted(c.tradeId(), c.symbol(), c.notional());
        } else if (event instanceof TradeUpdatedEvent u) {
            return "Trade %s updated: new notional=%.2f, reason=%s"
                    .formatted(u.tradeId(), u.newNotional(), u.updateReason());
        } else if (event instanceof TradeExecutedEvent e) {
            return "Trade %s executed at %.4f on %s (qty=%d)"
                    .formatted(e.tradeId(), e.executedPrice(),
                               e.executionVenue(), e.executedQuantity());
        } else if (event instanceof TradeRejectedEvent r) {
            return "Trade %s rejected [%s]: %s"
                    .formatted(r.tradeId(), r.rejectionCode(), r.rejectionReason());
        }
        // Sealed interface guarantees we never reach here; defensive throw for safety.
        throw new IllegalStateException("Unexpected event: " + event);
    }

    /**
     * Route events to the correct downstream system using pattern matching.
     *
     * NOTE: Switch expressions with type patterns (JEP 441) are a preview feature
     * in Java 17 and become GA in Java 21. Here we use instanceof pattern matching
     * (GA in Java 16) combined with a straightforward if-else chain, which is fully
     * supported in Java 17.
     */
    public String routeEvent(TradeEvent event) {
        if (event instanceof TradeCreatedEvent)  return "trade-creation-topic";
        if (event instanceof TradeUpdatedEvent)  return "trade-amendment-topic";
        if (event instanceof TradeExecutedEvent) return "execution-report-topic";
        if (event instanceof TradeRejectedEvent) return "rejection-alert-topic";
        // Sealed interface guarantees we never reach here
        throw new IllegalStateException("Unexpected event type: " + event);
    }

    /**
     * Classify a trade by asset class and notional size.
     *
     * Uses a String switch expression (GA since Java 14) for the asset-class
     * dimension, and standard if-else for the notional threshold dimension.
     * Guarded patterns in switch (when keyword) are a preview in Java 17 and
     * become GA in Java 21 – the equivalent logic here uses nested conditions.
     */
    public String classifyTrade(TradeTransactionRecord trade) {
        double n  = trade.notional();
        String ac = trade.assetClass();
        return switch (ac) {
            case "EQUITY"       -> n >= 100_000_000 ? "LARGE_EQUITY"    : "STANDARD_EQUITY";
            case "FIXED_INCOME" -> n >= 50_000_000  ? "LARGE_BOND"      : "STANDARD_BOND";
            default             -> n >= 10_000_000  ? "LARGE_OTHER"     : "STANDARD_OTHER";
        };
    }

    /**
     * Get settlement days using a compact switch expression (replaces 10-line switch).
     */
    public int getSettlementDays(String assetClass) {
        return switch (assetClass) {
            case "EQUITY"       -> 2;
            case "FIXED_INCOME" -> 1;
            case "FOREX"        -> 2;
            case "COMMODITY"    -> 5;
            default             -> 3;
        };
    }

    // =========================================================================
    // Stream-based data processing (replaces imperative loops)
    // =========================================================================

    public List<TradeTransactionRecord> filterByStatus(
            List<TradeTransactionRecord> trades, String status) {
        return trades.stream()
                .filter(t -> status.equals(t.status()))
                .collect(Collectors.toList());
    }

    public Map<String, Double> sumNotionalBySymbol(List<TradeTransactionRecord> trades) {
        return trades.stream()
                .collect(Collectors.groupingBy(
                        TradeTransactionRecord::symbol,
                        Collectors.summingDouble(TradeTransactionRecord::notional)));
    }

    public List<TradeTransactionRecord> topNByNotional(
            List<TradeTransactionRecord> trades, int n) {
        return trades.stream()
                .sorted((a, b) -> Double.compare(b.notional(), a.notional()))
                .limit(n)
                .collect(Collectors.toList());
    }

    // demo main
    public static void main(String[] args) {
        ModernTransactionService svc = new ModernTransactionService();

        System.out.println("=== Task 2: Text Block SQL ===");
        System.out.println(svc.buildSearchQuery("AAPL", "EXECUTED", LocalDate.now().minusDays(30)));

        System.out.println("=== Task 3 + 4: Sealed Events ===");
        TradeEvent[] events = {
            new TradeCreatedEvent("T001", "AAPL", 1_500_000, "CP01"),
            new TradeExecutedEvent("T001", "NYSE", 182.50, 8_000),
            new TradeRejectedEvent("T002", "RISK_LIMIT", "Exceeds daily limit"),
            new TradeUpdatedEvent("T003", 2_000_000, "Client request")
        };
        for (TradeEvent e : events) {
            System.out.println("  " + svc.processEvent(e));
            System.out.println("  -> route to: " + svc.routeEvent(e));
        }

        System.out.println("=== Task 1: Record ===");
        TradeTransactionRecord tr = TradeTransactionRecord.draft(
                "T004", "MSFT", 5_000_000, "CP02", "TR01", "USD", "EQUITY");
        System.out.println(tr);
        System.out.println("High value: " + tr.isHighValue());
        System.out.println("Classification: " + svc.classifyTrade(tr));
    }
}
