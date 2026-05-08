package com.training.java817.module1.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * =============================================================================
 * MODULE 1 – COLLECTION ENHANCEMENTS (Java 8 Map API + Java 9 Factory Methods)
 * =============================================================================
 *
 * THEORY
 * ------
 * Collections are used in virtually every Java program.  Two major improvements
 * were made across Java 8 and Java 9:
 *
 * JAVA 8 – NEW Map METHODS
 * -------------------------
 *   getOrDefault(key, default)              – avoids null check after get()
 *   putIfAbsent(key, value)                 – safe initialisation (no overwrite)
 *   computeIfAbsent(key, mappingFn)         – lazy value creation, ideal for multimap
 *   computeIfPresent(key, remappingFn)      – update only if key already exists
 *   compute(key, remappingFn)               – unconditional remap (insert/update/delete)
 *   merge(key, value, remappingFn)          – accumulate / combine existing value
 *   forEach(BiConsumer)                     – iterate without entrySet boilerplate
 *   replaceAll(BiFunction)                  – transform all values in-place
 *   Map.Entry.comparingByKey/Value()        – sort entries fluently
 *
 * JAVA 9 – IMMUTABLE COLLECTION FACTORY METHODS
 * -----------------------------------------------
 *   List.of(...)    – immutable list, null elements NOT allowed
 *   Set.of(...)     – immutable set, null elements and duplicates NOT allowed
 *   Map.of(k,v,...) – immutable map, null keys/values NOT allowed (≤10 pairs)
 *   Map.ofEntries(Map.entry(k,v),...) – for >10 pairs
 *   Map.copyOf(existingMap)           – immutable copy of any map
 *   List.copyOf / Set.copyOf          – immutable copies
 *
 * PROBLEM SOLVED
 * --------------
 * 1. Removes repetitive null-check boilerplate (getOrDefault, computeIfAbsent).
 * 2. Dramatically simplifies multimap / frequency-count patterns.
 * 3. Factory methods replace verbose Arrays.asList / Collections.unmodifiableXxx.
 * 4. Immutable collections prevent accidental mutation of shared data.
 */
public class CollectionEnhancementsExamples {

    public record Trade(String id, String symbol, double notional, String status) {}

    // =========================================================================
    // BEFORE – Java 7 Map patterns
    // =========================================================================

    /** getOrDefault – before: manual null check. */
    public int getTradeCount_Before(Map<String, Integer> counts, String symbol) {
        Integer count = counts.get(symbol);
        return count != null ? count : 0;
    }

    /** computeIfAbsent (multimap) – before: manual check + new list creation. */
    public Map<String, List<Trade>> groupBySymbol_Before(List<Trade> trades) {
        Map<String, List<Trade>> map = new HashMap<>();
        for (Trade t : trades) {
            if (!map.containsKey(t.symbol())) {
                map.put(t.symbol(), new ArrayList<>());
            }
            map.get(t.symbol()).add(t);
        }
        return map;
    }

    /** merge (frequency count) – before: verbose null-check increment. */
    public Map<String, Integer> countByStatus_Before(List<Trade> trades) {
        Map<String, Integer> counts = new HashMap<>();
        for (Trade t : trades) {
            Integer existing = counts.get(t.status());
            counts.put(t.status(), existing == null ? 1 : existing + 1);
        }
        return counts;
    }

    // =========================================================================
    // AFTER – Java 8 Map methods
    // =========================================================================

    /** getOrDefault: clean one-liner, no null check needed. */
    public int getTradeCount_After(Map<String, Integer> counts, String symbol) {
        return counts.getOrDefault(symbol, 0);
    }

    /**
     * computeIfAbsent: creates the list the first time the key is seen.
     * Perfect for building multimap / group-by structures.
     */
    public Map<String, List<Trade>> groupBySymbol_After(List<Trade> trades) {
        Map<String, List<Trade>> map = new HashMap<>();
        for (Trade t : trades) {
            map.computeIfAbsent(t.symbol(), k -> new ArrayList<>()).add(t);
        }
        return map;
    }

    /**
     * merge: if key absent, inserts value; if present, applies remapping function.
     * Ideal for frequency counting and accumulation.
     */
    public Map<String, Integer> countByStatus_After(List<Trade> trades) {
        Map<String, Integer> counts = new HashMap<>();
        for (Trade t : trades) {
            counts.merge(t.status(), 1, Integer::sum);
        }
        return counts;
    }

    /** forEach: iterate a map without the entrySet boilerplate. */
    public void printCounts(Map<String, Integer> counts) {
        counts.forEach((status, count) ->
                System.out.println(status + " : " + count));
    }

    /** putIfAbsent: safe default initialisation, won't overwrite existing value. */
    public Map<String, String> registerDefaultDesks(Map<String, String> desks) {
        desks.putIfAbsent("EQUITY",       "equity-desk@bank.com");
        desks.putIfAbsent("FIXED_INCOME", "rates-desk@bank.com");
        desks.putIfAbsent("FOREX",        "fx-desk@bank.com");
        return desks;
    }

    /** computeIfPresent: update a value only when the key already exists. */
    public Map<String, Integer> doubleIfPresent(Map<String, Integer> scores, String key) {
        scores.computeIfPresent(key, (k, v) -> v * 2);
        return scores;
    }

    /** replaceAll: transform every value in-place. */
    public Map<String, String> normaliseSymbols(Map<String, String> symbols) {
        symbols.replaceAll((key, value) -> value.trim().toUpperCase());
        return symbols;
    }

    // =========================================================================
    // JAVA 9 – Immutable factory methods
    // =========================================================================

    /** List.of: concise immutable list (replaces Arrays.asList + unmodifiableList). */
    public List<String> supportedCurrencies() {
        return List.of("USD", "EUR", "GBP", "JPY", "CHF");
    }

    /** Set.of: concise immutable set – duplicate elements cause IllegalArgumentException. */
    public Set<String> validStatuses() {
        return Set.of("DRAFT", "PENDING", "EXECUTED", "SETTLED", "REJECTED");
    }

    /**
     * Map.of: concise immutable map (up to 10 key-value pairs).
     * Arguments are alternating key, value, key, value, ...
     */
    public Map<String, Integer> slaByStatus() {
        return Map.of(
                "DRAFT",    24,
                "PENDING",  4,
                "EXECUTED", 1,
                "SETTLED",  0
        );
    }

    /**
     * Map.ofEntries: for maps with more than 10 pairs.
     */
    public Map<String, String> deskRoutingTable() {
        return Map.ofEntries(
                Map.entry("EQUITY",        "equity-desk@bank.com"),
                Map.entry("FIXED_INCOME",  "rates-desk@bank.com"),
                Map.entry("COMMODITY",     "commodity-desk@bank.com"),
                Map.entry("FOREX",         "fx-desk@bank.com"),
                Map.entry("DERIVATIVE",    "derivatives-desk@bank.com"),
                Map.entry("CRYPTO",        "crypto-desk@bank.com"),
                Map.entry("STRUCTURED",    "structured-desk@bank.com"),
                Map.entry("REPO",          "repo-desk@bank.com"),
                Map.entry("INDEX",         "index-desk@bank.com"),
                Map.entry("ETF",           "etf-desk@bank.com"),
                Map.entry("OPTION",        "options-desk@bank.com")  // >10 pairs – requires ofEntries
        );
    }

    /** List.copyOf / Map.copyOf: produce immutable snapshots of mutable collections. */
    public List<String> snapshotSymbols(List<String> mutableList) {
        return List.copyOf(mutableList);   // immutable; reflects state at copy time
    }

    public Map<String, Integer> snapshotCounts(Map<String, Integer> mutable) {
        return Map.copyOf(mutable);
    }

    // demo main
    public static void main(String[] args) {
        CollectionEnhancementsExamples ex = new CollectionEnhancementsExamples();

        Map<String, Integer> counts = new HashMap<>();
        counts.put("AAPL", 5);
        System.out.println("getOrDefault AAPL : " + ex.getTradeCount_After(counts, "AAPL"));
        System.out.println("getOrDefault MSFT : " + ex.getTradeCount_After(counts, "MSFT"));

        List<Trade> trades = List.of(
                new Trade("T1", "AAPL", 100_000, "EXECUTED"),
                new Trade("T2", "AAPL", 200_000, "PENDING"),
                new Trade("T3", "MSFT", 300_000, "EXECUTED")
        );
        System.out.println("Group by symbol : " + ex.groupBySymbol_After(trades).keySet());

        Map<String, Integer> freq = ex.countByStatus_After(trades);
        System.out.println("Count EXECUTED  : " + freq.get("EXECUTED"));

        System.out.println("Currencies      : " + ex.supportedCurrencies());
        System.out.println("Statuses        : " + ex.validStatuses());
        System.out.println("SLA by status   : " + ex.slaByStatus());
    }
}
