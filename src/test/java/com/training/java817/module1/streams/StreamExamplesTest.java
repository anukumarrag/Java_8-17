package com.training.java817.module1.streams;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 1 – Stream API")
class StreamExamplesTest {

    private StreamExamples ex;
    private List<StreamExamples.Trade> trades;

    @BeforeEach
    void setUp() {
        ex = new StreamExamples();
        trades = Arrays.asList(
                new StreamExamples.Trade("T001", "AAPL",   500_000, "EXECUTED"),
                new StreamExamples.Trade("T002", "MSFT", 1_200_000, "PENDING"),
                new StreamExamples.Trade("T003", "AAPL",   800_000, "EXECUTED"),
                new StreamExamples.Trade("T004", "GOOG", 3_000_000, "EXECUTED"),
                new StreamExamples.Trade("T005", "MSFT",   200_000, "REJECTED")
        );
    }

    @Test
    @DisplayName("Before: imperative sum of EXECUTED notional")
    void sumExecutedNotional_Before() {
        assertEquals(4_300_000.0, ex.sumExecutedNotional_Before(trades));
    }

    @Test
    @DisplayName("After: stream sum matches imperative sum")
    void sumExecutedNotional_After_matchesBefore() {
        assertEquals(
                ex.sumExecutedNotional_Before(trades),
                ex.sumExecutedNotional_After(trades));
    }

    @Test
    @DisplayName("Before: imperative distinct sorted symbols")
    void distinctSortedSymbols_Before() {
        List<String> result = ex.distinctSortedSymbols_Before(trades);
        assertEquals(List.of("AAPL", "GOOG", "MSFT"), result);
    }

    @Test
    @DisplayName("After: stream distinct sorted symbols matches before")
    void distinctSortedSymbols_After_matchesBefore() {
        assertEquals(
                ex.distinctSortedSymbols_Before(trades),
                ex.distinctSortedSymbols_After(trades));
    }

    @Test
    @DisplayName("groupByStatus returns correct group sizes")
    void groupByStatus_After_correctGroupSizes() {
        Map<String, List<StreamExamples.Trade>> grouped = ex.groupByStatus_After(trades);
        assertEquals(3, grouped.get("EXECUTED").size());
        assertEquals(1, grouped.get("PENDING").size());
        assertEquals(1, grouped.get("REJECTED").size());
    }

    @Test
    @DisplayName("flatMap flattens nested lists with distinct sorted result")
    void allSymbolsFromPortfolios_flattens() {
        List<List<String>> portfolios = Arrays.asList(
                Arrays.asList("AAPL", "MSFT"),
                Arrays.asList("GOOG", "AAPL")
        );
        List<String> result = ex.allSymbolsFromPortfolios(portfolios);
        assertEquals(List.of("AAPL", "GOOG", "MSFT"), result);
    }

    @Test
    @DisplayName("countPending returns correct count")
    void countPending_returnsOne() {
        assertEquals(1, ex.countPending(trades));
    }

    @Test
    @DisplayName("anyMatch: hasHighValueTrade detects 15M GOOG trade")
    void hasHighValueTrade_detectsGoogTrade() {
        List<StreamExamples.Trade> withHighValue = Arrays.asList(
                new StreamExamples.Trade("T001", "AAPL",   500_000, "EXECUTED"),
                new StreamExamples.Trade("T004", "GOOG", 15_000_000, "EXECUTED") // >10M
        );
        assertTrue(ex.hasHighValueTrade(withHighValue));
    }

    @Test
    @DisplayName("anyMatch: no high-value trade when all are small")
    void hasHighValueTrade_allSmall_returnsFalse() {
        List<StreamExamples.Trade> small = List.of(
                new StreamExamples.Trade("T1", "X", 50_000, "EXECUTED"));
        assertFalse(ex.hasHighValueTrade(small));
    }

    @Test
    @DisplayName("allMatch: not all trades are executed")
    void allTradesExecuted_returnsFalse_whenMixed() {
        assertFalse(ex.allTradesExecuted(trades));
    }

    @Test
    @DisplayName("allMatch: true when every trade is executed")
    void allTradesExecuted_returnsTrue_whenAllExecuted() {
        List<StreamExamples.Trade> allExecuted = List.of(
                new StreamExamples.Trade("T1", "AAPL", 100_000, "EXECUTED"),
                new StreamExamples.Trade("T2", "MSFT", 200_000, "EXECUTED")
        );
        assertTrue(ex.allTradesExecuted(allExecuted));
    }

    @Test
    @DisplayName("joining: tradeIdsCsv produces comma-separated IDs")
    void tradeIdsCsv_correctFormat() {
        String csv = ex.tradeIdsCsv(trades);
        assertTrue(csv.contains("T001"));
        assertTrue(csv.contains("T002"));
        assertTrue(csv.contains(", "));
    }

    @Test
    @DisplayName("toMap: indexById produces correct map")
    void indexById_correctMap() {
        Map<String, StreamExamples.Trade> map = ex.indexById(trades);
        assertEquals(5, map.size());
        assertEquals("AAPL", map.get("T001").symbol());
        assertEquals("GOOG", map.get("T004").symbol());
    }

    @Test
    @DisplayName("totalNotional: sum of all notionals")
    void totalNotional_sumsAll() {
        assertEquals(5_700_000.0, ex.totalNotional(trades));
    }
}
