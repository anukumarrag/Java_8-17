package com.training.java817.module1.collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 1 – Collection Enhancements")
class CollectionEnhancementsExamplesTest {

    private CollectionEnhancementsExamples ex;

    @BeforeEach
    void setUp() { ex = new CollectionEnhancementsExamples(); }

    private List<CollectionEnhancementsExamples.Trade> sampleTrades() {
        return List.of(
                new CollectionEnhancementsExamples.Trade("T1", "AAPL", 100_000, "EXECUTED"),
                new CollectionEnhancementsExamples.Trade("T2", "AAPL", 200_000, "PENDING"),
                new CollectionEnhancementsExamples.Trade("T3", "MSFT", 300_000, "EXECUTED"),
                new CollectionEnhancementsExamples.Trade("T4", "MSFT", 150_000, "REJECTED")
        );
    }

    // ---- getOrDefault -------------------------------------------------------

    @Test
    @DisplayName("getOrDefault: returns value when key present")
    void getOrDefault_keyPresent_returnsValue() {
        Map<String, Integer> counts = new HashMap<>(Map.of("AAPL", 5));
        assertEquals(5, ex.getTradeCount_After(counts, "AAPL"));
    }

    @Test
    @DisplayName("getOrDefault: returns 0 when key absent")
    void getOrDefault_keyAbsent_returnsZero() {
        assertEquals(0, ex.getTradeCount_After(new HashMap<>(), "MSFT"));
    }

    // ---- computeIfAbsent (groupBySymbol) ------------------------------------

    @Test
    @DisplayName("groupBySymbol: groups trades under correct symbol keys")
    void groupBySymbol_producesCorrectGroups() {
        var grouped = ex.groupBySymbol_After(sampleTrades());
        assertEquals(2, grouped.size());
        assertEquals(2, grouped.get("AAPL").size());
        assertEquals(2, grouped.get("MSFT").size());
    }

    @Test
    @DisplayName("groupBySymbol: before and after produce same result")
    void groupBySymbol_beforeAndAfterMatchKeys() {
        var before = ex.groupBySymbol_Before(sampleTrades());
        var after  = ex.groupBySymbol_After(sampleTrades());
        assertEquals(before.keySet(), after.keySet());
    }

    // ---- merge (countByStatus) ----------------------------------------------

    @Test
    @DisplayName("countByStatus: counts each status correctly")
    void countByStatus_countsCorrectly() {
        var counts = ex.countByStatus_After(sampleTrades());
        assertEquals(2, counts.get("EXECUTED"));
        assertEquals(1, counts.get("PENDING"));
        assertEquals(1, counts.get("REJECTED"));
    }

    @Test
    @DisplayName("countByStatus: before and after produce same counts")
    void countByStatus_beforeAndAfterMatch() {
        assertEquals(ex.countByStatus_Before(sampleTrades()),
                     ex.countByStatus_After(sampleTrades()));
    }

    // ---- putIfAbsent --------------------------------------------------------

    @Test
    @DisplayName("registerDefaultDesks: adds missing desks without overwriting existing")
    void registerDefaultDesks_addsDefaultsWithoutOverwriting() {
        Map<String, String> desks = new HashMap<>(Map.of("EQUITY", "custom@bank.com"));
        ex.registerDefaultDesks(desks);
        assertEquals("custom@bank.com", desks.get("EQUITY"),        "existing value preserved");
        assertEquals("rates-desk@bank.com", desks.get("FIXED_INCOME"), "missing value added");
        assertEquals("fx-desk@bank.com",    desks.get("FOREX"),         "missing value added");
    }

    // ---- computeIfPresent ---------------------------------------------------

    @Test
    @DisplayName("doubleIfPresent: doubles the value when key exists")
    void doubleIfPresent_doublesExistingValue() {
        Map<String, Integer> scores = new HashMap<>(Map.of("risk", 10));
        ex.doubleIfPresent(scores, "risk");
        assertEquals(20, scores.get("risk"));
    }

    @Test
    @DisplayName("doubleIfPresent: does nothing when key absent")
    void doubleIfPresent_doesNothingForAbsentKey() {
        Map<String, Integer> scores = new HashMap<>();
        ex.doubleIfPresent(scores, "risk");
        assertFalse(scores.containsKey("risk"));
    }

    // ---- replaceAll ---------------------------------------------------------

    @Test
    @DisplayName("normaliseSymbols: trims and uppercases all values")
    void normaliseSymbols_trimsAndUppercases() {
        Map<String, String> symbols = new HashMap<>(Map.of("k1", "  aapl  ", "k2", "msft"));
        ex.normaliseSymbols(symbols);
        assertEquals("AAPL", symbols.get("k1"));
        assertEquals("MSFT", symbols.get("k2"));
    }

    // ---- Java 9 factory methods ---------------------------------------------

    @Test
    @DisplayName("supportedCurrencies: returns immutable list with 5 entries")
    void supportedCurrencies_returnsImmutableList() {
        List<String> currencies = ex.supportedCurrencies();
        assertEquals(5, currencies.size());
        assertThrows(UnsupportedOperationException.class,
                () -> currencies.add("AUD"), "factory list should be immutable");
    }

    @Test
    @DisplayName("validStatuses: returns immutable set with correct values")
    void validStatuses_returnsImmutableSet() {
        Set<String> statuses = ex.validStatuses();
        assertTrue(statuses.contains("EXECUTED"));
        assertThrows(UnsupportedOperationException.class,
                () -> statuses.add("UNKNOWN"), "factory set should be immutable");
    }

    @Test
    @DisplayName("slaByStatus: Map.of returns correct SLA hours")
    void slaByStatus_returnsCorrectValues() {
        Map<String, Integer> sla = ex.slaByStatus();
        assertEquals(24, sla.get("DRAFT"));
        assertEquals(4,  sla.get("PENDING"));
        assertEquals(1,  sla.get("EXECUTED"));
        assertEquals(0,  sla.get("SETTLED"));
        assertThrows(UnsupportedOperationException.class,
                () -> sla.put("NEW", 99), "factory map should be immutable");
    }

    @Test
    @DisplayName("deskRoutingTable: Map.ofEntries supports more than 10 pairs")
    void deskRoutingTable_supportsMoreThan10Pairs() {
        Map<String, String> routing = ex.deskRoutingTable();
        assertTrue(routing.size() > 10, "ofEntries supports > 10 pairs");
        assertEquals("equity-desk@bank.com", routing.get("EQUITY"));
        assertEquals("options-desk@bank.com", routing.get("OPTION"));
    }

    // ---- copyOf -------------------------------------------------------------

    @Test
    @DisplayName("snapshotSymbols: immutable copy does not reflect later mutations")
    void snapshotSymbols_doesNotReflectMutations() {
        var mutable = new java.util.ArrayList<>(List.of("AAPL", "MSFT"));
        var snapshot = ex.snapshotSymbols(mutable);
        mutable.add("GOOG");
        assertEquals(2, snapshot.size(), "snapshot should not reflect post-copy mutation");
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.add("TSLA"), "copyOf list should be immutable");
    }
}
