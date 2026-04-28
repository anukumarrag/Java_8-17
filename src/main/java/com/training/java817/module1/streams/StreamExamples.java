package com.training.java817.module1.streams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * =============================================================================
 * MODULE 1 – THE STREAM API (Java 8)
 * =============================================================================
 *
 * THEORY
 * ------
 * A Stream<T> is a sequence of elements that supports sequential and parallel
 * aggregate operations.  It is NOT a data structure – it does not store data.
 * You create a stream FROM a source (collection, array, file…) and then chain
 * INTERMEDIATE operations (lazy, return a stream) and a TERMINAL operation
 * (eager, returns a result or side effect).
 *
 * PROBLEM SOLVED
 * --------------
 * 1. Replaces verbose imperative for-loops with declarative, readable pipelines.
 * 2. Makes the intent of each step crystal clear (filter, map, reduce).
 * 3. Parallel processing via parallelStream() with zero thread management.
 * 4. Dramatically reduces the chance of mutation bugs caused by shared mutable
 *    state inside loops.
 *
 * STREAM PIPELINE
 *  source  ──►  [intermediate ops]  ──►  terminal op
 *  (lazy evaluation: nothing runs until the terminal op is reached)
 */
public class StreamExamples {

    // =========================================================================
    // Domain model used in examples
    // =========================================================================

    public record Trade(String id, String symbol, double notional, String status) {}

    // =========================================================================
    // BEFORE Java 8 – Imperative style
    // =========================================================================

    /** Sum notional of all EXECUTED trades – imperative version. */
    public double sumExecutedNotional_Before(List<Trade> trades) {
        double total = 0;
        for (Trade t : trades) {
            if ("EXECUTED".equals(t.status())) {
                total += t.notional();
            }
        }
        return total;
    }

    /** Get distinct symbols sorted alphabetically – imperative version. */
    public List<String> distinctSortedSymbols_Before(List<Trade> trades) {
        List<String> symbols = new ArrayList<>();
        for (Trade t : trades) {
            if (!symbols.contains(t.symbol())) {
                symbols.add(t.symbol());
            }
        }
        Collections.sort(symbols);
        return symbols;
    }

    /** Group trades by status – imperative version. */
    public Map<String, List<Trade>> groupByStatus_Before(List<Trade> trades) {
        Map<String, List<Trade>> map = new HashMap<>();
        for (Trade t : trades) {
            map.computeIfAbsent(t.status(), k -> new ArrayList<>()).add(t);
        }
        return map;
    }

    // =========================================================================
    // AFTER Java 8 – Stream API
    // =========================================================================

    /** Sum notional of all EXECUTED trades – stream version. */
    public double sumExecutedNotional_After(List<Trade> trades) {
        return trades.stream()
                .filter(t -> "EXECUTED".equals(t.status()))
                .mapToDouble(Trade::notional)
                .sum();
    }

    /** Get distinct symbols sorted alphabetically – stream version. */
    public List<String> distinctSortedSymbols_After(List<Trade> trades) {
        return trades.stream()
                .map(Trade::symbol)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /** Group trades by status – stream version. */
    public Map<String, List<Trade>> groupByStatus_After(List<Trade> trades) {
        return trades.stream()
                .collect(Collectors.groupingBy(Trade::status));
    }

    // =========================================================================
    // More intermediate operations
    // =========================================================================

    /** flatMap – flatten a list of lists. */
    public List<String> allSymbolsFromPortfolios(List<List<String>> portfolios) {
        return portfolios.stream()
                .flatMap(List::stream)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /** peek – non-consuming side-effect useful for debugging. */
    public List<Trade> topThreeByNotional(List<Trade> trades) {
        return trades.stream()
                .sorted(Comparator.comparingDouble(Trade::notional).reversed())
                .peek(t -> System.out.println("  Considering: " + t.id()))
                .limit(3)
                .collect(Collectors.toList());
    }

    /** reduce – custom aggregation (compute total notional). */
    public double totalNotional(List<Trade> trades) {
        return trades.stream()
                .mapToDouble(Trade::notional)
                .reduce(0.0, Double::sum);
    }

    /** count – count PENDING trades. */
    public long countPending(List<Trade> trades) {
        return trades.stream()
                .filter(t -> "PENDING".equals(t.status()))
                .count();
    }

    /** anyMatch / allMatch / noneMatch – short-circuit predicates. */
    public boolean hasHighValueTrade(List<Trade> trades) {
        return trades.stream()
                .anyMatch(t -> t.notional() > 10_000_000.0);
    }

    public boolean allTradesExecuted(List<Trade> trades) {
        return trades.stream()
                .allMatch(t -> "EXECUTED".equals(t.status()));
    }

    /** Collectors.joining – build a CSV of trade IDs. */
    public String tradeIdsCsv(List<Trade> trades) {
        return trades.stream()
                .map(Trade::id)
                .collect(Collectors.joining(", "));
    }

    /** Collectors.toMap – index by trade ID. */
    public Map<String, Trade> indexById(List<Trade> trades) {
        return trades.stream()
                .collect(Collectors.toMap(Trade::id, t -> t));
    }

    /** Parallel stream – use with caution; best for CPU-bound, stateless ops. */
    public double parallelSum(List<Trade> trades) {
        return trades.parallelStream()
                .mapToDouble(Trade::notional)
                .sum();
    }

    // demo main
    public static void main(String[] args) {
        List<Trade> trades = Arrays.asList(
                new Trade("T001", "AAPL", 500_000, "EXECUTED"),
                new Trade("T002", "MSFT", 1_200_000, "PENDING"),
                new Trade("T003", "AAPL", 800_000, "EXECUTED"),
                new Trade("T004", "GOOG", 3_000_000, "EXECUTED"),
                new Trade("T005", "MSFT", 200_000, "REJECTED")
        );

        StreamExamples ex = new StreamExamples();
        System.out.println("Sum EXECUTED : " + ex.sumExecutedNotional_After(trades));
        System.out.println("Symbols      : " + ex.distinctSortedSymbols_After(trades));
        System.out.println("IDs CSV      : " + ex.tradeIdsCsv(trades));
        System.out.println("Has high val : " + ex.hasHighValueTrade(trades));
        System.out.println("All executed : " + ex.allTradesExecuted(trades));
    }
}
