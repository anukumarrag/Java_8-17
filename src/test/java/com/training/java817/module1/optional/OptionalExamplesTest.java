package com.training.java817.module1.optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 1 – Optional")
class OptionalExamplesTest {

    private OptionalExamples ex;

    @BeforeEach
    void setUp() { ex = new OptionalExamples(); }

    // --- getTradeCity ---

    @Test
    @DisplayName("Before/After: city from full chain returns correct city")
    void getTradeCity_fullChain_returnsCity() {
        OptionalExamples.Trade trade = new OptionalExamples.Trade("T1",
                new OptionalExamples.Counterparty("CP1", "Acme",
                        new OptionalExamples.Address("1 Wall St", "New York")));
        assertEquals("New York", ex.getTradeCity_Before(trade));
        assertEquals("New York", ex.getTradeCity_After(trade));
    }

    @Test
    @DisplayName("Before/After: null counterparty returns UNKNOWN")
    void getTradeCity_nullCp_returnsUnknown() {
        OptionalExamples.Trade trade = new OptionalExamples.Trade("T2", null);
        assertEquals("UNKNOWN", ex.getTradeCity_Before(trade));
        assertEquals("UNKNOWN", ex.getTradeCity_After(trade));
    }

    @Test
    @DisplayName("Before/After: null trade returns UNKNOWN")
    void getTradeCity_nullTrade_returnsUnknown() {
        assertEquals("UNKNOWN", ex.getTradeCity_Before(null));
        assertEquals("UNKNOWN", ex.getTradeCity_After(null));
    }

    @Test
    @DisplayName("Before/After: null address returns UNKNOWN")
    void getTradeCity_nullAddress_returnsUnknown() {
        OptionalExamples.Trade trade = new OptionalExamples.Trade("T3",
                new OptionalExamples.Counterparty("CP2", "Beta", null));
        assertEquals("UNKNOWN", ex.getTradeCity_Before(trade));
        assertEquals("UNKNOWN", ex.getTradeCity_After(trade));
    }

    // --- findCounterpartyName ---

    @Test
    @DisplayName("Before/After: known ID returns counterparty name")
    void findCounterpartyName_knownId_returnsName() {
        assertEquals("Acme Corp", ex.findCounterpartyName_Before("CP001"));
        assertEquals("Acme Corp", ex.findCounterpartyName_After("CP001"));
    }

    @Test
    @DisplayName("Before/After: unknown ID returns NOT_FOUND")
    void findCounterpartyName_unknownId_returnsDefault() {
        assertEquals("NOT_FOUND", ex.findCounterpartyName_Before("UNKNOWN"));
        assertEquals("NOT_FOUND", ex.findCounterpartyName_After("UNKNOWN"));
    }

    // --- resolveSymbol ---

    @Test
    @DisplayName("resolveSymbol: null returns DEFAULT_SYM")
    void resolveSymbol_null_returnsDefault() {
        assertEquals("DEFAULT_SYM", ex.resolveSymbol(null));
    }

    @Test
    @DisplayName("resolveSymbol: blank returns DEFAULT_SYM")
    void resolveSymbol_blank_returnsDefault() {
        assertEquals("DEFAULT_SYM", ex.resolveSymbol("   "));
    }

    @Test
    @DisplayName("resolveSymbol: non-blank returns the symbol")
    void resolveSymbol_nonBlank_returnsSymbol() {
        assertEquals("AAPL", ex.resolveSymbol("AAPL"));
    }

    // --- requireCounterparty ---

    @Test
    @DisplayName("requireCounterparty: known ID returns counterparty")
    void requireCounterparty_knownId_returnsCounterparty() {
        assertNotNull(ex.requireCounterparty("CP001"));
    }

    @Test
    @DisplayName("requireCounterparty: unknown ID throws IllegalArgumentException")
    void requireCounterparty_unknownId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ex.requireCounterparty("MISSING"));
    }

    // --- Optional chaining ---

    @Test
    @DisplayName("getCounterpartyCity: returns city in Optional when present")
    void getCounterpartyCity_presentCity_returnsOptional() {
        OptionalExamples.Trade trade = new OptionalExamples.Trade("T1",
                new OptionalExamples.Counterparty("CP1", "Acme",
                        new OptionalExamples.Address("1 Wall St", "New York")));
        Optional<String> city = ex.getCounterpartyCity(Optional.of(trade));
        assertTrue(city.isPresent());
        assertEquals("New York", city.get());
    }

    @Test
    @DisplayName("getCounterpartyCity: returns empty when counterparty is null")
    void getCounterpartyCity_nullCp_returnsEmpty() {
        OptionalExamples.Trade trade = new OptionalExamples.Trade("T2", null);
        assertTrue(ex.getCounterpartyCity(Optional.of(trade)).isEmpty());
    }

    @Test
    @DisplayName("resolveCounterparty: returns primary when present")
    void resolveCounterparty_primaryPresent_returnsPrimary() {
        Optional<OptionalExamples.Counterparty> result =
                ex.resolveCounterparty("CP001", "FALLBACK");
        assertTrue(result.isPresent());
        assertEquals("CP001", result.get().id());
    }

    @Test
    @DisplayName("resolveCounterparty: falls back to second when primary absent")
    void resolveCounterparty_primaryAbsent_returnsFallback() {
        Optional<OptionalExamples.Counterparty> result =
                ex.resolveCounterparty("MISSING", "CP001");
        assertTrue(result.isPresent());
        assertEquals("CP001", result.get().id());
    }

    @Test
    @DisplayName("resolveCounterparty: returns empty when both absent")
    void resolveCounterparty_bothAbsent_returnsEmpty() {
        assertTrue(ex.resolveCounterparty("MISSING", "ALSO_MISSING").isEmpty());
    }
}
