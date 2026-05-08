package com.training.java817.module1.concurrent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 1 – CompletableFuture")
class CompletableFutureExamplesTest {

    private CompletableFutureExamples ex;

    @BeforeEach
    void setUp() { ex = new CompletableFutureExamples(); }

    // ---- fetchFormattedPrice ------------------------------------------------

    @Test
    @DisplayName("fetchFormattedPrice: formats symbol and price")
    void fetchFormattedPrice_containsSymbolAndPrice() {
        String result = ex.fetchFormattedPrice("AAPL").join();
        assertTrue(result.startsWith("AAPL"),      "should start with AAPL");
        assertTrue(result.contains("182.5"),        "should contain price 182.5");
    }

    @Test
    @DisplayName("fetchFormattedPrice: unknown symbol returns default price")
    void fetchFormattedPrice_unknownSymbol_usesDefault() {
        String result = ex.fetchFormattedPrice("UNKNOWN").join();
        assertTrue(result.contains("100.00"), "default price should be 100.00");
    }

    // ---- fetchTradeWithCounterparty -----------------------------------------

    @Test
    @DisplayName("fetchTradeWithCounterparty: result contains trade and CP info")
    void fetchTradeWithCounterparty_containsTradeAndCp() {
        String result = ex.fetchTradeWithCounterparty("T001").join();
        assertTrue(result.contains("TRADE:T001"), "should contain trade id");
        assertTrue(result.contains("CP:ACME"),    "should contain counterparty");
    }

    // ---- fetchSpread --------------------------------------------------------

    @Test
    @DisplayName("fetchSpread: result contains BID, ASK, and SPREAD")
    void fetchSpread_containsAllComponents() {
        String result = ex.fetchSpread("AAPL").join();
        assertTrue(result.contains("BID="),    "should contain BID");
        assertTrue(result.contains("ASK="),    "should contain ASK");
        assertTrue(result.contains("SPREAD="), "should contain SPREAD");
    }

    // ---- enrichTradesBatch --------------------------------------------------

    @Test
    @DisplayName("enrichTradesBatch: all trades are enriched")
    void enrichTradesBatch_allTradesEnriched() {
        var ids    = List.of("T1", "T2", "T3");
        var result = ex.enrichTradesBatch(ids);
        assertEquals(3, result.size());
        assertTrue(result.contains("T1:ENRICHED"), "T1 should be enriched");
        assertTrue(result.contains("T2:ENRICHED"), "T2 should be enriched");
        assertTrue(result.contains("T3:ENRICHED"), "T3 should be enriched");
    }

    @Test
    @DisplayName("enrichTradesBatch: empty list returns empty result")
    void enrichTradesBatch_emptyList_returnsEmpty() {
        assertTrue(ex.enrichTradesBatch(List.of()).isEmpty());
    }

    // ---- fetchWithFallback --------------------------------------------------

    @Test
    @DisplayName("fetchWithFallback: valid id returns enriched result")
    void fetchWithFallback_validId_returnsEnriched() {
        String result = ex.fetchWithFallback("T001").join();
        assertEquals("ENRICHED:T001", result);
    }

    @Test
    @DisplayName("fetchWithFallback: blank id triggers fallback")
    void fetchWithFallback_blankId_returnsFallback() {
        String result = ex.fetchWithFallback("").join();
        assertTrue(result.startsWith("FALLBACK:"), "blank id should return fallback");
    }

    @Test
    @DisplayName("fetchWithFallback: null id triggers fallback")
    void fetchWithFallback_nullId_returnsFallback() {
        String result = ex.fetchWithFallback(null).join();
        assertTrue(result.startsWith("FALLBACK:"), "null id should return fallback");
    }

    // ---- fetchWithHandle ----------------------------------------------------

    @Test
    @DisplayName("fetchWithHandle: valid id returns OK result")
    void fetchWithHandle_validId_returnsOk() {
        String result = ex.fetchWithHandle("T001").join();
        assertEquals("OK[DATA:T001]", result);
    }

    @Test
    @DisplayName("fetchWithHandle: INVALID id returns ERROR result")
    void fetchWithHandle_invalidId_returnsError() {
        String result = ex.fetchWithHandle("INVALID").join();
        assertTrue(result.startsWith("ERROR["), "should return ERROR for INVALID id");
    }

    // ---- alreadyDone --------------------------------------------------------

    @Test
    @DisplayName("alreadyDone: already-completed future returns the value immediately")
    void alreadyDone_returnsValueImmediately() throws ExecutionException, InterruptedException {
        CompletableFuture<String> cf = ex.alreadyDone("DONE");
        assertTrue(cf.isDone());
        assertEquals("DONE", cf.get());
    }

    // ---- simulatePriceFetch helper ------------------------------------------

    @Test
    @DisplayName("simulatePriceFetch: returns deterministic prices for known symbols")
    void simulatePriceFetch_deterministicForKnownSymbols() {
        assertEquals(182.50, ex.simulatePriceFetch("AAPL"), 0.001);
        assertEquals(415.00, ex.simulatePriceFetch("MSFT"), 0.001);
        assertEquals(172.30, ex.simulatePriceFetch("GOOG"), 0.001);
        assertEquals(100.00, ex.simulatePriceFetch("UNKNOWN"), 0.001);
    }
}
