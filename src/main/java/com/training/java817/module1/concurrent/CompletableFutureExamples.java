package com.training.java817.module1.concurrent;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * =============================================================================
 * MODULE 1 – CompletableFuture (Java 8)
 * =============================================================================
 *
 * THEORY
 * ------
 * Before Java 8, asynchronous programming in Java required:
 *   • Thread / Runnable – low-level, manual thread management.
 *   • Future<T>         – can retrieve a result, but blocks on get() and has
 *                         no composition API.
 *
 * CompletableFuture<T> (Java 8) solves both problems:
 *   1. Non-blocking: chain callbacks that run when the future completes.
 *   2. Composable:   thenApply / thenCompose / thenCombine build pipelines.
 *   3. Exception handling: exceptionally / handle catch errors in the pipeline.
 *   4. Combines futures: allOf (wait for all) / anyOf (first to complete).
 *
 * PIPELINE OVERVIEW
 * -----------------
 *   supplyAsync  ──► thenApply ──► thenCompose ──► thenCombine ──► join/get
 *                                                              \──► exceptionally
 *
 * KEY METHODS
 * -----------
 *   supplyAsync(Supplier)         – start async task, returns CompletableFuture<T>
 *   runAsync(Runnable)            – start async task with no return value
 *   thenApply(Function)           – transform result (synchronous on result thread)
 *   thenApplyAsync(Function)      – transform result on ForkJoinPool thread
 *   thenCompose(Function→CF)      – flat-map: avoids CompletableFuture<CompletableFuture<T>>
 *   thenCombine(CF, BiFunction)   – combine results of two independent futures
 *   thenAccept(Consumer)          – consume result, no chaining
 *   thenRun(Runnable)             – run side-effect after completion
 *   exceptionally(Function)       – recover from exception
 *   handle(BiFunction)            – handle both result and exception
 *   whenComplete(BiConsumer)      – inspect result or exception (no transformation)
 *   allOf(CF...)                  – completes when ALL futures complete
 *   anyOf(CF...)                  – completes when ANY future completes
 *   completedFuture(value)        – already-completed future (useful in tests)
 */
public class CompletableFutureExamples {

    // =========================================================================
    // BEFORE – Blocking Future<T> (Java 5–7 style)
    // =========================================================================

    /** Fetch trade price from an exchange – blocks on get(). */
    public double fetchPrice_Before(String symbol) throws Exception {
        java.util.concurrent.Callable<Double> task = () -> simulatePriceFetch(symbol);
        java.util.concurrent.ExecutorService exec =
                java.util.concurrent.Executors.newSingleThreadExecutor();
        java.util.concurrent.Future<Double> future = exec.submit(task);
        // Blocks the calling thread until the result is ready
        double price = future.get();
        exec.shutdown();
        return price;
    }

    // =========================================================================
    // AFTER – CompletableFuture (Java 8)
    // =========================================================================

    // ---- supplyAsync + thenApply --------------------------------------------

    /**
     * Fetch a price asynchronously, then format it – non-blocking pipeline.
     * The thread that calls this method is NOT blocked.
     */
    public CompletableFuture<String> fetchFormattedPrice(String symbol) {
        return CompletableFuture
                .supplyAsync(() -> simulatePriceFetch(symbol))    // runs on ForkJoinPool
                .thenApply(price -> symbol + " @ " + String.format("%.4f", price));
    }

    // ---- thenCompose: flat-map two dependent async calls --------------------

    /**
     * First fetch the trade, then asynchronously look up the counterparty.
     * thenCompose avoids nesting: CompletableFuture<CompletableFuture<String>>.
     */
    public CompletableFuture<String> fetchTradeWithCounterparty(String tradeId) {
        return CompletableFuture
                .supplyAsync(() -> "TRADE:" + tradeId)
                .thenCompose(trade ->
                        CompletableFuture.supplyAsync(() -> trade + " CP:ACME"));
    }

    // ---- thenCombine: merge two independent async results -------------------

    /**
     * Fetch bid and ask prices from two independent sources simultaneously,
     * then combine them into a spread.
     */
    public CompletableFuture<String> fetchSpread(String symbol) {
        CompletableFuture<Double> bidFuture =
                CompletableFuture.supplyAsync(() -> simulatePriceFetch(symbol) - 0.01);
        CompletableFuture<Double> askFuture =
                CompletableFuture.supplyAsync(() -> simulatePriceFetch(symbol) + 0.01);

        return bidFuture.thenCombine(askFuture,
                (bid, ask) -> String.format("BID=%.4f ASK=%.4f SPREAD=%.4f",
                        bid, ask, ask - bid));
    }

    // ---- allOf: wait for a batch of futures ---------------------------------

    /**
     * Enrich all trades in parallel, then collect results.
     * allOf() itself returns CompletableFuture<Void>; you need to join each
     * individual future to retrieve the result.
     */
    public List<String> enrichTradesBatch(List<String> tradeIds) {
        List<CompletableFuture<String>> futures = tradeIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> id + ":ENRICHED"))
                .collect(Collectors.toList());

        // Wait for ALL futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Now all futures are complete – join() returns immediately
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    // ---- anyOf: first result wins -------------------------------------------

    /**
     * Query multiple pricing sources; return whichever responds first.
     */
    public CompletableFuture<Object> fastestPriceSource(String symbol) {
        CompletableFuture<String> source1 =
                CompletableFuture.supplyAsync(() -> { sleep(50); return "SRC1:" + symbol; });
        CompletableFuture<String> source2 =
                CompletableFuture.supplyAsync(() -> { sleep(30); return "SRC2:" + symbol; });
        CompletableFuture<String> source3 =
                CompletableFuture.supplyAsync(() -> { sleep(80); return "SRC3:" + symbol; });

        return CompletableFuture.anyOf(source1, source2, source3);
    }

    // ---- exceptionally: recover from errors ---------------------------------

    /**
     * Try to fetch an enriched trade; if anything fails, return a fallback value.
     */
    public CompletableFuture<String> fetchWithFallback(String tradeId) {
        return CompletableFuture
                .supplyAsync(() -> {
                    if (tradeId == null || tradeId.isBlank())
                        throw new IllegalArgumentException("tradeId is blank");
                    return "ENRICHED:" + tradeId;
                })
                .exceptionally(ex -> "FALLBACK:" + ex.getMessage());
    }

    // ---- handle: access both result and exception in one callback -----------

    /**
     * handle() runs regardless of success or failure.
     * The result parameter is null on failure; the exception is null on success.
     */
    public CompletableFuture<String> fetchWithHandle(String tradeId) {
        return CompletableFuture
                .supplyAsync(() -> {
                    if ("INVALID".equals(tradeId))
                        throw new RuntimeException("Trade not found: " + tradeId);
                    return "DATA:" + tradeId;
                })
                .handle((result, ex) ->
                        ex != null ? "ERROR[" + ex.getMessage() + "]" : "OK[" + result + "]");
    }

    // ---- whenComplete: observe without transforming -------------------------

    public CompletableFuture<String> fetchWithLogging(String tradeId) {
        return CompletableFuture
                .supplyAsync(() -> "TRADE:" + tradeId)
                .whenComplete((result, ex) -> {
                    if (ex != null) System.err.println("ERROR: " + ex.getMessage());
                    else System.out.println("Completed: " + result);
                });
    }

    // ---- completedFuture: useful in testing / mocking -----------------------

    public CompletableFuture<String> alreadyDone(String value) {
        return CompletableFuture.completedFuture(value);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Simulate a remote price fetch (returns a deterministic value for testing). */
    double simulatePriceFetch(String symbol) {
        return switch (symbol) {
            case "AAPL" -> 182.50;
            case "MSFT" -> 415.00;
            case "GOOG" -> 172.30;
            default     -> 100.00;
        };
    }

    private static void sleep(long ms) {
        try { TimeUnit.MILLISECONDS.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // demo main
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFutureExamples ex = new CompletableFutureExamples();

        System.out.println("Formatted     : " + ex.fetchFormattedPrice("AAPL").join());
        System.out.println("With CP       : " + ex.fetchTradeWithCounterparty("T001").join());
        System.out.println("Spread        : " + ex.fetchSpread("MSFT").join());
        System.out.println("Batch         : " + ex.enrichTradesBatch(List.of("T1","T2","T3")));
        System.out.println("Fallback      : " + ex.fetchWithFallback("").join());
        System.out.println("Handle ok     : " + ex.fetchWithHandle("T001").join());
        System.out.println("Handle err    : " + ex.fetchWithHandle("INVALID").join());
    }
}
