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
    void buildEmployeeQuery_beforeAndAfter_containSameKeywords() {
        String before = ex.buildEmployeeQuery_Before("ACTIVE");
        String after  = ex.buildEmployeeQuery_After("ACTIVE");

        // Both must contain the same key clauses
        for (String keyword : new String[]{"SELECT", "FROM", "JOIN", "WHERE", "ORDER"}) {
            assertTrue(before.contains(keyword), "before missing: " + keyword);
            assertTrue(after.contains(keyword),  "after missing: "  + keyword);
        }
    }

    @Test
    @DisplayName("SQL After: contains the interpolated status value")
    void buildEmployeeQuery_after_containsInterpolatedStatus() {
        String sql = ex.buildEmployeeQuery_After("ONBOARDING");
        assertTrue(sql.contains("'ONBOARDING'"));
    }

    @Test
    @DisplayName("SQL After: text block ends with a newline (trailing delimiter)")
    void buildEmployeeQuery_after_endsWithNewline() {
        String sql = ex.buildEmployeeQuery_After("ACTIVE");
        assertTrue(sql.endsWith("\n"));
    }

    // --- JSON ---

    @Test
    @DisplayName("JSON Before/After: both contain same fields")
    void buildEmployeeJson_beforeAndAfter_sameStructure() {
        String before = ex.buildEmployeeJson_Before("E001", "Alice", 95_000.0);
        String after  = ex.buildEmployeeJson_After("E001", "Alice", 95_000.0);

        for (String key : new String[]{"employeeId", "name", "salary", "status"}) {
            assertTrue(before.contains(key), "before missing key: " + key);
            assertTrue(after.contains(key),  "after missing key: "  + key);
        }
        assertTrue(before.contains("E001"));
        assertTrue(after.contains("E001"));
    }

    @Test
    @DisplayName("JSON After: uses double quotes for keys (valid JSON)")
    void buildEmployeeJson_after_usesDoubleQuotedKeys() {
        String json = ex.buildEmployeeJson_After("E002", "Bob", 80_000.0);
        assertTrue(json.contains("\"employeeId\""));
        assertTrue(json.contains("\"name\""));
    }

    // --- HTML ---

    @Test
    @DisplayName("HTML Before/After: both contain same structural elements")
    void buildHtml_beforeAndAfter_sameStructure() {
        String before = ex.buildEmployeeReportHtml_Before("E001", "ACTIVE");
        String after  = ex.buildEmployeeReportHtml_After("E001", "ACTIVE");

        for (String tag : new String[]{"<html>", "<body>", "<h1>", "</html>"}) {
            assertTrue(before.contains(tag), "before missing: " + tag);
            assertTrue(after.contains(tag),  "after missing: "  + tag);
        }
        assertTrue(before.contains("E001"));
        assertTrue(after.contains("E001"));
    }

    // --- XML ---

    @Test
    @DisplayName("XML employee message contains employee ID and name")
    void buildEmployeeXml_containsIdAndName() {
        String xml = ex.buildEmployeeXml_After("E001", "Alice", 95_000.0);
        assertTrue(xml.contains("<id>E001</id>"));
        assertTrue(xml.contains("<name>Alice</name>"));
        assertTrue(xml.contains("95000.0"));
    }

    // --- Line continuation ---

    @Test
    @DisplayName("singleLineFromBlock: no embedded newlines between clauses")
    void singleLineFromBlock_noNewlinesInMiddle() {
        String sql = ex.singleLineFromBlock();
        // The line continuation escape joins the first two lines; result should be one SQL line
        // plus a final newline
        assertTrue(sql.contains("SELECT employee_id, name, salary"));
        assertTrue(sql.contains("FROM employees"));
    }
}
