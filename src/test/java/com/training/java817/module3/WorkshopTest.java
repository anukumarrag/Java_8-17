package com.training.java817.module3;

import com.training.java817.module3.after.*;
import com.training.java817.module3.before.TradeTransaction;
import com.training.java817.module3.before.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 3 – Workshop: Before vs After")
class WorkshopTest {

    private TransactionService     legacySvc;
    private ModernTransactionService modernSvc;

    private static final LocalDate TODAY       = LocalDate.of(2024, 6, 1);
    private static final LocalDate SETTLEMENT  = TODAY.plusDays(2);

    @BeforeEach
    void setUp() {
        legacySvc  = new TransactionService();
        modernSvc  = new ModernTransactionService();
    }

    // =========================================================================
    // Task 1: POJO vs Record
    // =========================================================================

    @Test
    @DisplayName("Task 1: TradeTransaction POJO has same data as Record")
    void task1_pojoAndRecord_containSameData() {
        TradeTransaction pojo = new TradeTransaction(
                "T001", "AAPL", 1_500_000.0, "CP01", "TR01",
                "USD", "EQUITY", TODAY, SETTLEMENT, "EXECUTED");

        TradeTransactionRecord record = new TradeTransactionRecord(
                "T001", "AAPL", 1_500_000.0, "CP01", "TR01",
                "USD", "EQUITY", TODAY, SETTLEMENT, "EXECUTED");

        assertEquals(pojo.getTradeId(),        record.tradeId());
        assertEquals(pojo.getSymbol(),         record.symbol());
        assertEquals(pojo.getNotional(),       record.notional());
        assertEquals(pojo.getCounterpartyId(), record.counterpartyId());
        assertEquals(pojo.getStatus(),         record.status());
    }

    @Test
    @DisplayName("Task 1: Record isHighValue() returns true for 1.5M notional")
    void task1_record_isHighValue() {
        TradeTransactionRecord record = new TradeTransactionRecord(
                "T001", "AAPL", 1_500_000.0, "CP01", "TR01",
                "USD", "EQUITY", TODAY, SETTLEMENT, "EXECUTED");
        assertTrue(record.isHighValue());
    }

    @Test
    @DisplayName("Task 1: Record draft() factory sets DRAFT status")
    void task1_record_draftFactory() {
        TradeTransactionRecord draft = TradeTransactionRecord.draft(
                "T002", "MSFT", 500_000.0, "CP02", "TR02", "USD", "EQUITY");
        assertEquals("DRAFT", draft.status());
    }

    @Test
    @DisplayName("Task 1: Record withStatus() returns new record with updated status")
    void task1_record_withStatus() {
        TradeTransactionRecord draft = TradeTransactionRecord.draft(
                "T003", "GOOG", 250_000.0, "CP03", "TR03", "USD", "EQUITY");
        TradeTransactionRecord executed = draft.withStatus("EXECUTED");
        assertEquals("EXECUTED", executed.status());
        assertEquals(draft.tradeId(), executed.tradeId());  // immutable copy
    }

    @Test
    @DisplayName("Task 1: Record negative notional throws IllegalArgumentException")
    void task1_record_negativeNotional_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new TradeTransactionRecord("T1", "AAPL", -1.0, "CP1", "TR1",
                        "USD", "EQUITY", TODAY, SETTLEMENT, "DRAFT"));
    }

    // =========================================================================
    // Task 2: SQL Text Block
    // =========================================================================

    @Test
    @DisplayName("Task 2: Before/After SQL contain same core clauses")
    void task2_sqlBeforeAfter_sameCoreClauses() {
        String before = legacySvc.buildSearchQuery("AAPL", "EXECUTED", TODAY);
        String after  = modernSvc.buildSearchQuery("AAPL", "EXECUTED", TODAY);

        for (String clause : new String[]{"SELECT", "FROM", "WHERE", "ORDER"}) {
            assertTrue(before.contains(clause), "before missing: " + clause);
            assertTrue(after.contains(clause),  "after missing: "  + clause);
        }
        assertTrue(before.contains("AAPL"));
        assertTrue(after.contains("AAPL"));
    }

    @Test
    @DisplayName("Task 2: Aggregation query Before/After both contain GROUP BY")
    void task2_aggregationQuery_containsGroupBy() {
        String before = legacySvc.buildAggregationQuery("EQUITY");
        String after  = modernSvc.buildAggregationQuery("EQUITY");
        assertTrue(before.contains("GROUP"));
        assertTrue(after.contains("GROUP"));
    }

    // =========================================================================
    // Task 3: Sealed Events
    // =========================================================================

    @Test
    @DisplayName("Task 3: TradeCreatedEvent is-a TradeEvent")
    void task3_tradeCreatedEvent_isTradeEvent() {
        TradeEvent event = new TradeCreatedEvent("T001", "AAPL", 1_000_000, "CP01");
        assertInstanceOf(TradeEvent.class, event);
    }

    @Test
    @DisplayName("Task 3: processEvent describes creation correctly")
    void task3_processEvent_creation() {
        TradeEvent event = new TradeCreatedEvent("T001", "AAPL", 500_000, "CP01");
        String desc = modernSvc.processEvent(event);
        assertTrue(desc.contains("T001"));
        assertTrue(desc.contains("AAPL"));
    }

    @Test
    @DisplayName("Task 3: processEvent describes rejection correctly")
    void task3_processEvent_rejection() {
        TradeEvent event = new TradeRejectedEvent("T002", "RISK_LIMIT", "Over daily limit");
        String desc = modernSvc.processEvent(event);
        assertTrue(desc.contains("T002"));
        assertTrue(desc.contains("Over daily limit"));
    }

    @Test
    @DisplayName("Task 3: processEvent describes execution correctly")
    void task3_processEvent_execution() {
        TradeEvent event = new TradeExecutedEvent("T003", "LSE", 182.50, 10_000);
        String desc = modernSvc.processEvent(event);
        assertTrue(desc.contains("T003"));
        assertTrue(desc.contains("LSE"));
    }

    @Test
    @DisplayName("Task 3: processEvent describes update correctly")
    void task3_processEvent_update() {
        TradeEvent event = new TradeUpdatedEvent("T004", 2_000_000, "Client request");
        String desc = modernSvc.processEvent(event);
        assertTrue(desc.contains("T004"));
        assertTrue(desc.contains("Client request"));
    }

    // =========================================================================
    // Task 4: Modern Control Flow
    // =========================================================================

    @Test
    @DisplayName("Task 4: routeEvent correctly routes each sealed event type")
    void task4_routeEvent_allTypes() {
        assertEquals("trade-creation-topic",
                modernSvc.routeEvent(new TradeCreatedEvent("T1", "A", 1, "CP1")));
        assertEquals("trade-amendment-topic",
                modernSvc.routeEvent(new TradeUpdatedEvent("T2", 2, "reason")));
        assertEquals("execution-report-topic",
                modernSvc.routeEvent(new TradeExecutedEvent("T3", "NYSE", 10.0, 100)));
        assertEquals("rejection-alert-topic",
                modernSvc.routeEvent(new TradeRejectedEvent("T4", "CODE", "reason")));
    }

    @Test
    @DisplayName("Task 4: getSettlementDays Before/After match for all asset classes")
    void task4_settlementDays_beforeAndAfterMatch() {
        for (String ac : new String[]{"EQUITY", "FIXED_INCOME", "FOREX", "COMMODITY", "UNKNOWN"}) {
            assertEquals(
                    legacySvc.getSettlementDays_Before(ac),
                    modernSvc.getSettlementDays(ac),
                    "Mismatch for asset class: " + ac);
        }
    }

    // =========================================================================
    // Stream-based data processing
    // =========================================================================

    private List<TradeTransactionRecord> sampleRecords() {
        return Arrays.asList(
                new TradeTransactionRecord("T1", "AAPL", 500_000,   "CP1", "TR1", "USD", "EQUITY",      TODAY, SETTLEMENT, "EXECUTED"),
                new TradeTransactionRecord("T2", "MSFT", 1_200_000, "CP2", "TR2", "USD", "EQUITY",      TODAY, SETTLEMENT, "PENDING"),
                new TradeTransactionRecord("T3", "AAPL", 800_000,   "CP1", "TR1", "USD", "EQUITY",      TODAY, SETTLEMENT, "EXECUTED"),
                new TradeTransactionRecord("T4", "GOOG", 3_000_000, "CP3", "TR3", "USD", "FIXED_INCOME",TODAY, SETTLEMENT, "EXECUTED")
        );
    }

    @Test
    @DisplayName("filterByStatus: returns only EXECUTED trades")
    void filterByStatus_executedOnly() {
        List<TradeTransactionRecord> executed = modernSvc.filterByStatus(sampleRecords(), "EXECUTED");
        assertEquals(3, executed.size());
        assertTrue(executed.stream().allMatch(t -> "EXECUTED".equals(t.status())));
    }

    @Test
    @DisplayName("sumNotionalBySymbol: AAPL totals 1.3M")
    void sumNotionalBySymbol_aaplTotal() {
        Map<String, Double> sums = modernSvc.sumNotionalBySymbol(sampleRecords());
        assertEquals(1_300_000.0, sums.get("AAPL"), 1e-9);
    }

    @Test
    @DisplayName("topNByNotional: top 2 trades are GOOG and MSFT")
    void topNByNotional_top2() {
        List<TradeTransactionRecord> top2 = modernSvc.topNByNotional(sampleRecords(), 2);
        assertEquals(2, top2.size());
        assertEquals("GOOG", top2.get(0).symbol());
        assertEquals("MSFT", top2.get(1).symbol());
    }
}
