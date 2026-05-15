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

    /** Fetch employee salary from HR system – blocks on get(). */
    public double fetchSalary_Before(String department) throws Exception {
        java.util.concurrent.Callable<Double> task = () -> simulateSalaryFetch(department);
        java.util.concurrent.ExecutorService exec =
                java.util.concurrent.Executors.newSingleThreadExecutor();
        java.util.concurrent.Future<Double> future = exec.submit(task);
        // Blocks the calling thread until the result is ready
        double salary = future.get();
        exec.shutdown();
        return salary;
    }

    // =========================================================================
    // AFTER – CompletableFuture (Java 8)
    // =========================================================================

    // ---- supplyAsync + thenApply --------------------------------------------

    /**
     * Fetch a salary asynchronously, then format it – non-blocking pipeline.
     * The thread that calls this method is NOT blocked.
     */
    public CompletableFuture<String> fetchFormattedSalary(String department) {
        return CompletableFuture
                .supplyAsync(() -> simulateSalaryFetch(department))    // runs on ForkJoinPool
                .thenApply(salary -> department + " @ " + String.format("%.2f", salary));
    }

    // ---- thenCompose: flat-map two dependent async calls --------------------

    /**
     * First fetch the employee, then asynchronously look up the department.
     * thenCompose avoids nesting: CompletableFuture<CompletableFuture<String>>.
     */
    public CompletableFuture<String> fetchEmployeeWithDepartment(String employeeId) {
        return CompletableFuture
                .supplyAsync(() -> "EMPLOYEE:" + employeeId)
                .thenCompose(employee ->
                        CompletableFuture.supplyAsync(() -> employee + " DEPT:ENGINEERING"));
    }

    // ---- thenCombine: merge two independent async results -------------------

    /**
     * Fetch min and max salaries from two independent sources simultaneously,
     * then combine them into a salary range.
     */
    public CompletableFuture<String> fetchSalaryRange(String department) {
        CompletableFuture<Double> minFuture =
                CompletableFuture.supplyAsync(() -> simulateSalaryFetch(department) - 5_000);
        CompletableFuture<Double> maxFuture =
                CompletableFuture.supplyAsync(() -> simulateSalaryFetch(department) + 5_000);

        return minFuture.thenCombine(maxFuture,
                (min, max) -> String.format("MIN=%.2f MAX=%.2f RANGE=%.2f",
                        min, max, max - min));
    }

    // ---- allOf: wait for a batch of futures ---------------------------------

    /**
     * Enrich all employees in parallel, then collect results.
     * allOf() itself returns CompletableFuture<Void>; you need to join each
     * individual future to retrieve the result.
     */
    public List<String> enrichEmployeesBatch(List<String> employeeIds) {
        List<CompletableFuture<String>> futures = employeeIds.stream()
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
     * Query multiple data sources; return whichever responds first.
     */
    public CompletableFuture<Object> fastestDataSource(String department) {
        CompletableFuture<String> source1 =
                CompletableFuture.supplyAsync(() -> { sleep(50); return "SRC1:" + department; });
        CompletableFuture<String> source2 =
                CompletableFuture.supplyAsync(() -> { sleep(30); return "SRC2:" + department; });
        CompletableFuture<String> source3 =
                CompletableFuture.supplyAsync(() -> { sleep(80); return "SRC3:" + department; });

        return CompletableFuture.anyOf(source1, source2, source3);
    }

    // ---- exceptionally: recover from errors ---------------------------------

    /**
     * Try to fetch an enriched employee; if anything fails, return a fallback value.
     */
    public CompletableFuture<String> fetchWithFallback(String employeeId) {
        return CompletableFuture
                .supplyAsync(() -> {
                    if (employeeId == null || employeeId.isBlank())
                        throw new IllegalArgumentException("employeeId is blank");
                    return "ENRICHED:" + employeeId;
                })
                .exceptionally(ex -> "FALLBACK:" + ex.getMessage());
    }

    // ---- handle: access both result and exception in one callback -----------

    /**
     * handle() runs regardless of success or failure.
     * The result parameter is null on failure; the exception is null on success.
     */
    public CompletableFuture<String> fetchWithHandle(String employeeId) {
        return CompletableFuture
                .supplyAsync(() -> {
                    if ("INVALID".equals(employeeId))
                        throw new RuntimeException("Employee not found: " + employeeId);
                    return "DATA:" + employeeId;
                })
                .handle((result, ex) ->
                        ex != null ? "ERROR[" + ex.getMessage() + "]" : "OK[" + result + "]");
    }

    // ---- whenComplete: observe without transforming -------------------------

    public CompletableFuture<String> fetchWithLogging(String employeeId) {
        return CompletableFuture
                .supplyAsync(() -> "EMPLOYEE:" + employeeId)
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

    /** Simulate a remote salary fetch (returns a deterministic value for testing). */
    double simulateSalaryFetch(String department) {
        return switch (department) {
            case "ENGINEERING" -> 85_000;
            case "MARKETING"   -> 75_000;
            case "SALES"       -> 80_000;
            default            -> 70_000;
        };
    }

    private static void sleep(long ms) {
        try { TimeUnit.MILLISECONDS.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // demo main
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFutureExamples ex = new CompletableFutureExamples();

        System.out.println("Formatted     : " + ex.fetchFormattedSalary("ENGINEERING").join());
        System.out.println("With Dept     : " + ex.fetchEmployeeWithDepartment("E001").join());
        System.out.println("Range         : " + ex.fetchSalaryRange("MARKETING").join());
        System.out.println("Batch         : " + ex.enrichEmployeesBatch(List.of("E1","E2","E3")));
        System.out.println("Fallback      : " + ex.fetchWithFallback("").join());
        System.out.println("Handle ok     : " + ex.fetchWithHandle("E001").join());
        System.out.println("Handle err    : " + ex.fetchWithHandle("INVALID").join());
    }
}
