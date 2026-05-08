package com.training.java817.module1.var;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * =============================================================================
 * MODULE 1 – LOCAL VARIABLE TYPE INFERENCE (var, Java 10 – JEP 286)
 * =============================================================================
 *
 * THEORY
 * ------
 * Java 10 introduced the `var` keyword for LOCAL VARIABLE TYPE INFERENCE.
 * The compiler infers the type from the initialiser expression – you write less
 * without losing static type safety.
 *
 * IMPORTANT RULES
 * ---------------
 *  • Only applies to LOCAL variables with an initialiser.
 *  • Cannot be used for:
 *      – method parameters
 *      – return types
 *      – fields
 *      – catch variables (until Java 10 this was not allowed)
 *  • The type is still STATIC – the variable is not dynamically typed.
 *  • Cannot be initialised to null (the compiler cannot infer the type).
 *  • Cannot be used without an initialiser.
 *
 * JAVA 11 EXTENSION
 * -----------------
 *  • var is allowed in lambda parameter lists:
 *      (var x, var y) -> x + y
 *    This enables annotations on lambda parameters:
 *      (@NonNull var name) -> name.toUpperCase()
 *
 * WHEN TO USE var
 * ---------------
 *  ✔ When the type is obvious from the right-hand side (constructor call, literal).
 *  ✔ When the type name is long / repetitive (generics, anonymous class types).
 *  ✔ In short, readable for-each loops.
 *  ✘ When the type is NOT obvious – var can reduce readability.
 *  ✘ In public API signatures (parameters, return types – not supported anyway).
 *
 * PROBLEM SOLVED
 * --------------
 * 1. Reduces redundant type declarations (the type appears twice in verbose code).
 * 2. Improves readability for complex generic types.
 * 3. Makes refactoring easier – change the type on the right, var adapts.
 */
public class LocalVarInferenceExamples {

    public record Trade(String id, String symbol, double notional, String status) {}

    // =========================================================================
    // BEFORE – Verbose explicit types (Java 9 style)
    // =========================================================================

    public Map<String, List<Trade>> groupByStatus_Before(List<Trade> trades) {
        Map<String, List<Trade>> result = new HashMap<>();
        for (Trade trade : trades) {
            List<Trade> group = result.computeIfAbsent(trade.status(), k -> new ArrayList<>());
            group.add(trade);
        }
        return result;
    }

    // =========================================================================
    // AFTER – var (Java 10)
    // =========================================================================

    public Map<String, List<Trade>> groupByStatus_After(List<Trade> trades) {
        var result = new HashMap<String, List<Trade>>();    // type inferred: HashMap<String, List<Trade>>
        for (var trade : trades) {                          // type inferred: Trade
            var group = result.computeIfAbsent(trade.status(), k -> new ArrayList<>());
            group.add(trade);
        }
        return result;
    }

    // =========================================================================
    // var with primitives and literals
    // =========================================================================

    public double calculateNotional(int quantity, double price) {
        var total    = quantity * price;        // inferred: double
        var rounded  = Math.round(total);       // inferred: long
        var currency = "USD";                   // inferred: String
        System.out.println(currency + " " + rounded);
        return total;
    }

    // =========================================================================
    // var reduces noise with long generic types
    // =========================================================================

    public Map<String, Map<String, List<String>>> buildNestedMap() {
        // Before: Map<String, Map<String, List<String>>> outer = new HashMap<>();
        var outer = new HashMap<String, Map<String, List<String>>>();
        var inner = new HashMap<String, List<String>>();
        inner.put("symbols", new ArrayList<>(List.of("AAPL", "MSFT")));
        outer.put("equities", inner);
        return outer;
    }

    // =========================================================================
    // var in try-with-resources
    // =========================================================================

    public List<String> readLines(String content) throws Exception {
        var lines = new ArrayList<String>();
        try (var reader = new BufferedReader(new StringReader(content))) {  // var for AutoCloseable
            var line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        }
        return lines;
    }

    // =========================================================================
    // var in for-each (most common use case)
    // =========================================================================

    public double sumHighValueTrades(List<Trade> trades) {
        double sum = 0;
        for (var t : trades) {                              // concise loop variable
            if (t.notional() > 1_000_000) {
                sum += t.notional();
            }
        }
        return sum;
    }

    // =========================================================================
    // Java 11 – var in lambda parameters (enables annotations)
    // =========================================================================

    public List<String> processSymbols(List<String> symbols) {
        // var in lambda parameters allows attaching annotations like @NonNull
        // (annotation support shown conceptually – requires annotation library)
        return symbols.stream()
                .filter((var s) -> s != null && !s.isBlank())
                .map((var s)    -> s.trim().toUpperCase())
                .toList();
    }

    // =========================================================================
    // What var CANNOT do – documented for clarity
    // =========================================================================

    /** This method CANNOT use var for its parameter (compile error if tried). */
    public String formatTrade(String tradeId, double notional) {
        // var tradeId  – NOT allowed: method parameter
        // var notional – NOT allowed: method parameter
        var formatted = "TRD[" + tradeId + "]=" + notional;   // OK: local var
        return formatted;
    }

    // demo main
    public static void main(String[] args) throws Exception {
        LocalVarInferenceExamples ex = new LocalVarInferenceExamples();

        var trades = List.of(
                new Trade("T1", "AAPL", 500_000,   "EXECUTED"),
                new Trade("T2", "MSFT", 1_500_000, "EXECUTED"),
                new Trade("T3", "GOOG", 800_000,   "PENDING")
        );

        var grouped = ex.groupByStatus_After(trades);
        System.out.println("Grouped keys  : " + grouped.keySet());

        var sum = ex.sumHighValueTrades(trades);
        System.out.println("High-value sum: " + sum);

        var lines = ex.readLines("line1\nline2\nline3");
        System.out.println("Lines read    : " + lines);

        var symbols = ex.processSymbols(List.of("aapl", " msft ", null, ""));
        System.out.println("Symbols       : " + symbols);
    }
}
