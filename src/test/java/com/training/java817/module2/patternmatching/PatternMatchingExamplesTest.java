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
    @DisplayName("Before/After: PayrollEvent description matches")
    void describeEvent_payrollEvent_matchesBeforeAndAfter() {
        var event = new PatternMatchingExamples.PayrollEvent("E001", "ENGINEERING", 95_000);
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
    @DisplayName("After: PayrollEvent description contains employee ID and amount")
    void describeEvent_after_payrollContainsIdAndAmount() {
        var event = new PatternMatchingExamples.PayrollEvent("E002", "MARKETING", 80_000.0);
        String desc = ex.describeEvent_After(event);
        assertTrue(desc.contains("E002"));
        assertTrue(desc.contains("80000.0"));
    }

    // --- Guard condition (&&) ---

    @Test
    @DisplayName("High-salary guard: payroll above 100K is classified as high-salary")
    void describeHighSalaryEvent_aboveThreshold_classified() {
        var event = new PatternMatchingExamples.PayrollEvent("E003", "FINANCE", 150_000);
        String result = ex.describeHighSalaryEvent(event);
        assertTrue(result.startsWith("HIGH-SALARY"));
        assertTrue(result.contains("E003"));
    }

    @Test
    @DisplayName("High-salary guard: payroll below 100K is classified as standard")
    void describeHighSalaryEvent_belowThreshold_classified() {
        var event = new PatternMatchingExamples.PayrollEvent("E004", "HR", 60_000);
        String result = ex.describeHighSalaryEvent(event);
        assertTrue(result.startsWith("Standard"));
    }

    @Test
    @DisplayName("High-salary guard: non-payroll event returns not-a-payroll message")
    void describeHighSalaryEvent_nonPayroll_returnsMessage() {
        var login = new PatternMatchingExamples.LoginEvent("alice", "10.0.0.1", true);
        assertEquals("Not a payroll event", ex.describeHighSalaryEvent(login));
    }

    // --- Negation ---

    @Test
    @DisplayName("isNotLoginEvent: true for PayrollEvent")
    void isNotLoginEvent_payrollEvent_returnsTrue() {
        assertTrue(ex.isNotLoginEvent(new PatternMatchingExamples.PayrollEvent("E1", "SALES", 70_000)));
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
