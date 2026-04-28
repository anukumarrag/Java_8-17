package com.training.java817.module3.before;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * =============================================================================
 * MODULE 3 – WORKSHOP: BEFORE STATE
 * Legacy TransactionService – all four tasks (before)
 * =============================================================================
 *
 * This service intentionally uses:
 *  - String concatenation for SQL (Task 2)
 *  - Uncontrolled TradeEvent interface (Task 3)
 *  - instanceof + manual cast + if-else chain (Task 4)
 *  - Imperative loops instead of streams
 *
 * Workshop goal: refactor this into the modern after/ version.
 */
public class TransactionService {

    // =========================================================================
    // Task 2 (BEFORE): SQL built by string concatenation
    // =========================================================================

    /** Fragile SQL construction – easy to introduce space/newline bugs. */
    public String buildSearchQuery(String symbol, String status, LocalDate fromDate) {
        return "SELECT t.trade_id, t.symbol, t.notional, t.counterparty_id, " +
               "t.trader_id, t.currency, t.asset_class, t.trade_date, " +
               "t.settlement_date, t.status " +
               "FROM trades t " +
               "JOIN counterparties c ON c.id = t.counterparty_id " +
               "JOIN traders tr ON tr.id = t.trader_id " +
               "WHERE t.symbol = '" + symbol + "' " +
               "AND   t.status = '" + status + "' " +
               "AND   t.trade_date >= '" + fromDate + "' " +
               "ORDER BY t.trade_date DESC, t.notional DESC";
    }

    /** Aggregation query with concatenation. */
    public String buildAggregationQuery(String assetClass) {
        return "SELECT t.asset_class, " +
               "       COUNT(*) AS trade_count, " +
               "       SUM(t.notional) AS total_notional, " +
               "       AVG(t.notional) AS avg_notional " +
               "FROM trades t " +
               "WHERE t.asset_class = '" + assetClass + "' " +
               "  AND t.status NOT IN ('REJECTED', 'CANCELLED') " +
               "GROUP BY t.asset_class";
    }

    // =========================================================================
    // Task 3 (BEFORE): Uncontrolled event interface
    // =========================================================================

    /**
     * Any class can implement TradeEventLegacy.
     * The handler needs a catch-all default because the compiler cannot prove
     * exhaustiveness.
     */
    public interface TradeEventLegacy {
        String getTradeId();
        String getEventType();
    }

    public static class TradeCreatedLegacy implements TradeEventLegacy {
        private String tradeId; private String symbol;
        public TradeCreatedLegacy(String tradeId, String symbol) {
            this.tradeId = tradeId; this.symbol = symbol;
        }
        @Override public String getTradeId()    { return tradeId; }
        @Override public String getEventType()  { return "CREATED"; }
        public String getSymbol()               { return symbol; }
    }

    public static class TradeExecutedLegacy implements TradeEventLegacy {
        private String tradeId; private double executedPrice;
        public TradeExecutedLegacy(String tradeId, double price) {
            this.tradeId = tradeId; this.executedPrice = price;
        }
        @Override public String getTradeId()    { return tradeId; }
        @Override public String getEventType()  { return "EXECUTED"; }
        public double getExecutedPrice()        { return executedPrice; }
    }

    public static class TradeRejectedLegacy implements TradeEventLegacy {
        private String tradeId; private String reason;
        public TradeRejectedLegacy(String tradeId, String reason) {
            this.tradeId = tradeId; this.reason = reason;
        }
        @Override public String getTradeId()    { return tradeId; }
        @Override public String getEventType()  { return "REJECTED"; }
        public String getReason()               { return reason; }
    }

    // =========================================================================
    // Task 4 (BEFORE): if-else + manual casts + instanceof
    // =========================================================================

    /**
     * Handles events using the old pattern: instanceof check then manual cast.
     * Issues:
     *  1. Redundant casting on each branch.
     *  2. Default branch is defensive – silently ignores unknown events.
     *  3. No compiler guarantee that all cases are handled.
     */
    public String processEventLegacy(TradeEventLegacy event) {
        String result;
        if (event instanceof TradeCreatedLegacy) {
            TradeCreatedLegacy created = (TradeCreatedLegacy) event;   // redundant cast
            result = "Trade " + created.getTradeId() + " created for " + created.getSymbol();
        } else if (event instanceof TradeExecutedLegacy) {
            TradeExecutedLegacy executed = (TradeExecutedLegacy) event; // redundant cast
            result = "Trade " + executed.getTradeId() + " executed at " + executed.getExecutedPrice();
        } else if (event instanceof TradeRejectedLegacy) {
            TradeRejectedLegacy rejected = (TradeRejectedLegacy) event; // redundant cast
            result = "Trade " + rejected.getTradeId() + " rejected: " + rejected.getReason();
        } else {
            result = "Unknown event type: " + event.getEventType();
        }
        return result;
    }

    /**
     * Classify trade by notional – uses nested if-else chain.
     */
    public String classifyTrade_Before(TradeTransaction trade) {
        String classification;
        if (trade.getNotional() >= 100_000_000) {
            classification = "TIER1_LARGE";
        } else if (trade.getNotional() >= 10_000_000) {
            classification = "TIER2_MEDIUM";
        } else if (trade.getNotional() >= 1_000_000) {
            classification = "TIER3_SMALL";
        } else {
            classification = "TIER4_MICRO";
        }
        return classification;
    }

    /**
     * Get settlement days from asset class – old switch statement.
     */
    public int getSettlementDays_Before(String assetClass) {
        int days;
        switch (assetClass) {
            case "EQUITY":
                days = 2;
                break;
            case "FIXED_INCOME":
                days = 1;
                break;
            case "FOREX":
                days = 2;
                break;
            case "COMMODITY":
                days = 5;
                break;
            default:
                days = 3;
        }
        return days;
    }

    // =========================================================================
    // Imperative data processing (BEFORE streams)
    // =========================================================================

    public List<TradeTransaction> filterByStatus(List<TradeTransaction> trades, String status) {
        List<TradeTransaction> result = new ArrayList<>();
        for (TradeTransaction t : trades) {
            if (status.equals(t.getStatus())) {
                result.add(t);
            }
        }
        return result;
    }

    public Map<String, Double> sumNotionalBySymbol(List<TradeTransaction> trades) {
        Map<String, Double> result = new HashMap<>();
        for (TradeTransaction t : trades) {
            result.merge(t.getSymbol(), t.getNotional(), Double::sum);
        }
        return result;
    }
}
