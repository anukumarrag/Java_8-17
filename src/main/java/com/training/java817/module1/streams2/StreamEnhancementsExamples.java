package com.training.java817.module1.streams2;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * =============================================================================
 * MODULE 1 – STREAM API ENHANCEMENTS (Java 9 – 16)
 * =============================================================================
 *
 * THEORY
 * ------
 * The Stream API introduced in Java 8 was enriched across subsequent releases:
 *
 * JAVA 9 (JEP 269)
 *   • Stream.takeWhile(predicate)  – take elements while predicate holds, then stop
 *   • Stream.dropWhile(predicate)  – skip elements while predicate holds, then take rest
 *   • Stream.iterate(seed, hasNext, next) – bounded iterate (avoids limit())
 *   • Stream.ofNullable(value)     – stream of 0 or 1 elements, safe null handling
 *
 * JAVA 12
 *   • Collectors.teeing(d1, d2, merger) – collect into two collectors simultaneously
 *
 * JAVA 16
 *   • Stream.toList()              – terminal op that returns an unmodifiable List
 *                                    (replaces .collect(Collectors.toList()))
 *
 * PROBLEM SOLVED
 * --------------
 * 1. takeWhile/dropWhile enable processing of sorted/ordered data without full scan.
 * 2. iterate-with-predicate eliminates the need to pair iterate() with limit().
 * 3. ofNullable safely wraps nullable values for flat-mapping.
 * 4. toList() reduces boilerplate (one method instead of collect(toList())).
 * 5. teeing collects one stream into two results in a single pass.
 */
public class StreamEnhancementsExamples {

    public record Trade(String id, String symbol, double notional, String status) {}

    // =========================================================================
    // Java 9 – takeWhile
    // =========================================================================

    /**
     * takeWhile: process a sorted price list until a price exceeds the threshold.
     * Stops as soon as the predicate returns FALSE – unlike filter which scans all.
     *
     * USE CASE: streaming time-series data sorted by timestamp; stop at cutoff.
     */
    public List<Double> pricesBelowCeiling(List<Double> sortedPrices, double ceiling) {
        return sortedPrices.stream()
                .takeWhile(price -> price < ceiling)
                .toList();   // Java 16 toList()
    }

    // =========================================================================
    // Java 9 – dropWhile
    // =========================================================================

    /**
     * dropWhile: skip all PENDING trades at the start of a sorted list,
     * then process the rest.
     *
     * USE CASE: skip all header/setup records at the beginning of a batch file.
     */
    public List<Trade> skipLeadingPending(List<Trade> trades) {
        return trades.stream()
                .dropWhile(t -> "PENDING".equals(t.status()))
                .toList();
    }

    // =========================================================================
    // Java 9 – Stream.iterate with predicate (bounded)
    // =========================================================================

    /**
     * BEFORE (Java 8): iterate() generates an infinite stream – you must call limit().
     *   Stream.iterate(0, n -> n + 1).limit(10)
     *
     * AFTER (Java 9): provide a hasNext predicate inline.
     *   Stream.iterate(0, n -> n < 10, n -> n + 1)
     *
     * USE CASE: generate a sequence of page offsets for pagination.
     */
    public List<Integer> pageOffsets(int pageSize, int totalRecords) {
        return Stream.iterate(0,
                        offset -> offset < totalRecords,   // hasNext predicate
                        offset -> offset + pageSize)       // next value
                .toList();
    }

    // =========================================================================
    // Java 9 – Stream.ofNullable
    // =========================================================================

    /**
     * Stream.ofNullable wraps a value that might be null into a Stream of
     * 0 (if null) or 1 element.
     *
     * USE CASE: flat-map a field that may be null in a record, avoiding NPE.
     */
    public List<String> collectNotes(List<Trade> trades, Map<String, String> tradeNotes) {
        return trades.stream()
                .flatMap(t -> Stream.ofNullable(tradeNotes.get(t.id())))  // null → empty stream
                .toList();
    }

    /** Return an Optional-like single-element stream from a nullable value. */
    public long countNonNull(List<String> values) {
        return values.stream()
                .flatMap(Stream::ofNullable)
                .count();
    }

    // =========================================================================
    // Java 12 – Collectors.teeing
    // =========================================================================

    /**
     * teeing: accumulates stream elements into TWO collectors simultaneously,
     * then merges the two results with a combining function.
     *
     * USE CASE: compute min and max (or count and sum) in a single pass.
     */
    public record NotionalStats(double sum, long count) {}

    public NotionalStats computeStats(List<Trade> trades) {
        return trades.stream()
                .collect(Collectors.teeing(
                        Collectors.summingDouble(Trade::notional),   // collector 1: sum
                        Collectors.counting(),                        // collector 2: count
                        NotionalStats::new                            // merger
                ));
    }

    /** Separate EXECUTED from other statuses in one pass using teeing. */
    public record PartitionResult(List<Trade> executed, List<Trade> others) {}

    public PartitionResult partitionTrades(List<Trade> trades) {
        return trades.stream()
                .collect(Collectors.teeing(
                        Collectors.filtering(t -> "EXECUTED".equals(t.status()), Collectors.toList()),
                        Collectors.filtering(t -> !"EXECUTED".equals(t.status()), Collectors.toList()),
                        PartitionResult::new
                ));
    }

    // =========================================================================
    // Java 16 – Stream.toList()
    // =========================================================================

    /**
     * Stream.toList() returns an UNMODIFIABLE list.
     * Replaces .collect(Collectors.toList()) – shorter and communicates intent.
     *
     * NOTE: unlike Collectors.toList(), the result of toList() is guaranteed
     * to be unmodifiable. Attempting to add/remove throws UnsupportedOperationException.
     */
    public List<String> executedSymbols(List<Trade> trades) {
        // Java 16: .toList() instead of .collect(Collectors.toList())
        return trades.stream()
                .filter(t -> "EXECUTED".equals(t.status()))
                .map(Trade::symbol)
                .distinct()
                .toList();
    }

    // =========================================================================
    // Combined: chaining new operations together
    // =========================================================================

    /**
     * Real-world pipeline:
     * - Drop any leading DRAFT trades (batch might have them at the start)
     * - Take only while below a notional threshold (risk limit)
     * - Collect distinct symbols
     */
    public List<String> eligibleSymbols(List<Trade> sortedByNotional, double maxNotional) {
        return sortedByNotional.stream()
                .dropWhile(t -> "DRAFT".equals(t.status()))
                .takeWhile(t -> t.notional() <= maxNotional)
                .map(Trade::symbol)
                .distinct()
                .toList();
    }

    // demo main
    public static void main(String[] args) {
        StreamEnhancementsExamples ex = new StreamEnhancementsExamples();

        System.out.println("Prices < 200 : " +
                ex.pricesBelowCeiling(List.of(150.0, 170.0, 195.0, 210.0, 250.0), 200.0));

        List<Trade> trades = List.of(
                new Trade("T1", "AAPL", 100_000, "PENDING"),
                new Trade("T2", "MSFT", 200_000, "PENDING"),
                new Trade("T3", "GOOG", 300_000, "EXECUTED"),
                new Trade("T4", "TSLA", 50_000,  "REJECTED")
        );
        System.out.println("Skip pending : " + ex.skipLeadingPending(trades).size());
        System.out.println("Offsets      : " + ex.pageOffsets(100, 350));

        NotionalStats stats = ex.computeStats(trades);
        System.out.println("Stats sum    : " + stats.sum() + " count=" + stats.count());
        System.out.println("Exec symbols : " + ex.executedSymbols(trades));
    }
}
