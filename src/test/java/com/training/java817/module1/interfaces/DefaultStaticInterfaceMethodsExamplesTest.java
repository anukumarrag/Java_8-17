package com.training.java817.module1.interfaces;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 1 – Default & Static Interface Methods")
class DefaultStaticInterfaceMethodsExamplesTest {

    private DefaultStaticInterfaceMethodsExamples ex;

    @BeforeEach
    void setUp() { ex = new DefaultStaticInterfaceMethodsExamples(); }

    // ---- default processWithFee ---------------------------------------------

    @Test
    @DisplayName("FX processor: processWithFee adds 25 bps fee")
    void fxProcessor_processWithFee_includes25bpsFee() {
        String result = ex.processPayment(10_000, "USD");
        assertTrue(result.contains("FX_PROCESSED"),   "should contain FX_PROCESSED");
        assertTrue(result.contains("[fee="),           "should contain fee annotation");
    }

    @Test
    @DisplayName("Institutional processor: overrides processWithFee with 10 bps")
    void institutionalProcessor_overridesProcessWithFee() {
        String result = ex.processPayment(10_000, "USD");
        assertTrue(result.contains("INST_PROCESSED"), "should contain INST_PROCESSED");
        assertTrue(result.contains("[inst-fee="),     "should contain inst-fee annotation");
    }

    // ---- default safeProcess ------------------------------------------------

    @Test
    @DisplayName("safeProcess: rejects non-positive amount")
    void safeProcess_rejectsNonPositiveAmount() {
        String result = ex.safeProcessPayment(-1, "USD");
        assertEquals("REJECTED: non-positive amount", result);
    }

    @Test
    @DisplayName("safeProcess: rejects blank currency")
    void safeProcess_rejectsBlankCurrency() {
        String result = ex.safeProcessPayment(100, "  ");
        assertEquals("REJECTED: missing currency", result);
    }

    @Test
    @DisplayName("safeProcess: processes valid payment")
    void safeProcess_processesValidPayment() {
        String result = ex.safeProcessPayment(5_000, "EUR");
        assertTrue(result.contains("FX_PROCESSED"), "should contain FX_PROCESSED");
    }

    // ---- static interface methods -------------------------------------------

    @Test
    @DisplayName("isSupportedCurrency: returns true for USD, EUR, GBP, JPY")
    void isSupportedCurrency_knownCurrencies_returnsTrue() {
        assertTrue(ex.currencySupported("USD"));
        assertTrue(ex.currencySupported("EUR"));
        assertTrue(ex.currencySupported("GBP"));
        assertTrue(ex.currencySupported("JPY"));
    }

    @Test
    @DisplayName("isSupportedCurrency: returns false for unsupported currency")
    void isSupportedCurrency_unknownCurrency_returnsFalse() {
        assertFalse(ex.currencySupported("CNY"));
        assertFalse(ex.currencySupported("AUD"));
    }

    // ---- transformer pipeline -----------------------------------------------

    @Test
    @DisplayName("applyTransformerPipeline: trims, uppercases, and prepends EMP-")
    void transformerPipeline_trimsAndFormats() {
        assertEquals("EMP-AAPL", ex.applyTransformerPipeline("  aapl  "));
        assertEquals("EMP-MSFT", ex.applyTransformerPipeline("msft"));
    }

    // ---- diamond resolution -------------------------------------------------

    @Test
    @DisplayName("AuditedProcessor: resolves diamond conflict – combines both labels")
    void auditedProcessor_resolvesDiamondConflict() {
        String label = ex.resolveAuditLabel();
        assertTrue(label.contains("AUDITABLE"), "should contain AUDITABLE");
        assertTrue(label.contains("LOGGABLE"),  "should contain LOGGABLE");
    }

    // ---- noOp factory -------------------------------------------------------

    @Test
    @DisplayName("noOp factory: process returns NOOP prefix")
    void noOp_returnsNoopString() {
        var noop   = DefaultStaticInterfaceMethodsExamples.PaymentProcessor.noOp();
        String res = noop.process(999, "USD");
        assertTrue(res.startsWith("NOOP:"), "should start with NOOP:");
    }
}
