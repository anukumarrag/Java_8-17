package com.training.bonus.structuredconcurrency;

import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.ShutdownOnFailure;
import java.util.concurrent.StructuredTaskScope.ShutdownOnSuccess;

/**
 * =============================================================================
 * BONUS – STRUCTURED CONCURRENCY (JEP 453, Java 21 preview)
 * =============================================================================
 *
 * THEORY
 * ------
 * Structured Concurrency treats a group of related tasks as a single unit of
 * work – analogous to how structured programming treats blocks of code.
 *
 * With traditional CompletableFuture or ExecutorService:
 *   • Tasks are "fire-and-forget" – they can outlive their owner.
 *   • Cancellation requires manual propagation.
 *   • Partial failure is hard to handle correctly.
 *   • Stack traces don't reveal the full context of concurrent failures.
 *
 * With StructuredTaskScope:
 *   • Tasks are started inside a scope that OWNS them.
 *   • When the scope exits (close()), all tasks are guaranteed to have completed
 *     or been cancelled.
 *   • Two built-in policies:
 *       ShutdownOnFailure  – cancel all tasks as soon as ONE fails.
 *       ShutdownOnSuccess  – cancel all tasks as soon as ONE succeeds.
 *   • Errors propagate naturally via throwIfFailed().
 *
 * KEY API
 * -------
 *   try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
 *       var task1 = scope.fork(() -> fetchA());
 *       var task2 = scope.fork(() -> fetchB());
 *       scope.join().throwIfFailed();          // wait, then throw on first error
 *       return combine(task1.get(), task2.get());
 *   }
 *
 * STATUS (as of Java 21 / 22)
 * ----------------------------
 * Still a PREVIEW feature. To compile and run, add:
 *   mvn compile -Dmaven.compiler.compilerArgs=--enable-preview
 *   java --enable-preview ...
 *
 * REQUIRES: Java 21 + --enable-preview
 *
 * NOTE: This file shows the API patterns with commented-out code where
 *       the preview API calls are not yet stable across versions.
 *       Uncomment and enable preview to run.
 */
public class StructuredConcurrencyExamples {

    // =========================================================================
    // Domain model
    // =========================================================================

    public record TradeData(String id, String symbol) {}
    public record EnrichedTrade(TradeData trade, String counterparty, double price) {}

    // =========================================================================
    // BEFORE – CompletableFuture (Java 17)
    // =========================================================================

    /**
     * Fetch trade and price in parallel using CompletableFuture.
     * Problems:
     *   – If fetchCounterparty() fails, fetchPrice() may still be running.
     *   – Cancellation of the other task must be done manually.
     *   – Exception handling is verbose.
     */
    public EnrichedTrade enrich_Before(TradeData trade) throws Exception {
        var cpFuture    = java.util.concurrent.CompletableFuture.supplyAsync(
                () -> fetchCounterparty(trade.id()));
        var priceFuture = java.util.concurrent.CompletableFuture.supplyAsync(
                () -> fetchPrice(trade.symbol()));

        // Both run in parallel; join waits for both
        String cp    = cpFuture.join();
        double price = priceFuture.join();
        return new EnrichedTrade(trade, cp, price);
    }

    // =========================================================================
    // AFTER – StructuredTaskScope (Java 21 preview)
    // =========================================================================

    /**
     * ShutdownOnFailure: if ANY forked task fails, cancel the others immediately.
     * After join(), call throwIfFailed() to re-throw the first exception.
     * task.get() is safe only after join() + throwIfFailed() succeed.
     *
     * NOTE: Requires --enable-preview to compile/run.
     */
    public EnrichedTrade enrich_After(TradeData trade) throws Exception {
        try (var scope = new ShutdownOnFailure()) {
            var cpTask    = scope.fork(() -> fetchCounterparty(trade.id()));
            var priceTask = scope.fork(() -> fetchPrice(trade.symbol()));

            scope.join()           // wait until all tasks complete or any fails
                 .throwIfFailed(); // re-throw the first exception if any task failed

            // At this point both tasks succeeded
            return new EnrichedTrade(trade, cpTask.get(), priceTask.get());
        }
    }

    /**
     * ShutdownOnSuccess: as soon as ANY task returns a result, cancel the rest
     * and return that result.
     * USE CASE: query multiple pricing sources and use whichever responds first.
     */
    public double fastestPriceSource(String symbol) throws Exception {
        try (var scope = new ShutdownOnSuccess<Double>()) {
            scope.fork(() -> { simulateDelay(50);  return fetchPrice(symbol); });
            scope.fork(() -> { simulateDelay(30);  return fetchPrice(symbol); });
            scope.fork(() -> { simulateDelay(80);  return fetchPrice(symbol); });

            scope.join();          // wait until one succeeds or all fail
            return scope.result(); // throws NoSuchElementException if all failed
        }
    }

    // =========================================================================
    // Helpers (simulated I/O)
    // =========================================================================

    String fetchCounterparty(String tradeId) {
        simulateDelay(20);
        return "CP_" + tradeId;
    }

    double fetchPrice(String symbol) {
        simulateDelay(15);
        return switch (symbol) {
            case "AAPL" -> 182.50;
            case "MSFT" -> 415.00;
            default     -> 100.00;
        };
    }

    void simulateDelay(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // demo main
    public static void main(String[] args) throws Exception {
        StructuredConcurrencyExamples ex = new StructuredConcurrencyExamples();

        var trade = new TradeData("T001", "AAPL");

        System.out.println("=== ShutdownOnFailure (both succeed) ===");
        var enriched = ex.enrich_After(trade);
        System.out.printf("Trade: %s CP=%s Price=%.2f%n",
                enriched.trade().id(), enriched.counterparty(), enriched.price());

        System.out.println("=== ShutdownOnSuccess (fastest source) ===");
        double price = ex.fastestPriceSource("MSFT");
        System.out.printf("Fastest price for MSFT: %.2f%n", price);
    }
}
