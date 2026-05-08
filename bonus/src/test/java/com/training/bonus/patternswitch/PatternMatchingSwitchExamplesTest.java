package com.training.bonus.patternswitch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Bonus – Pattern Matching for Switch (Java 21)")
class PatternMatchingSwitchExamplesTest {

    private PatternMatchingSwitchExamples ex;

    @BeforeEach
    void setUp() { ex = new PatternMatchingSwitchExamples(); }

    // ---- describe_After -----------------------------------------------------

    @Test
    @DisplayName("describe_After: Created event contains symbol")
    void describe_created_containsSymbol() {
        var event  = new PatternMatchingSwitchExamples.TradeEvent.Created("T001", "AAPL", 500_000);
        String res = ex.describe_After(event);
        assertTrue(res.startsWith("CREATED"), "should start with CREATED");
        assertTrue(res.contains("AAPL"), "should contain symbol");
    }

    @Test
    @DisplayName("describe_After: Executed event contains price")
    void describe_executed_containsPrice() {
        var event  = new PatternMatchingSwitchExamples.TradeEvent.Executed("T001", "NYSE", 182.50);
        String res = ex.describe_After(event);
        assertTrue(res.startsWith("EXECUTED"));
        assertTrue(res.contains("182.5"));
    }

    @Test
    @DisplayName("describe_After: Rejected event contains reason")
    void describe_rejected_containsReason() {
        var event  = new PatternMatchingSwitchExamples.TradeEvent.Rejected("T002", "Insufficient funds");
        String res = ex.describe_After(event);
        assertTrue(res.startsWith("REJECTED"));
        assertTrue(res.contains("Insufficient funds"));
    }

    @Test
    @DisplayName("describe_Before and describe_After return equivalent results")
    void describe_beforeAndAfterMatch() {
        var events = new PatternMatchingSwitchExamples.TradeEvent[]{
            new PatternMatchingSwitchExamples.TradeEvent.Created("T1", "AAPL", 100_000),
            new PatternMatchingSwitchExamples.TradeEvent.Updated("T2", 200_000),
            new PatternMatchingSwitchExamples.TradeEvent.Executed("T3", "LSE", 150.0),
            new PatternMatchingSwitchExamples.TradeEvent.Rejected("T4", "Risk limit"),
            new PatternMatchingSwitchExamples.TradeEvent.Settled("T5", 300_000)
        };
        for (var e : events) {
            assertEquals(ex.describe_Before(e), ex.describe_After(e),
                    "Before and After should produce the same result for " + e.getClass().getSimpleName());
        }
    }

    // ---- classifyByNotional (guarded patterns) ------------------------------

    @Test
    @DisplayName("classifyByNotional: notional > 10M is LARGE_CREATION")
    void classify_largeCreation() {
        var event = new PatternMatchingSwitchExamples.TradeEvent.Created("T1", "GOOG", 15_000_000);
        assertEquals("LARGE_CREATION", ex.classifyByNotional(event));
    }

    @Test
    @DisplayName("classifyByNotional: notional between 1M and 10M is MEDIUM_CREATION")
    void classify_mediumCreation() {
        var event = new PatternMatchingSwitchExamples.TradeEvent.Created("T2", "AAPL", 5_000_000);
        assertEquals("MEDIUM_CREATION", ex.classifyByNotional(event));
    }

    @Test
    @DisplayName("classifyByNotional: notional <= 1M is SMALL_CREATION")
    void classify_smallCreation() {
        var event = new PatternMatchingSwitchExamples.TradeEvent.Created("T3", "TSLA", 500_000);
        assertEquals("SMALL_CREATION", ex.classifyByNotional(event));
    }

    @Test
    @DisplayName("classifyByNotional: high-price execution is PREMIUM_EXECUTION")
    void classify_premiumExecution() {
        var event = new PatternMatchingSwitchExamples.TradeEvent.Executed("T4", "NYSE", 600.0);
        assertEquals("PREMIUM_EXECUTION", ex.classifyByNotional(event));
    }

    // ---- handleWithNull -----------------------------------------------------

    @Test
    @DisplayName("handleWithNull: null event returns NULL_EVENT")
    void handleWithNull_nullReturnsNullEvent() {
        assertEquals("NULL_EVENT", ex.handleWithNull(null));
    }

    @Test
    @DisplayName("handleWithNull: Created event returns CREATED prefix")
    void handleWithNull_createdEvent() {
        var event = new PatternMatchingSwitchExamples.TradeEvent.Created("T1", "AAPL", 100_000);
        assertTrue(ex.handleWithNull(event).startsWith("CREATED:"));
    }

    // ---- formatValue (Object switch) ----------------------------------------

    @Test
    @DisplayName("formatValue: negative integer")
    void formatValue_negativeInt() {
        assertEquals("negative int: -5", ex.formatValue(-5));
    }

    @Test
    @DisplayName("formatValue: positive integer")
    void formatValue_positiveInt() {
        assertEquals("positive int: 42", ex.formatValue(42));
    }

    @Test
    @DisplayName("formatValue: double formats to 2 decimal places")
    void formatValue_double() {
        assertEquals("double: 3.14", ex.formatValue(3.14));
    }

    @Test
    @DisplayName("formatValue: blank string")
    void formatValue_blankString() {
        assertEquals("blank string", ex.formatValue("  "));
    }

    @Test
    @DisplayName("formatValue: non-blank string is uppercased")
    void formatValue_nonBlankString() {
        assertEquals("string: HELLO", ex.formatValue("hello"));
    }

    @Test
    @DisplayName("formatValue: null returns 'null'")
    void formatValue_null() {
        assertEquals("null", ex.formatValue(null));
    }

    // ---- toAuditEntry -------------------------------------------------------

    @Test
    @DisplayName("toAuditEntry: Created maps to LIFECYCLE category")
    void toAuditEntry_created() {
        var event = new PatternMatchingSwitchExamples.TradeEvent.Created("T1", "AAPL", 100_000);
        var entry = ex.toAuditEntry(event);
        assertEquals("T1",         entry.tradeId());
        assertEquals("LIFECYCLE",  entry.category());
        assertTrue(entry.detail().contains("AAPL"));
    }

    @Test
    @DisplayName("toAuditEntry: Rejected maps to REJECTION category")
    void toAuditEntry_rejected() {
        var event = new PatternMatchingSwitchExamples.TradeEvent.Rejected("T2", "No credit");
        var entry = ex.toAuditEntry(event);
        assertEquals("REJECTION", entry.category());
        assertTrue(entry.detail().contains("No credit"));
    }
}
