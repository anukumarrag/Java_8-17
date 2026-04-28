package com.training.java817.module2.patternmatching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 2 – Pattern Matching for instanceof (JEP 394)")
class PatternMatchingExamplesTest {

    private PatternMatchingExamples ex;

    @BeforeEach
    void setUp() { ex = new PatternMatchingExamples(); }

    // --- Before/After equivalence ---

    @Test
    @DisplayName("Before/After: LoginEvent description matches")
    void describeEvent_loginEvent_matchesBeforeAndAfter() {
        var event = new PatternMatchingExamples.LoginEvent("alice", "10.0.0.1", true);
        assertEquals(ex.describeEvent_Before(event), ex.describeEvent_After(event));
    }

    @Test
    @DisplayName("Before/After: TradeEvent description matches")
    void describeEvent_tradeEvent_matchesBeforeAndAfter() {
        var event = new PatternMatchingExamples.TradeEvent("T001", "AAPL", 1_000_000);
        assertEquals(ex.describeEvent_Before(event), ex.describeEvent_After(event));
    }

    @Test
    @DisplayName("Before/After: AlertEvent description matches")
    void describeEvent_alertEvent_matchesBeforeAndAfter() {
        var event = new PatternMatchingExamples.AlertEvent("A001", "HIGH", "Threshold breached");
        assertEquals(ex.describeEvent_Before(event), ex.describeEvent_After(event));
    }

    @Test
    @DisplayName("Before/After: unknown object returns UNKNOWN EVENT")
    void describeEvent_unknownObject_returnsUnknown() {
        assertEquals("UNKNOWN EVENT", ex.describeEvent_Before("not an event"));
        assertEquals("UNKNOWN EVENT", ex.describeEvent_After("not an event"));
    }

    // --- Pattern matching content ---

    @Test
    @DisplayName("After: LoginEvent description contains userId and success flag")
    void describeEvent_after_loginContainsUserId() {
        var event = new PatternMatchingExamples.LoginEvent("bob", "192.168.1.1", false);
        String desc = ex.describeEvent_After(event);
        assertTrue(desc.contains("bob"));
        assertTrue(desc.contains("false"));
    }

    @Test
    @DisplayName("After: TradeEvent description contains trade ID and amount")
    void describeEvent_after_tradeContainsIdAndAmount() {
        var event = new PatternMatchingExamples.TradeEvent("T002", "MSFT", 2_500_000.0);
        String desc = ex.describeEvent_After(event);
        assertTrue(desc.contains("T002"));
        assertTrue(desc.contains("2500000.0"));
    }

    // --- Guard condition (&&) ---

    @Test
    @DisplayName("High-value guard: trade above 1M is classified as high-value")
    void describeHighValueTrade_aboveThreshold_classified() {
        var event = new PatternMatchingExamples.TradeEvent("T003", "GOOG", 5_000_000);
        String result = ex.describeHighValueTrade(event);
        assertTrue(result.startsWith("HIGH-VALUE"));
        assertTrue(result.contains("T003"));
    }

    @Test
    @DisplayName("High-value guard: trade below 1M is classified as standard")
    void describeHighValueTrade_belowThreshold_classified() {
        var event = new PatternMatchingExamples.TradeEvent("T004", "AAPL", 500_000);
        String result = ex.describeHighValueTrade(event);
        assertTrue(result.startsWith("Standard"));
    }

    @Test
    @DisplayName("High-value guard: non-trade event returns not-a-trade message")
    void describeHighValueTrade_nonTrade_returnsMessage() {
        var login = new PatternMatchingExamples.LoginEvent("alice", "10.0.0.1", true);
        assertEquals("Not a trade event", ex.describeHighValueTrade(login));
    }

    // --- Negation ---

    @Test
    @DisplayName("isNotLoginEvent: true for TradeEvent")
    void isNotLoginEvent_tradeEvent_returnsTrue() {
        assertTrue(ex.isNotLoginEvent(new PatternMatchingExamples.TradeEvent("T1", "AAPL", 1000)));
    }

    @Test
    @DisplayName("isNotLoginEvent: false for LoginEvent")
    void isNotLoginEvent_loginEvent_returnsFalse() {
        assertFalse(ex.isNotLoginEvent(new PatternMatchingExamples.LoginEvent("u", "ip", true)));
    }

    // --- Payment / Money composition ---

    @Test
    @DisplayName("describePayment: USD payment described correctly")
    void describePayment_usdPayment_described() {
        var payment = new PatternMatchingExamples.Payment(
                new PatternMatchingExamples.Money(5000.0, "USD"), "Alice");
        assertTrue(ex.describePayment(payment).contains("USD payment"));
        assertTrue(ex.describePayment(payment).contains("Alice"));
    }

    @Test
    @DisplayName("describePayment: non-USD payment returns non-USD message")
    void describePayment_eurPayment_notUsd() {
        var payment = new PatternMatchingExamples.Payment(
                new PatternMatchingExamples.Money(5000.0, "EUR"), "Bob");
        assertTrue(ex.describePayment(payment).contains("Non-USD"));
    }
}
