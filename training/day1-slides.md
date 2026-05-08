# Day 1 — The Functional Revolution
## *Java 8 Foundation*

**Duration:** 1.5 hours | **Source files:** `module1/lambdas` · `module1/streams` · `module1/optional`

---

---

## 🎯 Opening Hook — *The Chef Analogy* `[5 min]`

> *"Before Java 8, writing a sort function was like hiring a chef to come to your house just to slice one onion.  
> Now you hand them the knife and say: **slice**."*

### The Jarring Contrast

**Java 7 — 8 lines to sort a list**
```java
list.sort(new Comparator<String>() {
    @Override
    public int compare(String a, String b) {
        return a.compareTo(b);
    }
});
```

**Java 8 — 1 line**
```java
list.sort((a, b) -> a.compareTo(b));
// or even cleaner with a method reference:
list.sort(String::compareTo);
```

> **Question for the audience:** How many of you have written the 8-line version at least once?  
> *(pause for hands)*  
> Today we make that a thing of the past.

---

---

## 📖 Theory Block 1 — Lambdas & Functional Interfaces `[20 min]`

### What Changed?

| Java 7 | Java 8 |
|--------|--------|
| Pass behaviour via anonymous inner classes | Pass behaviour as a **lambda expression** |
| Verbose, hides intent | Concise, reads like the intent |
| Cannot be stored as a variable easily | First-class value — assign to a variable, pass as argument, return from method |

---

### What Is a Functional Interface?

> Any interface with **exactly one abstract method** — annotated `@FunctionalInterface` by convention.

```java
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);   // the ONE abstract method
}
```

The compiler matches the lambda `value -> value > 1_000_000` to `test(T t)` automatically.

---

### The 5 Pillars — Built-in Functional Interfaces

| Interface | Signature | Use case | Example |
|-----------|-----------|----------|---------|
| `Predicate<T>` | `T → boolean` | Filter / test | `t -> t.notional() > 1_000_000` |
| `Function<T,R>` | `T → R` | Map / transform | `id -> "TRD-" + id` |
| `Consumer<T>` | `T → void` | Side effects | `System.out::println` |
| `Supplier<T>` | `() → T` | Lazy value | `() -> "UNKNOWN_CP"` |
| `BiFunction<T,U,R>` | `(T,U) → R` | Two inputs | `(sym, ver) -> sym + "_v" + ver` |

**Source:** `LambdaExamples.java` — `isHighValue`, `formatTradeId`, `printTrade`, `getDefaultCounterparty`, `buildTradeKey`

---

### Method References — 4 Kinds

```java
// 1. Static method reference
Function<String, Integer> parse = Integer::parseInt;

// 2. Instance method reference (arbitrary instance)
Function<String, String> upper = String::toUpperCase;

// 3. Instance method reference (specific instance)
Consumer<String> print = System.out::println;

// 4. Constructor reference
Supplier<ArrayList<String>> listFactory = ArrayList::new;
```

---

### Function Composition

```java
Function<String, String> trim      = String::trim;
Function<String, String> upperCase = String::toUpperCase;

// andThen: trim THEN upperCase
Function<String, String> pipeline = trim.andThen(upperCase);
pipeline.apply("  aapl  ");   // → "AAPL"
```

```java
Predicate<String> isActive = s -> s.startsWith("ACTIVE");
Predicate<String> isLong   = s -> s.length() > 10;

// and / or / negate
Predicate<String> activeAndLong = isActive.and(isLong);
```

**Source:** `LambdaExamples.sanitizeAndFormat`, `LambdaExamples.activeAndLongSymbol`

---

### Effectively Final Variables

```java
// 'prefix' is captured by the lambda — must be effectively final
public List<String> filterByPrefix(List<String> items, String prefix) {
    return items.stream()
            .filter(item -> item.startsWith(prefix))   // captures 'prefix'
            .collect(Collectors.toList());
}
```

> **Rule:** A lambda can read local variables from the enclosing scope only if
> they are never reassigned after their first assignment.

---

## 💻 Hands-On 1 — Lambda Rewrite Challenge `[15 min]`

**File:** `src/main/java/com/training/java817/module1/lambdas/LambdaExamples.java`

> **Your challenge:** The three methods below use Java 7 style. Rewrite each as a lambda (or method reference).

### Task A — Sort
```java
// Before
list.sort(new Comparator<String>() {
    @Override public int compare(String a, String b) { return a.compareTo(b); }
});

// After → your answer here
```

### Task B — Filter (use streams)
```java
// Before
List<String> result = new ArrayList<>();
for (String s : statuses) {
    if (s.startsWith("ACTIVE")) result.add(s);
}

// After → your answer here
```

### Task C — Transform
```java
// Before
List<String> upper = new ArrayList<>();
for (String s : symbols) { upper.add(s.toUpperCase()); }

// After → your answer here (use a method reference!)
```

<details>
<summary>💡 Reveal solutions</summary>

```java
// A
list.sort(String::compareTo);

// B
statuses.stream().filter(s -> s.startsWith("ACTIVE")).collect(Collectors.toList());

// C
symbols.stream().map(String::toUpperCase).collect(Collectors.toList());
```
</details>

---

---

## 📖 Theory Block 2 — The Stream API `[25 min]`

### The Pipeline Mental Model

```
 source          intermediate ops (lazy)          terminal op (eager)
 ──────   ──────────────────────────────────   ─────────────────────
 List  →  filter() → map() → sorted() → ...  →  collect() / count() / reduce()

 Nothing executes until the terminal operation is reached!
```

---

### Intermediate vs Terminal Operations

| Category | Operations | Returns |
|----------|-----------|---------|
| **Intermediate** (lazy) | `filter`, `map`, `flatMap`, `distinct`, `sorted`, `limit`, `skip`, `peek` | `Stream<T>` |
| **Terminal** (eager) | `collect`, `count`, `reduce`, `forEach`, `anyMatch`, `allMatch`, `findFirst` | result / side effect |

> **Common mistake:** Calling a terminal operation twice on the same stream throws `IllegalStateException`.

---

### Before vs After — 3 Real Examples

**Example 1 — Sum EXECUTED trade notional**
```java
// Before (Java 7) — 5 lines, mutation
double total = 0;
for (Trade t : trades) {
    if ("EXECUTED".equals(t.status())) total += t.notional();
}

// After (Java 8) — 3 lines, no mutation
double total = trades.stream()
        .filter(t -> "EXECUTED".equals(t.status()))
        .mapToDouble(Trade::notional)
        .sum();
```

**Example 2 — Distinct sorted symbols**
```java
// Before — 7 lines including Collections.sort
// After
List<String> symbols = trades.stream()
        .map(Trade::symbol)
        .distinct()
        .sorted()
        .collect(Collectors.toList());
```

**Example 3 — Group by status**
```java
// Before — 6 lines with computeIfAbsent
// After — 1 line
Map<String, List<Trade>> grouped = trades.stream()
        .collect(Collectors.groupingBy(Trade::status));
```

**Source:** `StreamExamples.java` — all three before/after methods

---

### Key Collectors

| Collector | What it does | Example |
|-----------|-------------|---------|
| `Collectors.toList()` | Collect into a List | `stream().collect(toList())` |
| `Collectors.toUnmodifiableList()` | Immutable list (Java 10) | — |
| `Collectors.joining(", ")` | Concatenate strings | `"T001, T002, T003"` |
| `Collectors.groupingBy(fn)` | Group into Map | `groupingBy(Trade::status)` |
| `Collectors.toMap(k, v)` | Index by key | `toMap(Trade::id, t -> t)` |
| `Collectors.counting()` | Count per group | with `groupingBy` |

---

### flatMap — Flatten Nested Collections

```java
// Input: List<List<String>> portfolios
// Goal:  one flat sorted distinct list of all symbols

List<String> allSymbols = portfolios.stream()
        .flatMap(List::stream)     // flatten: List<List<>> → Stream<String>
        .distinct()
        .sorted()
        .collect(Collectors.toList());
```

---

### parallelStream — Proceed with Caution

```java
double sum = trades.parallelStream()
        .mapToDouble(Trade::notional)
        .sum();   // safe: reduction is associative
```

> ✅ Use `parallelStream()` when:
> - Large datasets (10,000+ elements)
> - Operation is CPU-bound and stateless
> - No shared mutable state
>
> ❌ Avoid when:
> - Small collections (overhead exceeds gain)
> - Operations have side effects or shared state
> - Order matters and you haven't verified correctness

---

## 💻 Hands-On 2 — Stream Kata `[15 min]`

**File:** `StreamExamples.java`

> **Given a list of `Trade(id, symbol, notional, status)` objects, write a single stream pipeline that:**
> 1. Keeps only trades with status `"ACTIVE"`
> 2. Sorts them by `notional` descending
> 3. Takes the top 3
> 4. Returns their IDs as a comma-separated `String`

```java
List<Trade> trades = List.of(
    new Trade("T001", "AAPL", 3_000_000, "ACTIVE"),
    new Trade("T002", "MSFT",   500_000, "ACTIVE"),
    new Trade("T003", "GOOG", 1_200_000, "PENDING"),
    new Trade("T004", "TSLA", 2_100_000, "ACTIVE"),
    new Trade("T005", "NVDA", 4_500_000, "ACTIVE"),
    new Trade("T006", "META",   800_000, "ACTIVE")
);

// Your pipeline here — expected: "T005, T001, T004"
String result = trades.stream()
        // ...
```

<details>
<summary>💡 Reveal solution</summary>

```java
String result = trades.stream()
        .filter(t -> "ACTIVE".equals(t.status()))
        .sorted(Comparator.comparingDouble(Trade::notional).reversed())
        .limit(3)
        .map(Trade::id)
        .collect(Collectors.joining(", "));
// → "T005, T001, T004"
```
</details>

---

---

## 📖 Theory Block 3 — Optional: Taming `null` `[10 min]`

### The Problem

> `NullPointerException` has been called *"the billion-dollar mistake"* by Tony Hoare, who invented the null reference.

```java
// This crashes with NPE if ANY link in the chain is null
String city = trade.counterparty().address().city();
```

### The Java 7 Solution — Defensive Null Checks

```java
String city = "UNKNOWN";
if (trade != null) {
    Counterparty cp = trade.counterparty();
    if (cp != null) {
        Address addr = cp.address();
        if (addr != null) {
            city = addr.city();
        }
    }
}
```

> 14 lines. What was the actual business logic? `trade.counterparty().address().city()`.

---

### The Java 8 Solution — `Optional<T>`

```java
String city = Optional.ofNullable(trade)
        .map(Trade::counterparty)
        .map(Counterparty::address)
        .map(Address::city)
        .orElse("UNKNOWN");
```

**Source:** `OptionalExamples.getTradeCity_After`

---

### Key Optional Methods

| Method | When to use |
|--------|------------|
| `Optional.of(value)` | Value is guaranteed non-null |
| `Optional.ofNullable(value)` | Value may be null |
| `Optional.empty()` | Explicitly absent |
| `.map(fn)` | Transform value if present |
| `.flatMap(fn)` | Transform when fn itself returns Optional |
| `.filter(pred)` | Keep value only if condition is true |
| `.orElse(default)` | Provide a fallback value |
| `.orElseGet(supplier)` | Provide a fallback lazily (preferred for expensive defaults) |
| `.orElseThrow(supplier)` | Throw a domain exception if absent |
| `.ifPresent(consumer)` | Execute side effect only if present |
| `.ifPresentOrElse(fn, runnable)` | Branch both ways (Java 9+) |
| `.or(supplier)` | Fallback to another Optional (Java 9+) |

---

### Best Practice Rules

| ✅ Do | ❌ Don't |
|-------|---------|
| Use as a **return type** when absence is valid | Use as a **field type** (serialisation issues) |
| Use `map`/`flatMap`/`filter` for transformations | Call `.get()` without `.isPresent()` |
| Use `orElseGet` for expensive defaults | Use as a **method parameter** |
| Use `orElseThrow` when absence is a contract violation | Return `Optional.of(null)` |

---

---

## 🔑 Day 1 Takeaways `[5 min]`

> **Card 1 — Lambdas:**  
> A lambda is a concise way to pass behaviour as a value. It implements a `@FunctionalInterface`.
> Use method references (`Type::method`) whenever the lambda just delegates to a single method.

> **Card 2 — Streams:**  
> A stream is a lazy pipeline: intermediate ops do nothing until a terminal op triggers execution.
> `filter → map → collect` replaces 90 % of imperative for-loops.

> **Card 3 — Optional:**  
> Return `Optional<T>` from methods where absence is valid. Use `map`/`orElse` chains
> instead of nested null checks. Never call `.get()` without `.isPresent()`.

---

### 📚 Pre-read for Day 2

> *"What is `var` in Java 10? When should you use it — and when should you avoid it?"*  
> Hint: open `LocalVarInferenceExamples.java` and read the header comment.

---

> **End of Day 1**  
> Source: `src/main/java/com/training/java817/module1/`  
> Tests: `src/test/java/com/training/java817/module1/`  
> Run: `mvn test -pl . -Dtest="Lambda*,Stream*,Optional*"`
