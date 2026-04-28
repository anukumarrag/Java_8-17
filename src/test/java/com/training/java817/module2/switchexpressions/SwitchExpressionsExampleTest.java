package com.training.java817.module2.switchexpressions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 2 – Switch Expressions (JEP 361)")
class SwitchExpressionsExampleTest {

    private SwitchExpressionsExample ex;

    @BeforeEach
    void setUp() { ex = new SwitchExpressionsExample(); }

    // --- SLA hours: before / after equivalence ---

    @ParameterizedTest(name = "SLA hours: {0} gives same result before and after")
    @EnumSource(SwitchExpressionsExample.TradeStatus.class)
    void getSlaHours_beforeAndAfterMatch(SwitchExpressionsExample.TradeStatus status) {
        assertEquals(ex.getSlaHours_Before(status), ex.getSlaHours_After(status));
    }

    @Test
    @DisplayName("SLA hours: DRAFT → 24 h")
    void getSlaHours_draft_is24() {
        assertEquals(24, ex.getSlaHours_After(SwitchExpressionsExample.TradeStatus.DRAFT));
    }

    @Test
    @DisplayName("SLA hours: EXECUTED → 1 h")
    void getSlaHours_executed_is1() {
        assertEquals(1, ex.getSlaHours_After(SwitchExpressionsExample.TradeStatus.EXECUTED));
    }

    @Test
    @DisplayName("SLA hours: REJECTED and CANCELLED share the same 48 h")
    void getSlaHours_rejectedAndCancelled_areBoth48() {
        assertEquals(48, ex.getSlaHours_After(SwitchExpressionsExample.TradeStatus.REJECTED));
        assertEquals(48, ex.getSlaHours_After(SwitchExpressionsExample.TradeStatus.CANCELLED));
    }

    // --- Risk weight: before / after equivalence ---

    @ParameterizedTest(name = "Risk weight: {0} gives same result before and after")
    @EnumSource(SwitchExpressionsExample.AssetClass.class)
    void getRiskWeight_beforeAndAfterMatch(SwitchExpressionsExample.AssetClass ac) {
        assertEquals(ex.getRiskWeight_Before(ac), ex.getRiskWeight_After(ac), 1e-9);
    }

    @Test
    @DisplayName("Risk weight: DERIVATIVE is highest at 1.5")
    void getRiskWeight_derivative_isHighest() {
        assertEquals(1.5, ex.getRiskWeight_After(SwitchExpressionsExample.AssetClass.DERIVATIVE));
    }

    @Test
    @DisplayName("Risk weight: FIXED_INCOME is lowest at 0.5")
    void getRiskWeight_fixedIncome_isLowest() {
        assertEquals(0.5, ex.getRiskWeight_After(SwitchExpressionsExample.AssetClass.FIXED_INCOME));
    }

    // --- Status message with yield ---

    @Test
    @DisplayName("Format message: SETTLED says settled")
    void formatStatusMessage_settled_containsSettled() {
        String msg = ex.formatStatusMessage_After(SwitchExpressionsExample.TradeStatus.SETTLED);
        assertTrue(msg.toLowerCase().contains("settled"));
    }

    @Test
    @DisplayName("Format message: REJECTED includes resubmit hint")
    void formatStatusMessage_rejected_includesResubmit() {
        String msg = ex.formatStatusMessage_After(SwitchExpressionsExample.TradeStatus.REJECTED);
        assertTrue(msg.contains("resubmit"));
    }

    @Test
    @DisplayName("Format message: CANCELLED includes operations hint")
    void formatStatusMessage_cancelled_includesOperations() {
        String msg = ex.formatStatusMessage_After(SwitchExpressionsExample.TradeStatus.CANCELLED);
        assertTrue(msg.contains("operations"));
    }

    // --- Notification channel ---

    @Test
    @DisplayName("Notification channel: EQUITY routes to equity-desk")
    void buildNotificationChannel_equity_routesToEquityDesk() {
        String channel = ex.buildNotificationChannel(SwitchExpressionsExample.AssetClass.EQUITY);
        assertTrue(channel.contains("equity-desk"));
    }

    @Test
    @DisplayName("Notification channel: DERIVATIVE also routes to equity-desk")
    void buildNotificationChannel_derivative_routesToEquityDesk() {
        String channel = ex.buildNotificationChannel(SwitchExpressionsExample.AssetClass.DERIVATIVE);
        assertTrue(channel.contains("equity-desk"));
    }

    @Test
    @DisplayName("Notification channel: FIXED_INCOME routes to rates-desk")
    void buildNotificationChannel_fixedIncome_routesToRatesDesk() {
        String channel = ex.buildNotificationChannel(SwitchExpressionsExample.AssetClass.FIXED_INCOME);
        assertTrue(channel.contains("rates-desk"));
    }
}
