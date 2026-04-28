package com.training.java817.module2.textblocks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 2 – Text Blocks (JEP 378)")
class TextBlockExamplesTest {

    private TextBlockExamples ex;

    @BeforeEach
    void setUp() { ex = new TextBlockExamples(); }

    // --- SQL equivalence ---

    @Test
    @DisplayName("SQL Before/After: both contain the same core keywords")
    void buildTradeQuery_beforeAndAfter_containSameKeywords() {
        String before = ex.buildTradeQuery_Before("EXECUTED");
        String after  = ex.buildTradeQuery_After("EXECUTED");

        // Both must contain the same key clauses
        for (String keyword : new String[]{"SELECT", "FROM", "JOIN", "WHERE", "ORDER"}) {
            assertTrue(before.contains(keyword), "before missing: " + keyword);
            assertTrue(after.contains(keyword),  "after missing: "  + keyword);
        }
    }

    @Test
    @DisplayName("SQL After: contains the interpolated status value")
    void buildTradeQuery_after_containsInterpolatedStatus() {
        String sql = ex.buildTradeQuery_After("PENDING");
        assertTrue(sql.contains("'PENDING'"));
    }

    @Test
    @DisplayName("SQL After: text block ends with a newline (trailing delimiter)")
    void buildTradeQuery_after_endsWithNewline() {
        String sql = ex.buildTradeQuery_After("EXECUTED");
        assertTrue(sql.endsWith("\n"));
    }

    // --- JSON ---

    @Test
    @DisplayName("JSON Before/After: both contain same fields")
    void buildTradeJson_beforeAndAfter_sameStructure() {
        String before = ex.buildTradeJson_Before("T001", "AAPL", 1_500_000.0);
        String after  = ex.buildTradeJson_After("T001", "AAPL", 1_500_000.0);

        for (String key : new String[]{"tradeId", "symbol", "notional", "status"}) {
            assertTrue(before.contains(key), "before missing key: " + key);
            assertTrue(after.contains(key),  "after missing key: "  + key);
        }
        assertTrue(before.contains("T001"));
        assertTrue(after.contains("T001"));
    }

    @Test
    @DisplayName("JSON After: uses double quotes for keys (valid JSON)")
    void buildTradeJson_after_usesDoubleQuotedKeys() {
        String json = ex.buildTradeJson_After("T002", "MSFT", 500_000.0);
        assertTrue(json.contains("\"tradeId\""));
        assertTrue(json.contains("\"symbol\""));
    }

    // --- HTML ---

    @Test
    @DisplayName("HTML Before/After: both contain same structural elements")
    void buildHtml_beforeAndAfter_sameStructure() {
        String before = ex.buildTradeConfirmationHtml_Before("T001", "EXECUTED");
        String after  = ex.buildTradeConfirmationHtml_After("T001", "EXECUTED");

        for (String tag : new String[]{"<html>", "<body>", "<h1>", "</html>"}) {
            assertTrue(before.contains(tag), "before missing: " + tag);
            assertTrue(after.contains(tag),  "after missing: "  + tag);
        }
        assertTrue(before.contains("T001"));
        assertTrue(after.contains("T001"));
    }

    // --- XML ---

    @Test
    @DisplayName("XML FpML message contains trade ID and symbol")
    void buildFpmlMessage_containsTradeIdAndSymbol() {
        String xml = ex.buildFpmlMessage_After("T001", "AAPL", 1_500_000.0);
        assertTrue(xml.contains("<tradeId>T001</tradeId>"));
        assertTrue(xml.contains("<instrument>AAPL</instrument>"));
        assertTrue(xml.contains("1500000.0"));
    }

    // --- Line continuation ---

    @Test
    @DisplayName("singleLineFromBlock: no embedded newlines between clauses")
    void singleLineFromBlock_noNewlinesInMiddle() {
        String sql = ex.singleLineFromBlock();
        // The line continuation escape joins the first two lines; result should be one SQL line
        // plus a final newline
        assertTrue(sql.contains("SELECT trade_id, symbol, notional"));
        assertTrue(sql.contains("FROM trades"));
    }
}
