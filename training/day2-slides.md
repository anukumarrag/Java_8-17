# Day 2 — The Evolution Years
## *Java 9–14 Essentials*

**Duration:** 1.5 hours  
**Source files:** `module1/var` · `module1/datetime` · `module1/string` · `module1/httpclient` · `module1/concurrent` · `module1/collections` · `module2/switchexpressions`

---

---

## 🎯 Opening Hook — *The Silent Revolution* `[5 min]`

> *"Most teams jumped straight from Java 8 to Java 17 and missed six years of improvements.  
> Today we fill that gap — and you'll wonder how you lived without these."*

### Java Release Timeline

```
Java 8  ──── 2014 ── LTS ── Lambdas, Streams, Optional, Date/Time API
Java 9  ──── 2017 ──── Module System, Collection Factories, Stream enhancements
Java 10 ──── 2018 ──── var (local variable type inference)
Java 11 ──── 2018 ── LTS ── New HttpClient, String API enhancements
Java 12 ──── 2019 ──── Switch Expressions (preview)
Java 13 ──── 2019 ──── Text Blocks (preview)
Java 14 ──── 2020 ──── Switch Expressions GA, Helpful NPE (preview)
Java 15 ──── 2020 ──── Text Blocks GA, Helpful NPE default-on
Java 16 ──── 2021 ──── Records GA, Pattern Matching GA
Java 17 ──── 2021 ── LTS ── Sealed Classes GA
```

> **Key insight:** The 6-month release cadence means smaller, safer upgrades.
> LTS versions (8 → 11 → 17 → 21) are your migration waypoints.

---

---

## 📖 Theory Block 1 — `var` & Local Variable Type Inference `[15 min]`

**Java 10, JEP 286**

### What Is It?

```java
// Before (Java 9)
Map<String, List<Trade>> result = new HashMap<String, List<Trade>>();

// After (Java 10) — compiler infers the type from the right-hand side
var result = new HashMap<String, List<Trade>>();
```

> `var` is **not** `Object`. The type is still statically known at compile time.
> It is just the *declaration* that becomes shorter.

---

### Rules — Where `var` Works

```java
// ✅ Local variable with initialiser
var total = quantity * price;       // inferred: double
var currency = "USD";               // inferred: String
var reader = new BufferedReader(…); // inferred: BufferedReader

// ✅ For-each loop variable
for (var trade : trades) { … }

// ✅ Try-with-resources
try (var conn = dataSource.getConnection()) { … }

// ✅ Lambda parameter (Java 11) — enables annotations
.filter((var s) -> s != null && !s.isBlank())
```

---

### Rules — Where `var` Does NOT Work

```java
// ❌ Method parameter — compile error
public void process(var trade) { … }

// ❌ Return type — compile error
public var getTrade() { … }

// ❌ Field — compile error
class Service { var name = "Trading"; }

// ❌ No initialiser — compile error
var x;

// ❌ Initialised to null — type cannot be inferred
var cp = null;
```

---

### When to Use `var` — Guidelines

| ✅ Good fit | ❌ Bad fit |
|------------|-----------|
| Type is obvious from the RHS | Type is unclear without context |
| Constructor call: `var map = new HashMap<>()` | Complex expression: `var x = doSomething()` |
| Long generic type: `Map<String, List<Trade>>` | Short type already: `var i = 0` (just use `int`) |
| For-each over a typed collection | Return value of a method you need to look up |

---

### Before vs After

```java
// Before
Map<String, List<Trade>> result = new HashMap<>();
for (Trade trade : trades) {
    List<Trade> group = result.computeIfAbsent(trade.status(), k -> new ArrayList<>());
    group.add(trade);
}

// After — same logic, less noise
var result = new HashMap<String, List<Trade>>();
for (var trade : trades) {
    var group = result.computeIfAbsent(trade.status(), k -> new ArrayList<>());
    group.add(trade);
}
```

**Source:** `LocalVarInferenceExamples.groupByStatus_After`

---

---

## 📖 Theory Block 2 — String API Enhancements `[10 min]`

**Java 11 additions**

### New Methods at a Glance

```java
// isBlank() — true if empty or all whitespace (Unicode-aware)
"   ".isBlank()              // → true
"  x".isBlank()              // → false

// strip() — trim() but Unicode-aware (trim() only handles ASCII ≤ 0x20)
"  hello  ".strip()          // → "hello"
"\u2000hello\u2000".strip()  // → "hello"  (strip() handles it; trim() does NOT)

// stripLeading / stripTrailing
"  hello  ".stripLeading()   // → "hello  "
"  hello  ".stripTrailing()  // → "  hello"

// lines() — split on line terminators, returns Stream<String>
"line1\nline2\nline3".lines().count()  // → 3

// repeat(n)
"-".repeat(40)               // → "----------------------------------------"
```

**Source:** `StringApiEnhancementsExamples.java`

---

### Real-world Example

```java
// Parse a CSV config — skip blank lines and trim each field
List<String> symbols = configText.lines()
        .filter(line -> !line.isBlank())
        .map(String::strip)
        .filter(line -> !line.startsWith("#"))
        .collect(Collectors.toList());
```

---

---

## 📖 Theory Block 3 — Collection Factory Methods `[10 min]`

**Java 9**

### The Problem — Creating Small Immutable Collections

```java
// Before — verbose, 5 lines for a 3-element set
Set<String> set = new HashSet<>();
set.add("AAPL");
set.add("MSFT");
set.add("GOOG");
Set<String> immutable = Collections.unmodifiableSet(set);
```

### The Solution

```java
// After — Java 9 factory methods (immutable by definition)
List<String> symbols  = List.of("AAPL", "MSFT", "GOOG");
Set<String>  statusSet = Set.of("PENDING", "EXECUTED", "SETTLED");
Map<String, Integer> slaMap = Map.of(
        "EQUITY",       2,
        "FIXED_INCOME", 1,
        "FOREX",        1
);
```

**Source:** `CollectionEnhancementsExamples.java`

---

### Gotchas

| Behaviour | `List.of` / `Set.of` / `Map.of` |
|-----------|--------------------------------|
| Null elements | ❌ Throws `NullPointerException` — by design |
| Mutation (`add`, `put`, `remove`) | ❌ Throws `UnsupportedOperationException` |
| Duplicate keys in `Map.of` | ❌ Throws `IllegalArgumentException` at runtime |
| `Set.of` element order | Not guaranteed |
| `List.of` element order | Guaranteed (insertion order) |

---

### Java 10 — `Collectors.toUnmodifiableList()`

```java
// Before (Java 8) — still mutable
List<String> list = stream.collect(Collectors.toList());

// After (Java 10) — immutable result
List<String> list = stream.collect(Collectors.toUnmodifiableList());

// Even more idiomatic (Java 16+)
List<String> list = stream.toList();  // unmodifiable, null-permitting
```

---

---

## 📖 Theory Block 4 — Date/Time API `[10 min]`

**Java 8 (but ignored by most — let's fix that)**

### Why `java.util.Date` Had to Die

| Problem | `java.util.Date` | `java.time` |
|---------|-----------------|-------------|
| Thread safety | ❌ Mutable | ✅ Immutable |
| Month indexing | `0 = January` 😱 | `1 = January` ✅ |
| Timezone handling | Confusing | `ZonedDateTime` / `ZoneId` |
| Formatting | `SimpleDateFormat` (not thread-safe) | `DateTimeFormatter` (immutable, thread-safe) |
| Arithmetic | Manual `Calendar` | `plus()`, `minus()`, `until()` |

---

### Key Classes

```java
LocalDate today      = LocalDate.now();            // 2024-03-15
LocalDate settlement = LocalDate.of(2024, 3, 17);  // 2024-03-17
LocalTime closeTime  = LocalTime.of(16, 30);       // 16:30

LocalDateTime stamp  = LocalDateTime.of(today, closeTime);
ZonedDateTime nyTime = ZonedDateTime.now(ZoneId.of("America/New_York"));

// Arithmetic
LocalDate maturity = today.plusMonths(6).plusDays(2);
long days = ChronoUnit.DAYS.between(today, settlement);  // → 2

// Formatting
String formatted = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

// Period vs Duration
Period period     = Period.between(today, settlement);    // date-based
Duration duration = Duration.between(openTime, closeTime); // time-based
```

**Source:** `DateTimeApiExamples.java`

---

---

## 📖 Theory Block 5 — CompletableFuture `[10 min]`

**Java 8**

### The Problem with `Future<T>`

```java
Future<String> future = executor.submit(() -> fetchPrice("AAPL"));
String price = future.get();  // BLOCKS the calling thread until done
// No way to chain actions, handle errors functionally, or combine futures
```

### `CompletableFuture` — Non-blocking Composition

```java
// Fetch price and counterparty in parallel, then combine
CompletableFuture<String> priceFuture = CompletableFuture.supplyAsync(
        () -> fetchPrice("AAPL"));

CompletableFuture<String> cpFuture = CompletableFuture.supplyAsync(
        () -> fetchCounterparty("CP001"));

// Combine both when they complete
CompletableFuture<String> combined = priceFuture.thenCombine(
        cpFuture,
        (price, cp) -> "Price: " + price + ", CP: " + cp
);

String result = combined.join();  // wait for combined result
```

**Source:** `CompletableFutureExamples.java`

---

### Key Operations

| Method | What it does |
|--------|-------------|
| `supplyAsync(supplier)` | Run a value-producing task asynchronously |
| `thenApply(fn)` | Transform the result (like `map`) |
| `thenCompose(fn)` | Chain another `CompletableFuture` (like `flatMap`) |
| `thenCombine(cf, fn)` | Combine two independent futures |
| `thenAccept(consumer)` | Consume result, no return value |
| `exceptionally(fn)` | Handle exceptions, provide fallback |
| `allOf(cf1, cf2, …)` | Wait for all to complete |
| `anyOf(cf1, cf2, …)` | Wait for the first to complete |

---

---

## 📖 Theory Block 6 — HTTP Client + Switch Expressions `[10 min]`

### New HttpClient (Java 11, JEP 321)

```java
HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.example.com/trades/T001"))
        .header("Accept", "application/json")
        .GET()
        .build();

// Synchronous
HttpResponse<String> response = client.send(request,
        HttpResponse.BodyHandlers.ofString());

// Asynchronous (returns CompletableFuture)
CompletableFuture<HttpResponse<String>> async = client.sendAsync(request,
        HttpResponse.BodyHandlers.ofString());
```

**Advantages over `HttpURLConnection`:** HTTP/2, WebSocket, async first-class, no external deps.

---

### Switch Expressions (Java 14, JEP 361)

**Before — switch statement with fall-through risk**
```java
int sla;
switch (status) {
    case DRAFT:    sla = 24; break;
    case PENDING:  sla = 4;  break;
    case EXECUTED: sla = 1;  break;
    default:       throw new IllegalArgumentException(status.toString());
}
return sla;
```

**After — switch expression, no fall-through, no break**
```java
return switch (status) {
    case DRAFT      -> 24;
    case PENDING    -> 4;
    case EXECUTED   -> 1;
    case SETTLED    -> 0;
    case REJECTED,
         CANCELLED  -> 48;   // multiple labels, no fall-through
};
// Compiler error if a new enum value is added without a case — no silent gaps!
```

**`yield` for multi-statement block arms:**
```java
case REJECTED -> {
    String base = "Trade was REJECTED";
    yield base + " – please review and resubmit";
}
```

**Source:** `SwitchExpressionsExample.java` — `getSlaHours_After`, `formatStatusMessage_After`

---

---

## 💻 Hands-On — The Evolution Refactor `[15 min]`

**Goal:** Apply Day 2 features to a legacy status-mapper class.

### Task A — Replace `new HashMap<>()` with `var` (2 min)
```java
// Before
Map<String, Integer> slaMap = new HashMap<String, Integer>();
for (TradeStatus s : TradeStatus.values()) {
    slaMap.put(s.name(), getSla(s));
}

// After: use var
```

### Task B — Replace manual status set with `Set.of` (2 min)
```java
// Before
Set<String> terminal = new HashSet<>();
terminal.add("SETTLED");
terminal.add("REJECTED");
terminal.add("CANCELLED");

// After: one line with Set.of
```

### Task C — Replace `trim()` with `strip()` and null-check with `isBlank()` (3 min)
```java
// Before
if (symbol != null && !symbol.trim().isEmpty()) {
    process(symbol.trim().toUpperCase());
}

// After: use strip() + isBlank()
```

### Task D — Convert the if-else SLA mapper to a switch expression (8 min)
```java
// Before
public int getSla(String status) {
    if ("DRAFT".equals(status))      return 24;
    else if ("PENDING".equals(status)) return 4;
    else if ("EXECUTED".equals(status)) return 1;
    else if ("SETTLED".equals(status))  return 0;
    else return 48;
}

// After: switch expression with arrow cases
```

<details>
<summary>💡 Reveal solutions</summary>

```java
// A
var slaMap = new HashMap<String, Integer>();

// B
Set<String> terminal = Set.of("SETTLED", "REJECTED", "CANCELLED");

// C
if (symbol != null && !symbol.strip().isBlank()) {
    process(symbol.strip().toUpperCase());
}
// Even better with Optional:
Optional.ofNullable(symbol)
        .filter(s -> !s.strip().isBlank())
        .ifPresent(s -> process(s.strip().toUpperCase()));

// D
return switch (status) {
    case "DRAFT"     -> 24;
    case "PENDING"   -> 4;
    case "EXECUTED"  -> 1;
    case "SETTLED"   -> 0;
    default          -> 48;
};
```
</details>

---

---

## 🔑 Day 2 Takeaways `[5 min]`

> **Card 1 — `var`:**  
> Use `var` when the type is obvious from the right-hand side. It reduces noise without losing type safety. Never use it in public API signatures (they don't allow it anyway).

> **Card 2 — Collections & String:**  
> `List.of()` / `Set.of()` / `Map.of()` create immutable collections in one line — use them by default.
> Prefer `strip()` over `trim()` and `isBlank()` over `isEmpty()` for Unicode correctness.

> **Card 3 — Switch Expressions:**  
> Arrow `->` syntax eliminates fall-through bugs. Switch can now be used as an expression that returns a value. The compiler flags missing enum cases — no more silent defaults.

---

### 📚 Pre-read for Day 3

> *"What is a Java record? How does it differ from a regular class?  
> Open `RecordsExample.java` and count how many lines the POJO takes vs the record."*

---

> **End of Day 2**  
> Source: `src/main/java/com/training/java817/module1/` + `module2/switchexpressions`  
> Tests: `src/test/java/com/training/java817/module1/` + `module2/`  
> Run: `mvn test`
