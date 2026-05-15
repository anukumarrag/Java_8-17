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
    @EnumSource(SwitchExpressionsExample.EmployeeStatus.class)
    void getSlaHours_beforeAndAfterMatch(SwitchExpressionsExample.EmployeeStatus status) {
        assertEquals(ex.getSlaHours_Before(status), ex.getSlaHours_After(status));
    }

    @Test
    @DisplayName("SLA hours: APPLIED → 48 h")
    void getSlaHours_applied_is48() {
        assertEquals(48, ex.getSlaHours_After(SwitchExpressionsExample.EmployeeStatus.APPLIED));
    }

    @Test
    @DisplayName("SLA hours: ACTIVE → 72 h")
    void getSlaHours_active_is72() {
        assertEquals(72, ex.getSlaHours_After(SwitchExpressionsExample.EmployeeStatus.ACTIVE));
    }

    @Test
    @DisplayName("SLA hours: RESIGNED and TERMINATED share the same 0 h")
    void getSlaHours_resignedAndTerminated_areBoth0() {
        assertEquals(0, ex.getSlaHours_After(SwitchExpressionsExample.EmployeeStatus.RESIGNED));
        assertEquals(0, ex.getSlaHours_After(SwitchExpressionsExample.EmployeeStatus.TERMINATED));
    }

    // --- Budget multiplier: before / after equivalence ---

    @ParameterizedTest(name = "Budget multiplier: {0} gives same result before and after")
    @EnumSource(SwitchExpressionsExample.Department.class)
    void getBudgetMultiplier_beforeAndAfterMatch(SwitchExpressionsExample.Department dept) {
        assertEquals(ex.getBudgetMultiplier_Before(dept), ex.getBudgetMultiplier_After(dept), 1e-9);
    }

    @Test
    @DisplayName("Budget multiplier: ENGINEERING is highest at 1.5")
    void getBudgetMultiplier_engineering_isHighest() {
        assertEquals(1.5, ex.getBudgetMultiplier_After(SwitchExpressionsExample.Department.ENGINEERING));
    }

    @Test
    @DisplayName("Budget multiplier: HR is lowest at 0.8")
    void getBudgetMultiplier_hr_isLowest() {
        assertEquals(0.8, ex.getBudgetMultiplier_After(SwitchExpressionsExample.Department.HR));
    }

    // --- Status message with yield ---

    @Test
    @DisplayName("Format message: ACTIVE says active")
    void formatStatusMessage_active_containsActive() {
        String msg = ex.formatStatusMessage_After(SwitchExpressionsExample.EmployeeStatus.ACTIVE);
        assertTrue(msg.toLowerCase().contains("active"));
    }

    @Test
    @DisplayName("Format message: RESIGNED includes resubmit hint")
    void formatStatusMessage_resigned_includesResubmit() {
        String msg = ex.formatStatusMessage_After(SwitchExpressionsExample.EmployeeStatus.RESIGNED);
        assertTrue(msg.contains("resubmit"));
    }

    @Test
    @DisplayName("Format message: TERMINATED includes operations hint")
    void formatStatusMessage_terminated_includesOperations() {
        String msg = ex.formatStatusMessage_After(SwitchExpressionsExample.EmployeeStatus.TERMINATED);
        assertTrue(msg.contains("operations"));
    }

    // --- Notification channel ---

    @Test
    @DisplayName("Notification channel: ENGINEERING routes to engineering@company.com")
    void buildNotificationChannel_engineering_routesToEngineering() {
        String channel = ex.buildNotificationChannel(SwitchExpressionsExample.Department.ENGINEERING);
        assertTrue(channel.contains("engineering"));
    }

    @Test
    @DisplayName("Notification channel: MARKETING routes to marketing@company.com")
    void buildNotificationChannel_marketing_routesToMarketing() {
        String channel = ex.buildNotificationChannel(SwitchExpressionsExample.Department.MARKETING);
        assertTrue(channel.contains("marketing"));
    }

    @Test
    @DisplayName("Notification channel: FINANCE routes to finance@company.com")
    void buildNotificationChannel_finance_routesToFinance() {
        String channel = ex.buildNotificationChannel(SwitchExpressionsExample.Department.FINANCE);
        assertTrue(channel.contains("finance"));
    }
}
