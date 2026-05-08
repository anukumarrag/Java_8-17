package com.training.java817.module1.var;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 1 – Local Variable Type Inference (var)")
class LocalVarInferenceExamplesTest {

    private LocalVarInferenceExamples ex;

    @BeforeEach
    void setUp() { ex = new LocalVarInferenceExamples(); }

    private List<LocalVarInferenceExamples.Trade> sampleTrades() {
        return List.of(
                new LocalVarInferenceExamples.Trade("T1", "AAPL", 500_000,   "EXECUTED"),
                new LocalVarInferenceExamples.Trade("T2", "MSFT", 1_500_000, "EXECUTED"),
                new LocalVarInferenceExamples.Trade("T3", "GOOG", 800_000,   "PENDING")
        );
    }

    // ---- groupByStatus -------------------------------------------------------

    @Test
    @DisplayName("groupByStatus: before and after produce identical results")
    void groupByStatus_beforeAndAfterMatch() {
        var trades = sampleTrades();
        assertEquals(ex.groupByStatus_Before(trades).keySet(),
                     ex.groupByStatus_After(trades).keySet());
    }

    @Test
    @DisplayName("groupByStatus_After: groups correctly using var")
    void groupByStatus_After_groupsCorrectly() {
        var grouped = ex.groupByStatus_After(sampleTrades());
        assertEquals(2, grouped.size());
        assertEquals(2, grouped.get("EXECUTED").size());
        assertEquals(1, grouped.get("PENDING").size());
    }

    // ---- sumHighValueTrades --------------------------------------------------

    @Test
    @DisplayName("sumHighValueTrades: only sums trades above 1M notional")
    void sumHighValueTrades_onlySumsAbove1M() {
        double sum = ex.sumHighValueTrades(sampleTrades());
        assertEquals(1_500_000.0, sum, 0.001,
                "only T2 (1.5M) should be counted; T1 (500k) and T3 (800k) are below");
    }

    @Test
    @DisplayName("sumHighValueTrades: returns 0 when no trades above threshold")
    void sumHighValueTrades_noneAboveThreshold_returnsZero() {
        var lowTrades = List.of(
                new LocalVarInferenceExamples.Trade("T1", "AAPL", 100_000, "EXECUTED")
        );
        assertEquals(0.0, ex.sumHighValueTrades(lowTrades), 0.001);
    }

    // ---- readLines -----------------------------------------------------------

    @Test
    @DisplayName("readLines: reads all lines from a multi-line string")
    void readLines_readsAllLines() throws Exception {
        var lines = ex.readLines("line1\nline2\nline3");
        assertEquals(3, lines.size());
        assertEquals("line1", lines.get(0));
        assertEquals("line3", lines.get(2));
    }

    @Test
    @DisplayName("readLines: single line returns one entry")
    void readLines_singleLine_returnsOneEntry() throws Exception {
        var lines = ex.readLines("hello");
        assertEquals(1, lines.size());
        assertEquals("hello", lines.get(0));
    }

    // ---- processSymbols (var in lambda) -------------------------------------

    @Test
    @DisplayName("processSymbols: strips, uppercases, and filters blank/null")
    void processSymbols_stripsAndUppercases() {
        var mutable = new ArrayList<String>();
        mutable.add("aapl");
        mutable.add(" msft ");
        mutable.add(null);
        mutable.add("");
        var result = ex.processSymbols(mutable);
        assertEquals(List.of("AAPL", "MSFT"), result);
    }

    // ---- buildNestedMap ------------------------------------------------------

    @Test
    @DisplayName("buildNestedMap: produces correct deeply nested structure")
    void buildNestedMap_correctStructure() {
        var map = ex.buildNestedMap();
        assertNotNull(map.get("equities"));
        assertEquals(List.of("AAPL", "MSFT"), map.get("equities").get("symbols"));
    }

    // ---- formatTrade ---------------------------------------------------------

    @Test
    @DisplayName("formatTrade: produces formatted string")
    void formatTrade_producesFormattedString() {
        String result = ex.formatTrade("T001", 1_500_000.0);
        assertEquals("TRD[T001]=1500000.0", result);
    }
}
