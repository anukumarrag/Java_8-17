package com.training.java817.module1.lambdas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 1 – Lambda Expressions")
class LambdaExamplesTest {

    private LambdaExamples ex;

    @BeforeEach
    void setUp() { ex = new LambdaExamples(); }

    @Test
    @DisplayName("Before: anonymous Comparator sorts correctly")
    void sortTrades_Before_shouldSortAlphabetically() {
        List<String> sorted = ex.sortTrades_Before(Arrays.asList("TRD003", "TRD001", "TRD002"));
        assertEquals(List.of("TRD001", "TRD002", "TRD003"), sorted);
    }

    @Test
    @DisplayName("After: lambda Comparator produces same result as before")
    void sortTrades_After_shouldMatchBefore() {
        List<String> input  = Arrays.asList("TRD003", "TRD001", "TRD002");
        assertEquals(ex.sortTrades_Before(input), ex.sortTrades_After(input));
    }

    @Test
    @DisplayName("Before: loop-based filter keeps ACTIVE entries only")
    void filterActiveTrades_Before_keepsActiveOnly() {
        List<String> result = ex.filterActiveTrades_Before(
                Arrays.asList("ACTIVE_1", "PENDING_2", "ACTIVE_3", "REJECTED_4"));
        assertEquals(List.of("ACTIVE_1", "ACTIVE_3"), result);
    }

    @Test
    @DisplayName("After: stream-based filter produces same result as before")
    void filterActiveTrades_After_matchesBefore() {
        List<String> input = Arrays.asList("ACTIVE_1", "PENDING_2", "ACTIVE_3");
        assertEquals(ex.filterActiveTrades_Before(input), ex.filterActiveTrades_After(input));
    }

    @Test
    @DisplayName("Predicate: isHighValue returns true above 1M")
    void isHighValue_aboveThreshold_returnsTrue() {
        assertTrue(ex.isHighValue(1_500_000.0));
        assertFalse(ex.isHighValue(500_000.0));
    }

    @Test
    @DisplayName("Function: formatTradeId pads with leading zeros")
    void formatTradeId_padsCorrectly() {
        assertEquals("TRD-000042", ex.formatTradeId(42));
        assertEquals("TRD-000001", ex.formatTradeId(1));
    }

    @Test
    @DisplayName("Supplier: getDefaultCounterparty returns UNKNOWN_CP")
    void getDefaultCounterparty_returnsDefault() {
        assertEquals("UNKNOWN_CP", ex.getDefaultCounterparty());
    }

    @Test
    @DisplayName("BiFunction: buildTradeKey concatenates symbol and version")
    void buildTradeKey_formatsCorrectly() {
        assertEquals("AAPL_v3", ex.buildTradeKey("AAPL", 3));
    }

    @Test
    @DisplayName("Method reference: upperCaseSymbols converts to uppercase")
    void upperCaseSymbols_uppercasesAll() {
        List<String> result = ex.upperCaseSymbols(Arrays.asList("aapl", "msft"));
        assertEquals(List.of("AAPL", "MSFT"), result);
    }

    @Test
    @DisplayName("Function composition: sanitizeAndFormat trims and uppercases")
    void sanitizeAndFormat_trimsAndUppercases() {
        assertEquals("AAPL", ex.sanitizeAndFormat("  aapl  "));
        assertEquals("MSFT", ex.sanitizeAndFormat("msft"));
    }

    @Test
    @DisplayName("Predicate composition: activeAndLongSymbol requires both conditions")
    void activeAndLongSymbol_requiresBothConditions() {
        Predicate<String> pred = ex.activeAndLongSymbol();
        assertTrue(pred.test("ACTIVE_LONG_SYMBOL"));     // starts with ACTIVE (16 chars > 10) ✓
        assertFalse(pred.test("ACTIVE_NO"));             // starts with ACTIVE but length=9 ≤ 10 ✗
        assertFalse(pred.test("PENDING_LONG_SYMBOL"));   // length > 10 but doesn't start with ACTIVE ✗
    }

    @Test
    @DisplayName("Effectively final capture: filterByPrefix uses captured variable")
    void filterByPrefix_capturesVariable() {
        List<String> result = ex.filterByPrefix(
                Arrays.asList("USD_1", "EUR_2", "USD_3"), "USD");
        assertEquals(List.of("USD_1", "USD_3"), result);
    }
}
