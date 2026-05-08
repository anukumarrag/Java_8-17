package com.training.bonus.virtualthreads;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * =============================================================================
 * BONUS – VIRTUAL THREADS (JEP 444, Java 21 GA)
 * =============================================================================
 *
 * THEORY
 * ------
 * Traditional Java threads (platform threads) are thin wrappers over OS threads.
 * Creating thousands of platform threads is expensive:
 *   • ~1 MB stack per thread → OOM with 10k concurrent threads.
 *   • OS context-switch overhead dominates at high concurrency.
 *   • Thread-pool sizing becomes a manual, error-prone exercise.
 *
 * VIRTUAL THREADS (Project Loom) are extremely lightweight threads managed
 * by the JVM, not the OS:
 *   • Millions of virtual threads can run on a small pool of carrier (OS) threads.
 *   • Blocking I/O operations yield the carrier thread automatically.
 *   • Written in the familiar Thread / Runnable / blocking-API style.
 *   • No async/await or reactive chains required for high concurrency.
 *   • Memory: a few hundred bytes per thread (vs ~1 MB for platform threads).
 *
 * KEY CREATION PATTERNS
 * ----------------------
 *   Thread.ofVirtual().start(runnable)         – one-off virtual thread
 *   Thread.startVirtualThread(runnable)        – shortcut for one-off
 *   Executors.newVirtualThreadPerTaskExecutor()– one virtual thread per task
 *   Thread.ofVirtual().name("name").start(...) – named virtual thread
 *
 * WHEN TO USE
 * -----------
 *  ✔ I/O-bound workloads: HTTP calls, DB queries, file reads – anything that
 *    blocks waiting for external resources.
 *  ✔ High-concurrency servers: replace fixed-size thread pools with
 *    virtual-thread-per-request model ("thread-per-request" style).
 *  ✘ CPU-bound tasks: virtual threads don't help; still use ForkJoinPool.
 *
 * REQUIRES: Java 21
 */
public class VirtualThreadsExamples {

    // =========================================================================
    // BEFORE – Platform threads (Java 17 and earlier)
    // =========================================================================

    /**
     * Process N trade enrichment tasks using a fixed-size platform thread pool.
     * With a pool of 10, tasks queue up: throughput is capped at pool size.
     */
    public List<String> enrichTrades_Before(List<String> tradeIds) throws Exception {
        List<String> results = new ArrayList<>();
        try (ExecutorService exec = Executors.newFixedThreadPool(10)) {
            List<Future<String>> futures = new ArrayList<>();
            for (String id : tradeIds) {
                futures.add(exec.submit(() -> {
                    simulateIoWork(5);   // e.g., DB lookup
                    return id + ":ENRICHED_PT";
                }));
            }
            for (var f : futures) results.add(f.get());
        }
        return results;
    }

    // =========================================================================
    // AFTER – Virtual threads (Java 21)
    // =========================================================================

    // ---- Pattern 1: one virtual thread per task (most common) --------------

    /**
     * Exact same logic – just swap the executor.
     * newVirtualThreadPerTaskExecutor() creates a new virtual thread for every
     * submitted task.  Thousands of tasks run concurrently without blocking.
     */
    public List<String> enrichTrades_After(List<String> tradeIds) throws Exception {
        List<String> results = new ArrayList<>();
        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> futures = new ArrayList<>();
            for (String id : tradeIds) {
                futures.add(exec.submit(() -> {
                    simulateIoWork(5);   // yields the carrier thread while waiting
                    return id + ":ENRICHED_VT";
                }));
            }
            for (var f : futures) results.add(f.get());
        }
        return results;
    }

    // ---- Pattern 2: Thread.ofVirtual().start() ------------------------------

    /**
     * Start a single named virtual thread.
     * Useful for ad-hoc background tasks.
     */
    public Thread startVirtualTask(String tradeId, Runnable task) {
        return Thread.ofVirtual()
                .name("trade-processor-" + tradeId)
                .start(task);
    }

    // ---- Pattern 3: Thread.startVirtualThread shortcut ----------------------

    public Thread startSimpleVirtualThread(Runnable r) {
        return Thread.startVirtualThread(r);
    }

    // ---- Pattern 4: Check if current thread is virtual ----------------------

    public boolean isRunningOnVirtualThread() {
        return Thread.currentThread().isVirtual();
    }

    /**
     * Submit a task to a virtual thread and check whether it ran as virtual.
     */
    public boolean taskRanOnVirtualThread() throws Exception {
        try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
            return exec.submit(() -> Thread.currentThread().isVirtual()).get();
        }
    }

    // =========================================================================
    // Real-world pattern: fan-out I/O (call N external services in parallel)
    // =========================================================================

    /** Simulate calling a pricing service for each symbol. */
    public List<String> fetchPricesInParallel(List<String> symbols) throws Exception {
        try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> futures = symbols.stream()
                    .map(sym -> exec.submit(() -> {
                        simulateIoWork(10);   // simulate HTTP call
                        return sym + "=182.50";
                    }))
                    .toList();

            List<String> prices = new ArrayList<>();
            for (var f : futures) prices.add(f.get());
            return prices;
        }
    }

    // =========================================================================
    // Thread locals with virtual threads
    // =========================================================================

    private static final ThreadLocal<String> TRADE_CONTEXT = new ThreadLocal<>();

    /**
     * ThreadLocal works exactly the same with virtual threads.
     * Prefer ScopedValues (JEP 446, Java 21 preview) for new code.
     */
    public String runWithContext(String tradeId) throws Exception {
        try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
            return exec.submit(() -> {
                TRADE_CONTEXT.set(tradeId);
                String ctx = TRADE_CONTEXT.get();
                TRADE_CONTEXT.remove();
                return "Context: " + ctx;
            }).get();
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    void simulateIoWork(long millis) {
        try { Thread.sleep(Duration.ofMillis(millis)); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // demo main
    public static void main(String[] args) throws Exception {
        VirtualThreadsExamples ex = new VirtualThreadsExamples();

        System.out.println("=== Virtual Thread Facts ===");
        System.out.println("Is main virtual? " + Thread.currentThread().isVirtual());

        // Run a quick virtual thread
        Thread vt = Thread.ofVirtual().name("demo").start(() -> {
            System.out.println("Running in virtual thread: " + Thread.currentThread().isVirtual());
        });
        vt.join();

        System.out.println("=== Enrich Trades (virtual) ===");
        var ids     = List.of("T1", "T2", "T3", "T4", "T5");
        var results = ex.enrichTrades_After(ids);
        results.forEach(System.out::println);

        System.out.println("=== Fetch prices in parallel ===");
        var prices = ex.fetchPricesInParallel(List.of("AAPL", "MSFT", "GOOG"));
        prices.forEach(System.out::println);

        System.out.println("taskRanOnVirtualThread: " + ex.taskRanOnVirtualThread());
    }
}
