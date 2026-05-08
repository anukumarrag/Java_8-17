package com.training.bonus.sequencedcollections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Bonus – Sequenced Collections (Java 21)")
class SequencedCollectionsExamplesTest {

    private SequencedCollectionsExamples ex;

    @BeforeEach
    void setUp() { ex = new SequencedCollectionsExamples(); }

    private List<SequencedCollectionsExamples.Trade> trades() {
        return new ArrayList<>(List.of(
                new SequencedCollectionsExamples.Trade("T1", "AAPL", 100_000),
                new SequencedCollectionsExamples.Trade("T2", "MSFT", 200_000),
                new SequencedCollectionsExamples.Trade("T3", "GOOG", 300_000)
        ));
    }

    // ---- getFirst / getLast -------------------------------------------------

    @Test
    @DisplayName("getFirstTrade: matches get(0)")
    void getFirstTrade_matchesGet0() {
        var list = trades();
        assertEquals(list.get(0).id(), ex.getFirstTrade(list).id());
        assertEquals(ex.getFirstTrade_Before(list).id(), ex.getFirstTrade(list).id());
    }

    @Test
    @DisplayName("getLastTrade: matches get(size-1)")
    void getLastTrade_matchesLastIndex() {
        var list = trades();
        assertEquals(list.get(list.size() - 1).id(), ex.getLastTrade(list).id());
        assertEquals(ex.getLastTrade_Before(list).id(), ex.getLastTrade(list).id());
    }

    @Test
    @DisplayName("getFirstTrade: throws on empty list")
    void getFirstTrade_emptyList_throws() {
        assertThrows(java.util.NoSuchElementException.class,
                () -> ex.getFirstTrade(new ArrayList<>()));
    }

    // ---- addFirst / addLast -------------------------------------------------

    @Test
    @DisplayName("prioritiseTrade: priority trade appears at index 0")
    void prioritiseTrade_insertedAtHead() {
        var priority = new SequencedCollectionsExamples.Trade("T0", "PRIORITY", 50_000);
        var result   = ex.prioritiseTrade(trades(), priority);
        assertEquals("T0",         result.get(0).id());
        assertEquals("T1",         result.get(1).id());
        assertEquals(4,            result.size());
    }

    @Test
    @DisplayName("appendTrade: trade appears at the end")
    void appendTrade_addedAtTail() {
        var extra  = new SequencedCollectionsExamples.Trade("T99", "TSLA", 999_000);
        var result = ex.appendTrade(trades(), extra);
        assertEquals("T99", result.get(result.size() - 1).id());
        assertEquals(4,     result.size());
    }

    // ---- reversed -----------------------------------------------------------

    @Test
    @DisplayName("reverseOrder: reversed view has last element first")
    void reverseOrder_lastBecomesFirst() {
        var reversed = ex.reverseOrder(trades());
        assertEquals("T3", reversed.getFirst().id(),
                "reversed view's first should be original list's last");
        assertEquals("T1", reversed.getLast().id());
    }

    @Test
    @DisplayName("mostRecentTrade: returns the last added trade")
    void mostRecentTrade_returnsLast() {
        var list = trades();
        assertEquals(list.getLast().id(), ex.mostRecentTrade(list).id());
    }

    // ---- SequencedSet (LinkedHashSet) ---------------------------------------

    @Test
    @DisplayName("firstAddedSymbol: returns insertion-order first element")
    void firstAddedSymbol_returnsFirst() {
        var set = new LinkedHashSet<>(List.of("AAPL", "MSFT", "GOOG"));
        assertEquals("AAPL", ex.firstAddedSymbol(set));
    }

    @Test
    @DisplayName("lastAddedSymbol: returns insertion-order last element (O(1))")
    void lastAddedSymbol_returnsLast() {
        var set = new LinkedHashSet<>(List.of("AAPL", "MSFT", "GOOG"));
        assertEquals("GOOG", ex.lastAddedSymbol(set));
    }

    @Test
    @DisplayName("reversedSymbols: first of reversed is last of original")
    void reversedSymbols_firstIsLastOfOriginal() {
        var set      = new LinkedHashSet<>(List.of("AAPL", "MSFT", "GOOG"));
        var reversed = ex.reversedSymbols(set);
        assertEquals("GOOG", reversed.getFirst());
    }

    // ---- SequencedMap (TreeMap) ---------------------------------------------

    @Test
    @DisplayName("cheapestAsset: returns entry with smallest key in TreeMap")
    void cheapestAsset_smallestKey() {
        var priceMap = new TreeMap<>(java.util.Map.of(
                "AAPL", 182.50, "GOOG", 172.30, "MSFT", 415.00));
        var entry = ex.cheapestAsset(priceMap);
        assertEquals("AAPL", entry.getKey(),
                "AAPL is alphabetically first in a TreeMap");
    }

    @Test
    @DisplayName("mostExpensiveAsset: returns entry with largest key in TreeMap")
    void mostExpensiveAsset_largestKey() {
        var priceMap = new TreeMap<>(java.util.Map.of(
                "AAPL", 182.50, "GOOG", 172.30, "MSFT", 415.00));
        var entry = ex.mostExpensiveAsset(priceMap);
        assertEquals("MSFT", entry.getKey(),
                "MSFT is alphabetically last in this map");
    }

    // ---- summarise ----------------------------------------------------------

    @Test
    @DisplayName("summarise: shows first, last, and count")
    void summarise_showsFirstLastCount() {
        String result = ex.summarise(List.of("alpha", "beta", "gamma"));
        assertTrue(result.contains("alpha"), "should contain first element");
        assertTrue(result.contains("gamma"), "should contain last element");
        assertTrue(result.contains("3"),     "should contain count");
    }

    @Test
    @DisplayName("summarise: empty collection returns EMPTY")
    void summarise_empty() {
        assertEquals("EMPTY", ex.summarise(List.of()));
    }
}
