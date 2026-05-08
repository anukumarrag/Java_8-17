package com.training.bonus.recordpatterns;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Bonus – Record Patterns (Java 21)")
class RecordPatternsExamplesTest {

    private RecordPatternsExamples ex;

    @BeforeEach
    void setUp() { ex = new RecordPatternsExamples(); }

    private RecordPatternsExamples.Trade sampleTrade() {
        var addr  = new RecordPatternsExamples.Address("123 Wall St", "New York", "US");
        var cp    = new RecordPatternsExamples.Counterparty("CP001", "Acme Corp", addr);
        return new RecordPatternsExamples.Trade(
                "T001", "AAPL",
                new RecordPatternsExamples.Money(500_000, "USD"),
                cp);
    }

    // ---- describeTrade ------------------------------------------------------

    @Test
    @DisplayName("describeTrade: extracts components from Trade record")
    void describeTrade_extractsComponents() {
        String result = ex.describeTrade(sampleTrade());
        assertTrue(result.contains("T001"), "should contain trade ID");
        assertTrue(result.contains("AAPL"), "should contain symbol");
        assertTrue(result.contains("500000"), "should contain notional");
        assertTrue(result.contains("USD"),  "should contain currency");
    }

    @Test
    @DisplayName("describeTrade: non-Trade returns 'Not a trade'")
    void describeTrade_nonTrade_returnsDefault() {
        assertEquals("Not a trade", ex.describeTrade("not a trade"));
        assertEquals("Not a trade", ex.describeTrade(42));
        assertEquals("Not a trade", ex.describeTrade(null));
    }

    // ---- describeTradeCity_Before vs After ----------------------------------

    @Test
    @DisplayName("describeTradeCity: before and after return same city")
    void describeTradeCity_beforeAndAfterMatch() {
        var trade = sampleTrade();
        assertEquals(ex.describeTradeCity_Before(trade),
                     ex.describeTradeCity_After(trade));
    }

    @Test
    @DisplayName("describeTradeCity_After: returns city via nested record pattern")
    void describeTradeCity_After_returnsCity() {
        assertEquals("Trade T001 is in New York", ex.describeTradeCity_After(sampleTrade()));
    }

    @Test
    @DisplayName("describeTradeCity_After: non-Trade returns Unknown")
    void describeTradeCity_After_nonTrade_returnsUnknown() {
        assertEquals("Unknown", ex.describeTradeCity_After("not a trade"));
    }

    // ---- area (Shape sealed interface) --------------------------------------

    @Test
    @DisplayName("area: Circle area = pi * r^2")
    void area_circle() {
        double area = ex.area(new RecordPatternsExamples.Shape.Circle(5));
        assertEquals(Math.PI * 25, area, 1e-9);
    }

    @Test
    @DisplayName("area: Rectangle area = w * h")
    void area_rectangle() {
        assertEquals(24.0, ex.area(new RecordPatternsExamples.Shape.Rectangle(4, 6)));
    }

    @Test
    @DisplayName("area: Triangle area = 0.5 * b * h")
    void area_triangle() {
        assertEquals(12.0, ex.area(new RecordPatternsExamples.Shape.Triangle(3, 8)));
    }

    // ---- classifyPayment (guarded record pattern) ---------------------------

    @Test
    @DisplayName("classifyPayment: large USD payment")
    void classifyPayment_largeUsd() {
        var result = ex.classifyPayment(new RecordPatternsExamples.Money(2_000_000, "USD"));
        assertTrue(result.startsWith("Large USD payment"));
    }

    @Test
    @DisplayName("classifyPayment: standard EUR payment")
    void classifyPayment_standardEur() {
        var result = ex.classifyPayment(new RecordPatternsExamples.Money(5_000, "EUR"));
        assertTrue(result.contains("EUR"));
        assertTrue(result.startsWith("Standard"));
    }

    @Test
    @DisplayName("classifyPayment: zero amount")
    void classifyPayment_zeroAmount() {
        var result = ex.classifyPayment(new RecordPatternsExamples.Money(0, "USD"));
        assertEquals("Zero or negative payment", result);
    }

    @Test
    @DisplayName("classifyPayment: non-Money returns 'Not a Money object'")
    void classifyPayment_nonMoney() {
        assertEquals("Not a Money object", ex.classifyPayment("AAPL"));
    }

    // ---- extractSymbol (unnamed variables) ----------------------------------

    @Test
    @DisplayName("extractSymbol: extracts id and symbol using _ for unused components")
    void extractSymbol_extracts() {
        String result = ex.extractSymbol(sampleTrade());
        assertEquals("T001:AAPL", result);
    }

    @Test
    @DisplayName("extractSymbol: returns N/A for non-Trade")
    void extractSymbol_nonTrade() {
        assertEquals("N/A", ex.extractSymbol("not a trade"));
    }

    // ---- processEvent -------------------------------------------------------

    @Test
    @DisplayName("processEvent: Created event includes symbol and quantity")
    void processEvent_created() {
        var event  = new RecordPatternsExamples.TradeEvent.Created("T1", "MSFT", 100);
        String res = ex.processEvent(event);
        assertTrue(res.contains("T1"));
        assertTrue(res.contains("MSFT"));
    }

    @Test
    @DisplayName("processEvent: Priced event includes amount and currency")
    void processEvent_priced() {
        var event  = new RecordPatternsExamples.TradeEvent.Priced(
                "T2", new RecordPatternsExamples.Money(415.0, "USD"));
        String res = ex.processEvent(event);
        assertTrue(res.contains("T2"));
        assertTrue(res.contains("415.00"));
        assertTrue(res.contains("USD"));
    }
}
