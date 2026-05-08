package com.training.bonus.virtualthreads;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Bonus – Virtual Threads (Java 21)")
class VirtualThreadsExamplesTest {

    private VirtualThreadsExamples ex;

    @BeforeEach
    void setUp() { ex = new VirtualThreadsExamples(); }

    // ---- enrichTrades_After -------------------------------------------------

    @Test
    @DisplayName("enrichTrades_After: all trades are enriched with :ENRICHED_VT suffix")
    void enrichTrades_After_allEnriched() throws Exception {
        var ids    = List.of("T1", "T2", "T3");
        var result = ex.enrichTrades_After(ids);
        assertEquals(3, result.size());
        assertTrue(result.contains("T1:ENRICHED_VT"));
        assertTrue(result.contains("T2:ENRICHED_VT"));
        assertTrue(result.contains("T3:ENRICHED_VT"));
    }

    @Test
    @DisplayName("enrichTrades_After: empty list returns empty result")
    void enrichTrades_After_emptyList() throws Exception {
        assertTrue(ex.enrichTrades_After(List.of()).isEmpty());
    }

    @Test
    @DisplayName("enrichTrades_Before and After produce equivalent IDs")
    void enrichTrades_beforeAndAfterEquivalent() throws Exception {
        var ids = List.of("T1", "T2");
        var before = ex.enrichTrades_Before(ids);
        var after  = ex.enrichTrades_After(ids);
        assertEquals(before.size(), after.size());
        for (int i = 0; i < before.size(); i++) {
            // Both contain the trade id, just with different suffix
            assertTrue(before.get(i).startsWith("T"));
            assertTrue(after.get(i).startsWith("T"));
        }
    }

    // ---- taskRanOnVirtualThread ---------------------------------------------

    @Test
    @DisplayName("taskRanOnVirtualThread: tasks submitted to virtual executor run as virtual")
    void taskRanOnVirtualThread_isVirtual() throws Exception {
        assertTrue(ex.taskRanOnVirtualThread(),
                "tasks on newVirtualThreadPerTaskExecutor should run on virtual threads");
    }

    // ---- startVirtualTask ---------------------------------------------------

    @Test
    @DisplayName("startVirtualTask: starts a virtual thread that runs the task")
    void startVirtualTask_runsTask() throws InterruptedException {
        AtomicBoolean ran = new AtomicBoolean(false);
        Thread vt = ex.startVirtualTask("T999", () -> ran.set(true));
        vt.join(5000);
        assertTrue(ran.get(), "task should have run");
        assertTrue(vt.isVirtual(), "thread should be virtual");
    }

    // ---- startSimpleVirtualThread -------------------------------------------

    @Test
    @DisplayName("startSimpleVirtualThread: thread is virtual")
    void startSimpleVirtualThread_isVirtual() throws InterruptedException {
        AtomicBoolean isVirtual = new AtomicBoolean(false);
        Thread vt = ex.startSimpleVirtualThread(() ->
                isVirtual.set(Thread.currentThread().isVirtual()));
        vt.join(5000);
        assertTrue(isVirtual.get(), "thread started with startVirtualThread should be virtual");
    }

    // ---- fetchPricesInParallel ----------------------------------------------

    @Test
    @DisplayName("fetchPricesInParallel: returns one price entry per symbol")
    void fetchPricesInParallel_returnsAllPrices() throws Exception {
        var symbols = List.of("AAPL", "MSFT", "GOOG");
        var prices  = ex.fetchPricesInParallel(symbols);
        assertEquals(3, prices.size());
        assertTrue(prices.stream().anyMatch(p -> p.startsWith("AAPL=")));
        assertTrue(prices.stream().anyMatch(p -> p.startsWith("MSFT=")));
        assertTrue(prices.stream().anyMatch(p -> p.startsWith("GOOG=")));
    }

    // ---- runWithContext (ThreadLocal) ----------------------------------------

    @Test
    @DisplayName("runWithContext: ThreadLocal works correctly in virtual threads")
    void runWithContext_threadLocalWorks() throws Exception {
        String result = ex.runWithContext("T001");
        assertEquals("Context: T001", result);
    }
}
