# Day 5 — Bonus: The Future Is Now
## *Java 18–21 — Virtual Threads · Structured Concurrency · Record Patterns · Sequenced Collections*

**Duration:** 1.5 hours  
**Source files:** `bonus/virtualthreads` · `bonus/structuredconcurrency` · `bonus/recordpatterns` · `bonus/patternswitch` · `bonus/sequencedcollections` · `bonus/unnamedvariables`  
**Requires:** JDK 21

---

---

## 🎯 Opening Hook — *The Platform Is Accelerating* `[5 min]`

> *"Java 17 is your production foundation. But the language didn't stop there.  
> Everything we cover today is either already GA in Java 21 — the next LTS —  
> or landing in the very next release. Your upgrade path from 17 to 21 is direct.  
> Let's see what's waiting for you on the other side."*

### Java 18–21 Timeline

```
Java 18  ──── 2022 ── UTF-8 by default, Simple Web Server, JavaDoc code snippets
Java 19  ──── 2022 ── Virtual Threads (preview), Structured Concurrency (incubator)
Java 20  ──── 2023 ── Record Patterns (preview 2), Pattern Matching in switch (preview 4)
Java 21  ──── 2023 ── LTS ── Virtual Threads GA, Record Patterns GA,
                             Pattern Matching in switch GA, Sequenced Collections GA,
                             Structured Concurrency (preview)
Java 22  ──── 2024 ── Unnamed Variables GA (JEP 456)
```

> **Migration path:** Java 8 → **11** (LTS) → **17** (LTS) → **21** (LTS)

---

---

## 📖 Theory Block 1 — Virtual Threads `[20 min]`

**JEP 444, GA in Java 21**

### The Scalability Problem with Platform Threads

```
Platform Thread = OS Thread
  ├── ~1 MB stack allocation per thread
  ├── OS context switch (~1 µs overhead)
  ├── Typical server: 200–500 concurrent threads before OOM
  └── Thread-per-request model hits ceiling at ~500 rps
```

```java
// This is expensive — fixed pool of 200 platform threads caps your concurrency
ExecutorService exec = Executors.newFixedThreadPool(200);
```

---

### Virtual Threads — Project Loom

```
Virtual Thread = JVM-managed lightweight thread
  ├── A few hundred bytes per thread (vs ~1 MB)
  ├── Mounted on a carrier (OS) thread — unmounts automatically when blocking
  ├── Millions of virtual threads on a handful of carrier threads
  └── Thread-per-request model scales to millions of rps
```

```java
// Drop-in replacement — same API, massive scalability improvement
ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();
```

---

### Before vs After — Trade Enrichment

```java
// BEFORE — Fixed thread pool, throughput capped at pool size
public List<String> enrichTrades_Before(List<String> tradeIds) throws Exception {
    List<String> results = new ArrayList<>();
    try (ExecutorService exec = Executors.newFixedThreadPool(10)) {
        List<Future<String>> futures = new ArrayList<>();
        for (String id : tradeIds) {
            futures.add(exec.submit(() -> {
                simulateIoWork(5);   // blocks the platform thread!
                return id + ":ENRICHED";
            }));
        }
        for (var f : futures) results.add(f.get());
    }
    return results;
}
```

```java
// AFTER — Virtual thread per task; 10,000 tasks as easily as 10
public List<String> enrichTrades_After(List<String> tradeIds) throws Exception {
    List<String> results = new ArrayList<>();
    try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
        List<Future<String>> futures = new ArrayList<>();
        for (String id : tradeIds) {
            futures.add(exec.submit(() -> {
                simulateIoWork(5);   // yields the carrier thread — no blocking!
                return id + ":ENRICHED";
            }));
        }
        for (var f : futures) results.add(f.get());
    }
    return results;
}
```

**Source:** `VirtualThreadsExamples.enrichTrades_Before` / `enrichTrades_After`

---

### All Virtual Thread Creation Patterns

```java
// Pattern 1: one virtual thread per task (most common in servers)
ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();

// Pattern 2: named virtual thread
Thread vt = Thread.ofVirtual()
        .name("trade-processor-" + tradeId)
        .start(() -> process(tradeId));

// Pattern 3: shortcut
Thread vt = Thread.startVirtualThread(() -> process(tradeId));

// Pattern 4: check if running on virtual thread
boolean isVirtual = Thread.currentThread().isVirtual();  // → true
```

---

### Fan-Out I/O Pattern

```java
// Fetch prices for 1,000 symbols in parallel — one virtual thread per symbol
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
```

---

### When to Use Virtual Threads

| ✅ Great fit | ❌ Wrong fit |
|-------------|------------|
| HTTP server — one request per thread | CPU-bound tasks (sorting, encryption) |
| DB queries — each waits for a result set | Tight compute loops |
| External API calls — fan-out / fan-in | Tasks that never block |
| File I/O | Replacing `ForkJoinPool` for parallelism |

> **Rule of thumb:** if the task *blocks waiting for something external*, virtual threads help.
> If it burns CPU continuously, they don't.

---

---

## 📖 Theory Block 2 — Structured Concurrency `[15 min]`

**JEP 453, preview in Java 21**

### The Problem with Unstructured Concurrency

```java
// CompletableFuture — tasks can "escape" their scope
CompletableFuture<String> cpFuture = supplyAsync(() -> fetchCounterparty(id));
CompletableFuture<Double> priceFuture = supplyAsync(() -> fetchPrice(symbol));

// If fetchCounterparty throws, priceFuture still runs!
// Manual cancellation required. Stack traces lose context.
String cp    = cpFuture.join();
double price = priceFuture.join();
```

---

### StructuredTaskScope — Lifecycle-Bound Concurrency

```java
// ShutdownOnFailure: if ANY task fails, cancel the others immediately
public EnrichedTrade enrich(TradeData trade) throws Exception {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

        var cpTask    = scope.fork(() -> fetchCounterparty(trade.id()));
        var priceTask = scope.fork(() -> fetchPrice(trade.symbol()));

        scope.join()           // wait until all tasks complete or one fails
             .throwIfFailed(); // re-throw first exception if any failed

        // At this point BOTH tasks succeeded
        return new EnrichedTrade(trade, cpTask.get(), priceTask.get());
    }
    // scope.close() — all tasks are guaranteed done or cancelled here
}
```

**Source:** `StructuredConcurrencyExamples.enrich_After`

---

### ShutdownOnSuccess — Race to the First Result

```java
// Query 3 pricing sources — use whichever responds first
public double fastestPriceSource(String symbol) throws Exception {
    try (var scope = new StructuredTaskScope.ShutdownOnSuccess<Double>()) {

        scope.fork(() -> { simulateDelay(50); return fetchPrice(symbol); });
        scope.fork(() -> { simulateDelay(30); return fetchPrice(symbol); });  // wins
        scope.fork(() -> { simulateDelay(80); return fetchPrice(symbol); });

        scope.join();
        return scope.result();  // returns the first successful result
    }
}
```

**Source:** `StructuredConcurrencyExamples.fastestPriceSource`

---

### Structured vs Unstructured — Key Differences

| | `CompletableFuture` | `StructuredTaskScope` |
|-|--------------------|-----------------------|
| Task lifetime | Can outlive owner | Bounded by scope's `try` block |
| Cancellation | Manual | Automatic on scope exit |
| Error propagation | `.exceptionally()` callback | `throwIfFailed()` re-throws |
| Thread | Any | Always virtual threads |
| Stack traces | Fragmented | Full parent–child context |

---

---

## 📖 Theory Block 3 — Record Patterns + Pattern Matching in Switch `[20 min]`

**JEP 440 (Record Patterns) + JEP 441 (Switch Patterns), both GA in Java 21**

### Record Patterns — Destructuring in `instanceof`

```java
// Java 17 — bind a pattern variable, then call accessors
if (obj instanceof Trade t) {
    System.out.println(t.id() + " " + t.symbol());
}

// Java 21 — deconstruct the record inline
if (obj instanceof Trade(String id, String symbol, Money notional, Counterparty cp)) {
    System.out.println(id + " " + symbol);   // no t.id() call needed
}
```

---

### Nested Deconstruction

```java
// Single expression deconstructs Trade → Counterparty → Address → city
if (obj instanceof Trade(String id, _, _, Counterparty(_, _, Address(_, String city, _)))) {
    return "Trade " + id + " is in " + city;
}
```

> Compare to the Java 17 version (four nested null-check blocks to extract the same `city`).

**Source:** `RecordPatternsExamples.describeTradeCity_After`

---

### Pattern Matching in Switch — The Payoff

```java
// Java 21 — switch on types, exhaustive for sealed types, no default needed
public String processEvent(TradeEvent event) {
    return switch (event) {
        case TradeEvent.Created(String id, String sym, double qty)
                -> "New trade %s: %s x %.0f".formatted(id, sym, qty);
        case TradeEvent.Priced(String id, Money(double amt, String cur))
                -> "Trade %s priced at %.2f %s".formatted(id, amt, cur);
    };
}
```

> **This is the payoff for all the groundwork:**
> - Records → clean data carriers with known components
> - Sealed → the compiler knows every case
> - Pattern matching → deconstruct inline
> - Switch expression → return a value, exhaustive

**Source:** `RecordPatternsExamples.processEvent`, `PatternMatchingSwitchExamples.java`

---

### Guarded Patterns with `when`

```java
// 'when' replaces the && guard from Java 17
String result = switch (obj) {
    case Money(double amt, String cur) when amt > 1_000_000 && "USD".equals(cur)
            -> "Large USD payment: " + amt;
    case Money(double amt, String cur) when amt > 0
            -> "Standard %s payment: %.2f".formatted(cur, amt);
    case Money(_, _)
            -> "Zero or negative payment";
    default -> "Not a Money object";
};
```

**Source:** `RecordPatternsExamples.classifyPayment`

---

### Area Calculator — Sealed + Records + Pattern Switch

```java
public sealed interface Shape permits Shape.Circle, Shape.Rectangle, Shape.Triangle {
    record Circle(double radius)                   implements Shape {}
    record Rectangle(double width, double height)  implements Shape {}
    record Triangle(double base, double height)    implements Shape {}
}

public double area(Shape shape) {
    return switch (shape) {
        case Shape.Circle(double r)              -> Math.PI * r * r;
        case Shape.Rectangle(double w, double h) -> w * h;
        case Shape.Triangle(double b, double h)  -> 0.5 * b * h;
    };
    // Exhaustive — adding a new Shape variant → compile error until handled
}
```

**Source:** `RecordPatternsExamples.area`

---

---

## 📖 Theory Block 4 — Sequenced Collections + Unnamed Variables `[10 min]`

### Sequenced Collections (JEP 431, Java 21)

**The Problem:** No unified way to access first/last element of ordered collections.

```java
// Java 17 — different APIs for each collection type
list.get(0);                     // List — index 0
list.get(list.size() - 1);       // List — last element
deque.peekFirst();               // Deque
deque.peekLast();                // Deque
linkedHashSet.iterator().next(); // LinkedHashSet — no clean last()
```

**The Solution:** New interfaces `SequencedCollection`, `SequencedSet`, `SequencedMap`

```java
// Java 21 — uniform API for all ordered collections
list.getFirst();          // List, Deque, LinkedHashSet, LinkedHashMap
list.getLast();
list.reversed();          // returns a reversed view

// Map equivalents
map.firstEntry();
map.lastEntry();
map.sequencedEntrySet().reversed();
```

**Source:** `SequencedCollectionsExamples.java`

---

### Unnamed Variables `_` (JEP 456, Java 22 GA)

```java
// Before — forced to name variables you don't use
try {
    process(trade);
} catch (IOException e) {   // 'e' is never used — but you had to name it
    log("I/O error");
}

// After — explicit intent: "I'm intentionally ignoring this"
try {
    process(trade);
} catch (IOException _) {
    log("I/O error");
}
```

```java
// Unnamed in switch — ignore components you don't need
return switch (event) {
    case Created(String id, _, _)  -> "Created: " + id;
    case Priced(String id, Money _) -> "Priced: " + id;
};

// Unnamed in for-each (counting iterations)
int count = 0;
for (var _ : trades) count++;
```

**Source:** `UnnamedVariablesExamples.java`

---

---

## 💻 Hands-On — Putting It All Together `[15 min]`

**Goal:** Apply Java 21 features to upgrade the `ModernTransactionService` from Day 4.

### Task A — Record Patterns in switch `[8 min]`

```java
// Day 4 version (Java 17 style)
public String processEvent(TradeEvent event) {
    return switch (event) {
        case TradeCreatedEvent  c -> "Created: "  + c.tradeId() + " | " + c.symbol();
        case TradeExecutedEvent e -> "Executed: " + e.tradeId() + " at " + e.executedPrice();
        case TradeRejectedEvent r -> "Rejected: " + r.tradeId() + " – " + r.rejectionReason();
        case TradeUpdatedEvent  u -> "Updated: "  + u.tradeId() + " notional=" + u.newNotional();
    };
}

// Java 21 upgrade: deconstruct the record components inline
// Hint: case TradeCreatedEvent(String id, String sym, ...) ->
```

### Task B — Structured Concurrency for enrichment `[7 min]`

```java
// Refactor this sequential enrichment to use StructuredTaskScope.ShutdownOnFailure
// so counterparty and price lookups run in parallel
public EnrichedTrade enrichTrade(String tradeId, String symbol) throws Exception {
    String counterparty = fetchCounterparty(tradeId);  // sequential
    double price        = fetchPrice(symbol);           // sequential
    return new EnrichedTrade(tradeId, symbol, counterparty, price);
}

// After: use scope.fork() for both, scope.join().throwIfFailed(), then .get()
```

<details>
<summary>💡 Reveal Task A solution</summary>

```java
public String processEvent(TradeEvent event) {
    return switch (event) {
        case TradeCreatedEvent(String id, String sym, double notional, String cp, _, _)
                -> "Created: " + id + " | " + sym + " notional=" + notional;
        case TradeExecutedEvent(String id, _, _, double price, _)
                -> "Executed: " + id + " at " + price;
        case TradeRejectedEvent(String id, _, String reason)
                -> "Rejected: " + id + " – " + reason;
        case TradeUpdatedEvent(String id, double newNotional, _)
                -> "Updated: " + id + " notional=" + newNotional;
    };
}
```
</details>

<details>
<summary>💡 Reveal Task B solution</summary>

```java
public EnrichedTrade enrichTrade(String tradeId, String symbol) throws Exception {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        var cpTask    = scope.fork(() -> fetchCounterparty(tradeId));
        var priceTask = scope.fork(() -> fetchPrice(symbol));

        scope.join().throwIfFailed();

        return new EnrichedTrade(tradeId, symbol, cpTask.get(), priceTask.get());
    }
}
```
</details>

---

---

## 🏆 Closing Ceremony `[5 min]`

### The Migration Roadmap

```
Java 8  ──► Java 11 (LTS)  ──► Java 17 (LTS)  ──► Java 21 (LTS)
        Modules              Records            Virtual Threads
        var                  Text Blocks        Record Patterns
        List.of()            Sealed Classes     Pattern Switch
        new HttpClient       Pattern Matching   Structured Concurrency
        String enhancements  Helpful NPE        Sequenced Collections
```

> **Recommendation:** Target Java 21 for new projects today.
> For existing projects: migrate to 17 first (LTS), then 21.

---

### The Full Feature Cheat Sheet

> *[See `cheat-sheet.md`]*

---

### Resources to Keep Learning

| Resource | What it covers |
|----------|---------------|
| [JEP Index](https://openjdk.org/jeps/) | Every Java Enhancement Proposal with full details |
| [Inside Java Podcast](https://inside.java/podcast) | Java language team members discussing new features |
| [JEP Café (YouTube)](https://www.youtube.com/@Java) | José Paumard's video deep-dives on each JEP |
| [foojay.io](https://foojay.io) | Community articles, migration guides, tooling tips |
| [Dev.java](https://dev.java) | Official Java learning platform with tutorials |

---

### 🎓 You've Completed the Programme

> Over the past 5 days you've covered:
> - Java 8: Lambdas, Streams, Optional
> - Java 9–14: var, Collections, String API, CompletableFuture, HttpClient, Switch Expressions
> - Java 15–16: Records, Text Blocks, Helpful NPE, Pattern Matching
> - Java 17: Sealed Classes — the full picture
> - Java 21: Virtual Threads, Record Patterns, Pattern Matching in Switch, Structured Concurrency
>
> **Every concept is backed by runnable code in this repository. Run `mvn test` to verify it all works.**

---

> **End of Day 5 (Bonus)**  
> Source: `bonus/src/main/java/com/training/bonus/`  
> Tests: `bonus/src/test/java/com/training/bonus/`  
> Run: `cd bonus && mvn test`
