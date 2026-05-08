package com.training.java817.module1.streams2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 1 – Stream API Enhancements (Java 9–16)")
class StreamEnhancementsExamplesTest {

    private StreamEnhancementsExamples ex;

    @BeforeEach
    void setUp() { ex = new StreamEnhancementsExamples(); }

    private List<StreamEnhancementsExamples.Trade> sampleTrades() {
        return List.of(
                new StreamEnhancementsExamples.Trade("T1", "AAPL", 100_000, "PENDING"),
                new StreamEnhancementsExamples.Trade("T2", "MSFT", 200_000, "PENDING"),
                new StreamEnhancementsExamples.Trade("T3", "GOOG", 300_000, "EXECUTED"),
                new StreamEnhancementsExamples.Trade("T4", "TSLA", 50_000,  "REJECTED")
        );
    }

    // ---- takeWhile ----------------------------------------------------------

    @Test
    @DisplayName("takeWhile: stops taking once price exceeds ceiling")
    void pricesBelowCeiling_stopsAtCeiling() {
        var result = ex.pricesBelowCeiling(
                List.of(100.0, 150.0, 180.0, 210.0, 250.0), 200.0);
        assertEquals(List.of(100.0, 150.0, 180.0), result);
    }

    @Test
    @DisplayName("takeWhile: all elements below ceiling – returns all")
    void pricesBelowCeiling_allBelowCeiling_returnsAll() {
        var result = ex.pricesBelowCeiling(List.of(10.0, 20.0, 30.0), 100.0);
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("takeWhile: first element exceeds ceiling – returns empty")
    void pricesBelowCeiling_firstExceedsCeiling_returnsEmpty() {
        var result = ex.pricesBelowCeiling(List.of(300.0, 10.0, 20.0), 200.0);
        assertTrue(result.isEmpty(), "takeWhile stops on first failing element");
    }

    // ---- dropWhile ----------------------------------------------------------

    @Test
    @DisplayName("dropWhile: drops all leading PENDING trades")
    void skipLeadingPending_dropsLeadingPending() {
        var result = ex.skipLeadingPending(sampleTrades());
        assertEquals(2, result.size(),                   "two non-PENDING trades remain");
        assertEquals("EXECUTED", result.get(0).status(), "first remaining is EXECUTED");
    }

    @Test
    @DisplayName("dropWhile: no leading PENDING – returns all")
    void skipLeadingPending_noPending_returnsAll() {
        var trades = List.of(
                new StreamEnhancementsExamples.Trade("T1", "AAPL", 100_000, "EXECUTED"),
                new StreamEnhancementsExamples.Trade("T2", "MSFT", 200_000, "PENDING")
        );
        assertEquals(2, ex.skipLeadingPending(trades).size());
    }

    // ---- iterate with predicate ---------------------------------------------

    @Test
    @DisplayName("pageOffsets: produces correct page offsets")
    void pageOffsets_correctOffsets() {
        var offsets = ex.pageOffsets(100, 350);
        assertEquals(List.of(0, 100, 200, 300), offsets);
    }

    @Test
    @DisplayName("pageOffsets: total equals page size – single page")
    void pageOffsets_singlePage() {
        var offsets = ex.pageOffsets(100, 100);
        assertEquals(List.of(0), offsets);
    }

    @Test
    @DisplayName("pageOffsets: zero total – empty list")
    void pageOffsets_zeroTotal_emptyList() {
        var offsets = ex.pageOffsets(100, 0);
        assertTrue(offsets.isEmpty());
    }

    // ---- ofNullable ---------------------------------------------------------

    @Test
    @DisplayName("collectNotes: only includes notes that exist in the map")
    void collectNotes_onlyExistingNotes() {
        var trades = sampleTrades();
        var notes  = Map.of("T1", "Important trade", "T3", "High value");
        var result = ex.collectNotes(trades, notes);
        assertEquals(2, result.size());
        assertTrue(result.contains("Important trade"));
        assertTrue(result.contains("High value"));
    }

    @Test
    @DisplayName("countNonNull: counts only non-null values in list")
    void countNonNull_countsOnlyNonNull() {
        var values = new java.util.ArrayList<String>();
        values.add("a");
        values.add(null);
        values.add("b");
        values.add(null);
        assertEquals(2L, ex.countNonNull(values));
    }

    // ---- Collectors.teeing --------------------------------------------------

    @Test
    @DisplayName("computeStats: sum and count are correct")
    void computeStats_correctSumAndCount() {
        var stats = ex.computeStats(sampleTrades());
        assertEquals(650_000.0, stats.sum(),  0.001);
        assertEquals(4L,        stats.count());
    }

    @Test
    @DisplayName("partitionTrades: correctly separates executed from others")
    void partitionTrades_separatesExecutedFromOthers() {
        var result = ex.partitionTrades(sampleTrades());
        assertEquals(1, result.executed().size());
        assertEquals(3, result.others().size());
        assertEquals("T3", result.executed().get(0).id());
    }

    // ---- Stream.toList() ----------------------------------------------------

    @Test
    @DisplayName("executedSymbols: returns distinct executed symbols as unmodifiable list")
    void executedSymbols_returnsCorrectSymbols() {
        var symbols = ex.executedSymbols(sampleTrades());
        assertEquals(List.of("GOOG"), symbols);
        assertThrows(UnsupportedOperationException.class,
                () -> symbols.add("EXTRA"), "toList() returns unmodifiable list");
    }

    // ---- eligibleSymbols (combined) -----------------------------------------

    @Test
    @DisplayName("eligibleSymbols: drops DRAFT prefix and respects notional ceiling")
    void eligibleSymbols_dropsDraftAndRespectsCeiling() {
        var trades = List.of(
                new StreamEnhancementsExamples.Trade("T0", "DRAFT_CO", 50_000,  "DRAFT"),
                new StreamEnhancementsExamples.Trade("T1", "AAPL",     100_000, "EXECUTED"),
                new StreamEnhancementsExamples.Trade("T2", "MSFT",     200_000, "EXECUTED"),
                new StreamEnhancementsExamples.Trade("T3", "GOOG",     400_000, "EXECUTED")  // above ceiling
        );
        var result = ex.eligibleSymbols(trades, 300_000);
        assertTrue(result.contains("AAPL"), "AAPL is eligible");
        assertTrue(result.contains("MSFT"), "MSFT is eligible");
        assertFalse(result.contains("DRAFT_CO"), "DRAFT should be skipped");
        assertFalse(result.contains("GOOG"),     "GOOG exceeds notional ceiling");
    }
}
