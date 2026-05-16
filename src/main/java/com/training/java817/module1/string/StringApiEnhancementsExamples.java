package com.training.java817.module1.string;

import java.util.List;
import java.util.stream.Collectors;

/**
 * =============================================================================
 * MODULE 1 – STRING API ENHANCEMENTS (Java 11 – 15)
 * =============================================================================
 *
 * THEORY
 * ------
 * The java.lang.String class received several practical additions across
 * Java 11–15 that eliminate the need for external libraries (Apache Commons,
 * Guava) for common string operations.
 *
 * JAVA 11 ADDITIONS
 * -----------------
 *   isBlank()          – true if empty or whitespace-only (Unicode-aware)
 *   strip()            – Unicode-aware trim() (strips Unicode whitespace)
 *   stripLeading()     – strip whitespace from start only
 *   stripTrailing()    – strip whitespace from end only
 *   repeat(int n)      – repeat the string n times
 *   lines()            – Stream<String> of lines (handles \n, \r, \r\n)
 *
 * JAVA 12 ADDITIONS
 *   indent(int n)      – add/remove leading spaces from each line; ensures trailing \n
 *   transform(Function)– apply a function to the string (fluent pipeline)
 *
 * JAVA 15 ADDITIONS (released with Text Blocks)
 *   stripIndent()      – remove incidental leading whitespace (same as text block logic)
 *   translateEscapes() – interpret escape sequences (\n, \t, etc.) in a string literal
 *   formatted(args)    – instance method equivalent of String.format(this, args)
 *
 * PROBLEM SOLVED
 * --------------
 * 1. isBlank() / strip() correctly handle Unicode whitespace (trim() uses \u0020).
 * 2. lines() replaces split("\\n") – handles all line terminators.
 * 3. repeat() replaces manual StringBuilder loops.
 * 4. transform() enables fluent string processing without extra variables.
 */
public class StringApiEnhancementsExamples {

    // =========================================================================
    // BEFORE – Java 10 and earlier
    // =========================================================================

    /** isBlank – before: manual trim() + isEmpty(). */
    public boolean isBlankOrEmpty_Before(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** strip – before: trim() which only removes ASCII whitespace (\u0020 and below). */
    public String stripWhitespace_Before(String s) {
        return s.trim();   // does NOT handle Unicode whitespace like \u00A0 (NBSP)
    }

    /** Count lines – before: split on newline. */
    public long countLines_Before(String text) {
        return text.isEmpty() ? 0 : text.split("\n").length;
    }

    // =========================================================================
    // AFTER – Java 11 String methods
    // =========================================================================

    // ---- isBlank (Java 11) --------------------------------------------------

    /**
     * isBlank(): returns true if the string is empty or contains only
     * Unicode whitespace. Null-safe wrapper shown here for practical use.
     */
    public boolean isBlankOrEmpty(String s) {
        return s == null || s.isBlank();
    }

    /** Validate employee ID: reject blank or null values. */
    public String validateEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Employee ID must not be blank");
        }
        return employeeId.strip();
    }

    // ---- strip / stripLeading / stripTrailing (Java 11) ---------------------

    /**
     * strip() is Unicode-aware; trim() only removes characters <= \u0020.
     * Use strip() for modern code; trim() for legacy ASCII-only contexts.
     */
    public String cleanSymbol(String rawSymbol) {
        return rawSymbol.strip().toUpperCase();
    }

    public String removeLeadingSpaces(String s) { return s.stripLeading(); }
    public String removeTrailingSpaces(String s) { return s.stripTrailing(); }

    // ---- repeat (Java 11) ---------------------------------------------------

    /** repeat(n): build a separator line, padding, or test data. */
    public String buildSeparator(int width) {
        return "-".repeat(width);
    }

    public String buildEmployeeReport(List<String> employeeIds) {
        String header = "Employee Report";
        String sep    = "=".repeat(header.length());
        String rows   = String.join("\n", employeeIds);
        return sep + "\n" + header + "\n" + sep + "\n" + rows;
    }

    // ---- lines() (Java 11) --------------------------------------------------

    /**
     * lines(): returns a lazy Stream<String> – handles \n, \r\n, \r.
     * Far more robust than split("\\n") which can produce trailing empty strings.
     */
    public long countNonBlankLines(String text) {
        return text.lines()
                .filter(line -> !line.isBlank())
                .count();
    }

    public List<String> parseEmployeeIds(String csvBlock) {
        return csvBlock.lines()
                .map(String::strip)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Java 12 – indent and transform
    // =========================================================================

    /**
     * indent(n): adds n spaces to the start of each line and appends \n.
     * Negative n removes leading whitespace.
     */
    public String indentedJson(String json) {
        return json.indent(4);   // indent by 4 spaces for display
    }

    /**
     * transform(Function<String,R>): apply any function to this string, useful
     * for building readable processing pipelines.
     */
    public String processEmployeeId(String rawId) {
        return rawId
                .transform(String::strip)
                .transform(String::toUpperCase)
                .transform(s -> "EMP-" + s);
    }

    // =========================================================================
    // Java 15 – formatted, stripIndent, translateEscapes
    // =========================================================================

    /**
     * formatted(args): instance method equivalent of String.format(this, args).
     * Enables method chaining on the format template itself.
     */
    public String formatEmployeeMessage(String employeeId, String department, double salary) {
        return "Employee %s | Dept: %s | Salary: %.2f".formatted(employeeId, department, salary);
    }

    /**
     * translateEscapes(): interpret escape sequences in a regular string literal.
     * Useful when you receive escape sequences as data (e.g., from a config file).
     */
    public String expandEscapes(String raw) {
        return raw.translateEscapes();   // e.g., "line1\\nline2" → "line1\nline2"
    }

    /**
     * stripIndent(): removes common leading whitespace from all lines.
     * Same algorithm used by text blocks – useful for dynamically built strings.
     */
    public String stripCommonIndent(String indented) {
        return indented.stripIndent();
    }

    // demo main
    public static void main(String[] args) {
        StringApiEnhancementsExamples ex = new StringApiEnhancementsExamples();

        System.out.println("isBlank blank : " + ex.isBlankOrEmpty("   "));
        System.out.println("isBlank text  : " + ex.isBlankOrEmpty("AAPL"));
        System.out.println("cleanSymbol   : " + ex.cleanSymbol("  aapl  "));
        System.out.println("separator     : " + ex.buildSeparator(20));
        System.out.println("repeat        : " + "=-".repeat(5));

        String block = "E001\n  E002  \n\nE003\n";
        System.out.println("Non-blank lines : " + ex.countNonBlankLines(block));
        System.out.println("Employee IDs    : " + ex.parseEmployeeIds(block));

        System.out.println("processEmployeeId: " + ex.processEmployeeId("  e001  "));
        System.out.println("formatted        : " + ex.formatEmployeeMessage("E001","ENGINEERING",150_000.0));
        System.out.println("translateEscape  : " + ex.expandEscapes("line1\\nline2\\ttabbed"));
    }
}
