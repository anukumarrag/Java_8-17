package com.training.java817.module1.string;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 1 – String API Enhancements (Java 11–15)")
class StringApiEnhancementsExamplesTest {

    private StringApiEnhancementsExamples ex;

    @BeforeEach
    void setUp() { ex = new StringApiEnhancementsExamples(); }

    // ---- isBlank / isBlankOrEmpty -------------------------------------------

    @Test
    @DisplayName("isBlankOrEmpty: true for null, empty, and whitespace-only strings")
    void isBlankOrEmpty_trueForBlankInputs() {
        assertTrue(ex.isBlankOrEmpty(null));
        assertTrue(ex.isBlankOrEmpty(""));
        assertTrue(ex.isBlankOrEmpty("   "));
        assertTrue(ex.isBlankOrEmpty("\t\n"));
    }

    @Test
    @DisplayName("isBlankOrEmpty: false for non-blank strings")
    void isBlankOrEmpty_falseForNonBlank() {
        assertFalse(ex.isBlankOrEmpty("AAPL"));
        assertFalse(ex.isBlankOrEmpty(" a "));
    }

    // ---- validateTradeId ----------------------------------------------------

    @Test
    @DisplayName("validateTradeId: strips whitespace and returns value")
    void validateTradeId_stripsAndReturns() {
        assertEquals("T001", ex.validateTradeId("  T001  "));
    }

    @Test
    @DisplayName("validateTradeId: throws for null")
    void validateTradeId_nullThrows() {
        assertThrows(IllegalArgumentException.class, () -> ex.validateTradeId(null));
    }

    @Test
    @DisplayName("validateTradeId: throws for blank string")
    void validateTradeId_blankThrows() {
        assertThrows(IllegalArgumentException.class, () -> ex.validateTradeId("   "));
    }

    // ---- strip / stripLeading / stripTrailing --------------------------------

    @Test
    @DisplayName("cleanSymbol: strips and uppercases")
    void cleanSymbol_stripsAndUppercases() {
        assertEquals("AAPL", ex.cleanSymbol("  aapl  "));
        assertEquals("MSFT", ex.cleanSymbol("msft"));
    }

    @Test
    @DisplayName("removeLeadingSpaces: removes only leading whitespace")
    void removeLeadingSpaces_onlyLeading() {
        assertEquals("aapl  ", ex.removeLeadingSpaces("  aapl  "));
    }

    @Test
    @DisplayName("removeTrailingSpaces: removes only trailing whitespace")
    void removeTrailingSpaces_onlyTrailing() {
        assertEquals("  aapl", ex.removeTrailingSpaces("  aapl  "));
    }

    // ---- repeat -------------------------------------------------------------

    @Test
    @DisplayName("buildSeparator: produces correct number of dashes")
    void buildSeparator_correctLength() {
        String sep = ex.buildSeparator(10);
        assertEquals("----------", sep);
        assertEquals(10, sep.length());
    }

    @Test
    @DisplayName("buildSeparator: zero width produces empty string")
    void buildSeparator_zeroWidth_isEmpty() {
        assertEquals("", ex.buildSeparator(0));
    }

    // ---- lines --------------------------------------------------------------

    @Test
    @DisplayName("countNonBlankLines: ignores blank lines")
    void countNonBlankLines_ignoresBlanks() {
        String text = "line1\n\n  \nline2\nline3\n";
        assertEquals(3L, ex.countNonBlankLines(text));
    }

    @Test
    @DisplayName("countNonBlankLines: empty string returns 0")
    void countNonBlankLines_emptyString_returnsZero() {
        assertEquals(0L, ex.countNonBlankLines(""));
    }

    @Test
    @DisplayName("parseTradeIds: strips whitespace and filters blanks")
    void parseTradeIds_stripsAndFilters() {
        String block = "T001\n  T002  \n\nT003\n";
        List<String> ids = ex.parseTradeIds(block);
        assertEquals(List.of("T001", "T002", "T003"), ids);
    }

    // ---- transform (Java 12) ------------------------------------------------

    @Test
    @DisplayName("processTradeId: strips, uppercases, and prepends TRD-")
    void processTradeId_stripsUppercasesAndPrepends() {
        assertEquals("TRD-T001", ex.processTradeId("  t001  "));
        assertEquals("TRD-AAPL", ex.processTradeId("aapl"));
    }

    // ---- formatted (Java 15) ------------------------------------------------

    @Test
    @DisplayName("formatTradeMessage: produces formatted trade string")
    void formatTradeMessage_producesCorrectString() {
        String result = ex.formatTradeMessage("T001", "AAPL", 1_500_000.0);
        assertEquals("Trade T001 | Symbol: AAPL | Notional: 1500000.00", result);
    }

    // ---- translateEscapes (Java 15) -----------------------------------------

    @Test
    @DisplayName("expandEscapes: translates \\n to actual newline")
    void expandEscapes_translatesNewline() {
        String result = ex.expandEscapes("line1\\nline2");
        assertTrue(result.contains("\n"), "should contain actual newline");
        assertEquals(2, result.lines().count());
    }

    @Test
    @DisplayName("expandEscapes: translates \\t to actual tab")
    void expandEscapes_translatesTab() {
        String result = ex.expandEscapes("col1\\tcol2");
        assertTrue(result.contains("\t"), "should contain actual tab");
    }
}
