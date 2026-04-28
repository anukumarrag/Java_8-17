package com.training.java817.module2.records;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 2 – Records (JEP 395)")
class RecordsExampleTest {

    private static final LocalDate TODAY = LocalDate.of(2024, 6, 1);

    private RecordsExample.TradeDetailsRecord sample() {
        return new RecordsExample.TradeDetailsRecord(
                "T001", "AAPL", 2_000_000.0, "CP01", TODAY, "EXECUTED");
    }

    // --- Auto-generated accessors ---

    @Test
    @DisplayName("Accessors: record accessors return correct values")
    void accessors_returnCorrectValues() {
        RecordsExample.TradeDetailsRecord r = sample();
        assertEquals("T001",       r.tradeId());
        assertEquals("AAPL",       r.symbol());
        assertEquals(2_000_000.0,  r.notional());
        assertEquals("CP01",       r.counterpartyId());
        assertEquals(TODAY,        r.settlementDate());
        assertEquals("EXECUTED",   r.status());
    }

    // --- Auto-generated equals/hashCode ---

    @Test
    @DisplayName("Equality: two records with same data are equal")
    void equality_sameData_areEqual() {
        assertEquals(sample(), sample());
    }

    @Test
    @DisplayName("Equality: records with different notional are not equal")
    void equality_differentNotional_notEqual() {
        RecordsExample.TradeDetailsRecord r2 = new RecordsExample.TradeDetailsRecord(
                "T001", "AAPL", 3_000_000.0, "CP01", TODAY, "EXECUTED");
        assertNotEquals(sample(), r2);
    }

    @Test
    @DisplayName("HashCode: equal records have same hash code")
    void hashCode_equalRecords_sameHash() {
        assertEquals(sample().hashCode(), sample().hashCode());
    }

    // --- Auto-generated toString ---

    @Test
    @DisplayName("toString: contains field values")
    void toString_containsFieldValues() {
        String str = sample().toString();
        assertTrue(str.contains("T001"));
        assertTrue(str.contains("AAPL"));
        assertTrue(str.contains("2000000.0"));
    }

    // --- Compact constructor validation ---

    @Test
    @DisplayName("Compact constructor: null tradeId throws NullPointerException")
    void compactConstructor_nullTradeId_throws() {
        assertThrows(NullPointerException.class, () ->
                new RecordsExample.TradeDetailsRecord(
                        null, "AAPL", 1_000.0, "CP01", TODAY, "DRAFT"));
    }

    @Test
    @DisplayName("Compact constructor: negative notional throws IllegalArgumentException")
    void compactConstructor_negativeNotional_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new RecordsExample.TradeDetailsRecord(
                        "T001", "AAPL", -1.0, "CP01", TODAY, "DRAFT"));
    }

    // --- Custom method ---

    @Test
    @DisplayName("isHighValue: returns true above 1M")
    void isHighValue_aboveThreshold_returnsTrue() {
        assertTrue(sample().isHighValue());
    }

    @Test
    @DisplayName("isHighValue: returns false at or below 1M")
    void isHighValue_belowThreshold_returnsFalse() {
        RecordsExample.TradeDetailsRecord r = new RecordsExample.TradeDetailsRecord(
                "T002", "MSFT", 500_000.0, "CP02", TODAY, "DRAFT");
        assertFalse(r.isHighValue());
    }

    // --- Static factory ---

    @Test
    @DisplayName("draft factory: sets status to DRAFT and settlementDate to today")
    void draftFactory_setsCorrectDefaults() {
        RecordsExample.TradeDetailsRecord draft =
                RecordsExample.TradeDetailsRecord.draft("T003", "GOOG", 500_000, "CP03");
        assertEquals("DRAFT", draft.status());
        assertEquals(LocalDate.now(), draft.settlementDate());
    }

    // --- POJO comparison (same logical data) ---

    @Test
    @DisplayName("POJO and record contain the same data for the same inputs")
    void pojoAndRecord_containSameData() {
        RecordsExample.TradeDetailsPojo pojo = new RecordsExample.TradeDetailsPojo(
                "T001", "AAPL", 2_000_000.0, "CP01", TODAY, "EXECUTED");
        RecordsExample.TradeDetailsRecord record = sample();

        assertEquals(pojo.getTradeId(),        record.tradeId());
        assertEquals(pojo.getSymbol(),         record.symbol());
        assertEquals(pojo.getNotional(),       record.notional());
        assertEquals(pojo.getCounterpartyId(), record.counterpartyId());
        assertEquals(pojo.getStatus(),         record.status());
    }

    // --- AuditedTrade implements interface ---

    @Test
    @DisplayName("AuditedTrade: auditSummary contains tradeId, action, and performer")
    void auditedTrade_summaryContainsDetails() {
        RecordsExample.AuditedTrade audit =
                new RecordsExample.AuditedTrade("T001", "EXECUTE", "john.doe");
        String summary = audit.auditSummary();
        assertTrue(summary.contains("T001"));
        assertTrue(summary.contains("EXECUTE"));
        assertTrue(summary.contains("john.doe"));
    }
}
