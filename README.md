# Modernising with Java 17 – Training Project

A hands-on, production-style training module for Java developers upgrading from Java 8 to Java 17.
Every feature is illustrated with **before (legacy)** and **after (modern)** code examples, detailed
theoretical analysis, and a full JUnit 5 test suite.

---

## Prerequisites

| Tool  | Minimum version |
|-------|-----------------|
| JDK   | 17              |
| Maven | 3.6+            |

---

## Build & Test

```bash
# compile and run all 123 tests
mvn test

# compile only
mvn compile
```

---

## Project Structure

```
src/
├── main/java/com/training/java817/
│   ├── module1/          ← Java 8 Foundation
│   │   ├── lambdas/      LambdaExamples.java
│   │   ├── streams/      StreamExamples.java
│   │   └── optional/     OptionalExamples.java
│   ├── module2/          ← Java 17 Deep Dive
│   │   ├── records/      RecordsExample.java
│   │   ├── textblocks/   TextBlockExamples.java
│   │   ├── patternmatching/ PatternMatchingExamples.java
│   │   ├── sealedclasses/   SealedClassesExample.java
│   │   ├── switchexpressions/ SwitchExpressionsExample.java
│   │   └── helpfulnpe/   HelpfulNpeExamples.java
│   └── module3/          ← Hands-On Workshop
│       ├── before/       Legacy POJO + TransactionService
│       └── after/        Modern Record + ModernTransactionService
└── test/java/…           ← JUnit 5 test suite (mirrors main/)
```

---

## Module 1 – The Foundation (Java 8 Highlight Reel)

### Lambdas & Functional Interfaces (`module1/lambdas`)

**Problem solved:** Eliminates verbose anonymous inner classes and makes passing behaviour as a value natural.

| | Before (Java 7) | After (Java 8) |
|---|---|---|
| Sort | `new Comparator<String>() { @Override … }` | `(a, b) -> a.compareTo(b)` |
| Filter | Manual `for` loop + `if` | `list.stream().filter(s -> s.startsWith("ACTIVE"))` |
| Transform | Loop + temp list | `.map(String::toUpperCase)` (method reference) |

Key built-in functional interfaces:

| Interface | Signature | Use case |
|-----------|-----------|----------|
| `Predicate<T>` | `T → boolean` | filtering / testing |
| `Function<T,R>` | `T → R` | mapping / transforming |
| `Consumer<T>` | `T → void` | side effects (print, save) |
| `Supplier<T>` | `() → T` | lazy value production |
| `BiFunction<T,U,R>` | `(T,U) → R` | two inputs, one output |

---

### Stream API (`module1/streams`)

**Problem solved:** Replaces imperative `for` loops with readable, composable, and potentially parallel data pipelines.

```
source ──► [filter] ──► [map] ──► [reduce/collect]   (lazy until terminal op)
```

Key operations demonstrated: `filter`, `map`, `flatMap`, `distinct`, `sorted`, `limit`, `peek`,
`reduce`, `count`, `anyMatch`/`allMatch`, `Collectors.groupingBy`, `Collectors.joining`, `Collectors.toMap`.

---

### Optional (`module1/optional`)

**Problem solved:** Makes nullability explicit in the return type, eliminating null-check boilerplate and `NullPointerException`.

```java
// Before – nested null guards
if (trade != null && trade.counterparty() != null && …) { … }

// After – fluent chain
Optional.ofNullable(trade)
        .map(Trade::counterparty)
        .map(Counterparty::address)
        .map(Address::city)
        .orElse("UNKNOWN");
```

---

## Module 2 – The Core (Java 17 Deep Dive)

### 1. Records – JEP 395 (`module2/records`)

**What:** A special class that acts as an immutable, transparent data carrier.  
**Why:** Replaces 80-line DTOs/POJOs with a single declaration; auto-generates constructor, accessors (`tradeId()` not `getTradeId()`), `equals`, `hashCode`, `toString`.

```java
// Before – ~80 lines
public class TradeDetailsPojo { … getters … equals … hashCode … toString … }

// After – 1 declaration + compact constructor for validation
public record TradeDetailsRecord(
        String tradeId, String symbol, double notional,
        String counterpartyId, LocalDate settlementDate, String status) {
    public TradeDetailsRecord { Objects.requireNonNull(tradeId, "tradeId required"); … }
}
```

---

### 2. Text Blocks – JEP 378 (`module2/textblocks`)

**What:** Multi-line string literals delimited by `"""`.  
**Why:** Eliminates `\n`, `+`, and `\"` noise from embedded SQL, JSON, HTML, and XML.

```java
// Before
String sql = "SELECT t.trade_id " +
             "FROM   trades t " +
             "WHERE  t.status = '" + status + "'";

// After
String sql = """
        SELECT t.trade_id
        FROM   trades t
        WHERE  t.status = '%s'
        """.formatted(status);
```

---

### 3. Pattern Matching for instanceof – JEP 394 (`module2/patternmatching`)

**What:** Combines type test and binding in one expression.  
**Why:** Removes redundant casts and makes heterogeneous-type handling significantly cleaner.

```java
// Before
if (event instanceof TradeCreated) {
    TradeCreated c = (TradeCreated) event;   // redundant cast
    …
}

// After
if (event instanceof TradeCreated c) {       // test + bind in one step
    …
}

// With guard (&&)
if (event instanceof TradeEvent te && te.amount() > 1_000_000) { … }
```

---

### 4. Sealed Classes – JEP 409 (`module2/sealedclasses`)

**What:** Restricts which classes may extend or implement a class/interface.  
**Why:** Enables closed-world domain modelling; the compiler knows every possible subtype.

```java
public sealed interface TradeEvent
        permits TradeCreated, TradeUpdated, TradeExecuted, TradeRejected {}

public record TradeCreated(String tradeId, String symbol, double notional) implements TradeEvent {}
public record TradeRejected(String tradeId, String rejectionReason)        implements TradeEvent {}
// … etc.
```

Use `final` (no further subclassing), `sealed` (further restricted), or `non-sealed` (open again) on permitted subtypes.

---

### 5. Switch Expressions – JEP 361 (`module2/switchexpressions`)

**What:** Switch can now be used as an expression and return a value; arrow `->` prevents fall-through.  
**Why:** Eliminates missing-`break` bugs; switch result can flow directly into assignments or return statements.

```java
// Before – 10-line switch statement with break
int days;
switch (assetClass) { case "EQUITY": days = 2; break; … }
return days;

// After – one expression
return switch (assetClass) {
    case "EQUITY"       -> 2;
    case "FIXED_INCOME" -> 1;
    default             -> 3;
};
```

Use `yield` when a block arm needs multiple statements before returning a value.

---

### 6. Helpful NullPointerExceptions – JEP 358 (`module2/helpfulnpe`)

**What:** JVM enhancement that computes *which* variable was null.  
**Why:** Pinpoints the exact failure in chained method calls – no code changes required.

```
// Java < 14: just "NullPointerException"

// Java 14+:
Cannot invoke "Address.getCity()" because the return value of
"Customer.getAddress()" is null
```

Enabled by default from Java 15. JVM flag: `-XX:+ShowCodeDetailsInExceptionMessages`.

---

## Module 3 – Hands-On Workshop

Four refactoring tasks on a realistic financial-transaction service.

| Task | File (before) | File (after) | Feature |
|------|--------------|-------------|---------|
| 1 – DTO Conversion | `before/TradeTransaction.java` (POJO) | `after/TradeTransactionRecord.java` | Records |
| 2 – SQL Cleanup | `before/TransactionService.buildSearchQuery` | `after/ModernTransactionService.buildSearchQuery` | Text Blocks |
| 3 – Domain Modelling | `before/TransactionService.TradeEventLegacy` | `after/TradeEvent` (sealed) + event records | Sealed Classes |
| 4 – Control Flow | `before/TransactionService.processEventLegacy` | `after/ModernTransactionService.processEvent/routeEvent` | Pattern Matching + Switch Expressions |

Run `WorkshopTest` to verify every task produces identical outputs before and after.

---

## Key Takeaways

| Feature | JEP | GA | One-liner |
|---------|-----|----|-----------|
| Lambdas | – | Java 8 | Pass behaviour as a value |
| Stream API | – | Java 8 | Declarative data pipelines |
| Optional | – | Java 8 | Explicit nullability in APIs |
| Switch Expressions | 361 | Java 14 | Arrow cases, no fall-through, returns a value |
| Helpful NPE | 358 | Java 14 (default Java 15) | Precise null description at runtime |
| Records | 395 | Java 16 | Immutable data carriers, zero boilerplate |
| Pattern Matching | 394 | Java 16 | Test + bind in one `instanceof` expression |
| Sealed Classes | 409 | Java 17 | Closed-world type hierarchies |
| Text Blocks | 378 | Java 15 | Readable multi-line strings |

