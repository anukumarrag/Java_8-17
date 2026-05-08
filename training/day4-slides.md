# Day 4 — The Modern Java, Part 2 + Full Workshop
## *Pattern Matching · Sealed Classes · Module 3 Refactoring Workshop*

**Duration:** 1.5 hours  
**Source files:** `module2/patternmatching` · `module2/sealedclasses` · `module3/before` · `module3/after`

---

---

## 🎯 Opening Hook — *The Refactoring Challenge* `[5 min]`

> *"Today we take everything we've learned this week and apply it to a real-world refactoring job.  
> By the end of this session you'll have transformed a legacy Java 7-style financial transaction service  
> into clean, idiomatic, production-grade Java 17.  
> Let's see how all the pieces snap together."*

### What You'll Accomplish Today

| Task | From | To | Feature |
|------|------|----|---------|
| 1 — DTO | `TradeTransaction.java` (80-line POJO) | `TradeTransactionRecord.java` | Records |
| 2 — SQL | String concatenation in `buildSearchQuery` | Text block + `.formatted()` | Text Blocks |
| 3 — Events | `TradeEventLegacy` enum hack | `sealed` interface + record subtypes | Sealed Classes |
| 4 — Dispatch | `if/instanceof` chain | Pattern matching + switch expression | Pattern Matching + Switch |

> Each task is independent — finish early? Move to the next. All verified by `WorkshopTest`.

---

---

## 📖 Theory Block 1 — Pattern Matching for `instanceof` `[20 min]`

**JEP 394, GA in Java 16**

### The Redundant Cast Problem

```java
// Java 7–15 — you check the type, then immediately cast to the same type
if (event instanceof TradeCreated) {
    TradeCreated c = (TradeCreated) event;  // why cast when we just proved the type?
    log("New trade: " + c.tradeId());
}
```

> The cast is **redundant information**. The compiler already proved `event` is a `TradeCreated`.
> Pattern matching eliminates this noise.

---

### The Pattern Matching Solution

```java
// Java 16+ — test AND bind in one expression
if (event instanceof TradeCreated c) {
    log("New trade: " + c.tradeId());   // 'c' is in scope as TradeCreated
}
```

**What the compiler guarantees:**
- `c` is in scope **only** in the true branch (where `event` is definitely a `TradeCreated`)
- No `ClassCastException` possible
- No redundant variable declaration

---

### Before vs After — Full Event Handler

```java
// BEFORE — 3 redundant casts
public String describeEvent_Before(Object event) {
    if (event instanceof LoginEvent) {
        LoginEvent le = (LoginEvent) event;      // cast 1
        return "LOGIN | user=" + le.userId();
    } else if (event instanceof TradeEvent) {
        TradeEvent te = (TradeEvent) event;      // cast 2
        return "TRADE | id=" + te.tradeId();
    } else if (event instanceof AlertEvent) {
        AlertEvent ae = (AlertEvent) event;      // cast 3
        return "ALERT | " + ae.severity();
    } else {
        return "UNKNOWN EVENT";
    }
}
```

```java
// AFTER — pattern matching, zero casts
public String describeEvent_After(Object event) {
    if (event instanceof LoginEvent le) {
        return "LOGIN | user=" + le.userId();
    } else if (event instanceof TradeEvent te) {
        return "TRADE | id=" + te.tradeId();
    } else if (event instanceof AlertEvent ae) {
        return "ALERT | " + ae.severity();
    } else {
        return "UNKNOWN EVENT";
    }
}
```

**Source:** `PatternMatchingExamples.describeEvent_Before` / `describeEvent_After`

---

### Guard Conditions with `&&`

The binding variable is in scope on the **right** side of `&&`:

```java
// High-value trade guard
if (event instanceof TradeEvent te && te.amount() > 1_000_000) {
    return "HIGH-VALUE TRADE: " + te.tradeId() + " = " + te.amount();
} else if (event instanceof TradeEvent te) {
    return "Standard trade: " + te.tradeId();
}
```

```java
// Security and compliance monitoring
for (AuditEvent event : events) {
    if (event instanceof LoginEvent le && !le.success()) {
        System.out.println("SECURITY ALERT: Failed login from " + le.ipAddress());
    } else if (event instanceof TradeEvent te && te.amount() > 5_000_000) {
        System.out.println("COMPLIANCE ALERT: Large trade " + te.tradeId());
    } else if (event instanceof AlertEvent ae && "CRITICAL".equals(ae.severity())) {
        System.out.println("CRITICAL ALERT: " + ae.message());
    }
}
```

**Source:** `PatternMatchingExamples.describeHighValueTrade`, `processAuditLog`

---

### Scope Rule — Negation Pattern

```java
// The binding variable is NOT in scope when instanceof is false
if (!(event instanceof LoginEvent le)) {
    return;  // 'le' is NOT in scope here
}
// 'le' IS in scope here — compiler knows we passed the negation guard
doSomethingWith(le);
```

---

### Preview of Where This Leads — Day 5

```java
// Java 21: pattern matching IN switch (JEP 441)
String result = switch (event) {
    case TradeCreated c  -> "New trade: " + c.tradeId();
    case TradeRejected r -> "Rejected: " + r.rejectionReason();
    case TradeExecuted e -> "Executed at: " + e.executedPrice();
    case TradeUpdated u  -> "Updated notional: " + u.newNotional();
};
// Exhaustive — no default needed with a sealed type!
```

---

---

## 📖 Theory Block 2 — Sealed Classes `[20 min]`

**JEP 409, GA in Java 17**

### The Open-World Problem

```java
// Anyone can implement this — including code you don't control
public interface TradeEvent {
    String tradeId();
}

// Therefore, every switch/if-else MUST have a default — or silently miss new types
public String handle(TradeEvent event) {
    if (event instanceof TradeCreated) { … }
    else if (event instanceof TradeExecuted) { … }
    else {
        // This silently ignores new event types added by other teams!
        return "Unknown";
    }
}
```

---

### The Sealed Solution — Closed-World Modelling

```java
// Only these 4 types can implement TradeEvent — enforced by the compiler
public sealed interface TradeEvent
        permits TradeCreated, TradeUpdated, TradeExecuted, TradeRejected {
    String tradeId();
}

// Each permitted type declares itself final, sealed, or non-sealed
public record TradeCreated(String tradeId, String symbol, double notional)
        implements TradeEvent {}          // implicitly final (record)

public record TradeRejected(String tradeId, String rejectionReason)
        implements TradeEvent {}
```

**Source:** `SealedClassesExample.TradeEvent`, `TradeCreated`, `TradeRejected`, `TradeExecuted`, `TradeUpdated`

---

### Permitted Subtype Options

| Modifier | Meaning |
|----------|---------|
| `final` (or record) | Cannot be subclassed further — the hierarchy ends here |
| `sealed` | Can be subclassed, but only by its own `permits` list |
| `non-sealed` | Opens the hierarchy again — anyone can extend this |

```java
public sealed interface Shape permits Shape.Circle, Shape.Polygon {}

// final — leaf node
public final class Circle implements Shape { … }

// sealed — another level of restriction
public sealed class Polygon implements Shape permits Triangle, Quadrilateral { … }

// non-sealed — opens the hierarchy for Polygon subtypes
public non-sealed class IrregularPolygon extends Polygon { … }
```

---

### Sealed Class Hierarchy — Trade State Machine

```java
public abstract sealed class TradeState
        permits TradeState.Draft, TradeState.Pending,
                TradeState.Settled, TradeState.Cancelled {

    public final class Draft    extends TradeState { … }
    public final class Pending  extends TradeState { … }
    public final class Settled  extends TradeState { … }
    public final class Cancelled extends TradeState { … }
}
```

> Each state carries **only the data relevant to that state** — not a single bloated class
> with optional fields that mean different things in different states.

**Source:** `SealedClassesExample.TradeState`

---

### Sealed + Record = Algebraic Data Type

> **Records** give you: free `equals`, `hashCode`, `toString`, immutability, concise syntax  
> **Sealed** gives you: closed hierarchy, compiler-verified exhaustiveness

Combined, they model domain events, states, and result types as clearly as Scala's `case class` / `sealed trait`.

```java
// The full pattern: sealed interface + record subtypes
public sealed interface Result<T> permits Result.Success, Result.Failure {
    record Success<T>(T value)          implements Result<T> {}
    record Failure<T>(String reason)    implements Result<T> {}
}

Result<TradeDetails> result = …;
if (result instanceof Result.Success<TradeDetails> s) {
    process(s.value());
} else if (result instanceof Result.Failure<TradeDetails> f) {
    log("Failed: " + f.reason());
}
```

---

### Design Guidelines — When to Use Sealed Types

| ✅ Ideal Use Case | ❌ Wrong Use Case |
|------------------|-----------------|
| Domain event hierarchies (`TradeCreated`, `TradeRejected`, …) | Types that genuinely need open extension |
| State machines with known states | Plugin/extension points |
| Result / Either types (`Success` / `Failure`) | Types in a library intended for external extension |
| Command hierarchies in CQRS | General-purpose base classes |

---

---

## 💻 Full Workshop — Module 3 `[40 min]`

**Files:** `src/main/java/com/training/java817/module3/`  
**Verify:** `mvn test -Dtest="WorkshopTest"`

---

### Task 1 — DTO → Record `[10 min]`

**Before:** `module3/before/TradeTransaction.java` (~80 lines: constructor, 8 getters, `equals`, `hashCode`, `toString`)

**Goal:** Produce `module3/after/TradeTransactionRecord.java`

```java
// Step 1: Create the record declaration (one line)
public record TradeTransactionRecord(
        String transactionId, String tradeId, String symbol,
        double notional, String counterpartyId,
        LocalDateTime executionTime, String status, String venue) { }

// Step 2: Add a compact constructor for validation
public TradeTransactionRecord {
    Objects.requireNonNull(transactionId, "transactionId required");
    Objects.requireNonNull(tradeId, "tradeId required");
    // … remaining fields
}

// Step 3: Add any domain methods (isHighValue, etc.)
public boolean isHighValue() { return notional > 1_000_000; }
```

> Verify: `WorkshopTest.testTask1_RecordCreation` passes.

---

### Task 2 — String Concatenation → Text Block `[5 min]`

**Before:** `module3/before/TransactionService.buildSearchQuery`

```java
// Before — hard to read, easy to break
private String buildSearchQuery(String status, String symbol) {
    return "SELECT tx.transaction_id, tx.trade_id, tx.symbol, " +
           "tx.notional, tx.counterparty_id, tx.execution_time, " +
           "tx.status, tx.venue " +
           "FROM trade_transactions tx " +
           "WHERE tx.status = '" + status + "' " +
           "AND tx.symbol = '" + symbol + "' " +
           "ORDER BY tx.execution_time DESC";
}
```

**Goal:** Rewrite using a text block + `.formatted()`

> Reference: `module3/after/ModernTransactionService.buildSearchQuery`  
> Verify: `WorkshopTest.testTask2_TextBlockQuery` passes.

---

### Task 3 — Enum Hack → Sealed Hierarchy `[10 min]`

**Before:** `module3/before/TransactionService.TradeEventLegacy` — a plain enum that tries to carry different data for each event type (impossible cleanly with enums)

```java
// The enum hack — one class tries to represent all event shapes
public enum TradeEventLegacy {
    CREATED, UPDATED, EXECUTED, REJECTED;
    // No per-event data possible — callers use convention / casting
}
```

**Goal:** Replace with sealed interface + record subtypes

```java
// After: each event carries exactly the data it needs
public sealed interface TradeEvent
        permits TradeCreatedEvent, TradeUpdatedEvent,
                TradeExecutedEvent, TradeRejectedEvent {
    String tradeId();
}
```

> Reference files: `module3/after/TradeEvent.java`, `TradeCreatedEvent.java`,
> `TradeUpdatedEvent.java`, `TradeExecutedEvent.java`, `TradeRejectedEvent.java`  
> Verify: `WorkshopTest.testTask3_SealedEvents` passes.

---

### Task 4 — if/instanceof Chain → Pattern Matching + Switch `[15 min]`

**Before:** `TransactionService.processEventLegacy` — long if/instanceof chain with redundant casts

```java
// Before — 3 casts, silently ignores new event types
public String processEventLegacy(Object event) {
    if (event instanceof TradeCreated_Before) {
        TradeCreated_Before c = (TradeCreated_Before) event;   // cast!
        return "Created: " + c.tradeId();
    } else if (event instanceof TradeExecuted_Before) {
        TradeExecuted_Before e = (TradeExecuted_Before) event; // cast!
        return "Executed: " + e.tradeId();
    } else if (event instanceof TradeRejected_Before) {
        TradeRejected_Before r = (TradeRejected_Before) event; // cast!
        return "Rejected: " + r.reason();
    } else {
        return "Unknown event"; // silently misses new types
    }
}
```

**Goal:** Refactor using pattern matching + switch expression

```java
// After: ModernTransactionService.processEvent
public String processEvent(TradeEvent event) {
    return switch (event) {
        case TradeCreatedEvent  c -> "Created: "  + c.tradeId() + " | " + c.symbol();
        case TradeUpdatedEvent  u -> "Updated: "  + u.tradeId() + " notional=" + u.newNotional();
        case TradeExecutedEvent e -> "Executed: " + e.tradeId() + " at " + e.executedPrice();
        case TradeRejectedEvent r -> "Rejected: " + r.tradeId() + " – " + r.rejectionReason();
    };
    // Exhaustive — no default! New subtypes cause a compile error.
}
```

> Reference: `module3/after/ModernTransactionService.processEvent`, `routeEvent`  
> Verify: `WorkshopTest.testTask4_PatternMatchingDispatch` passes.

---

### Workshop Debrief

> Once all four tasks pass, step back and look at the diff:
> - **Before:** 1 POJO + 1 service class with 4 styles of boilerplate
> - **After:** 1 record + 1 sealed hierarchy + 1 modern service — fewer lines, more expressive

> **The features don't just reduce code — they compose:**
> - Record subtypes → perfect `TradeEvent` implementations
> - Sealed interface → the switch knows every case
> - Switch expression → returns a value, no fall-through
> - Pattern matching → no casts needed

---

---

## 🔑 Day 4 Takeaways `[5 min]`

> **Card 1 — Pattern Matching for `instanceof`:**  
> Combine the type test and variable binding in one step: `event instanceof TradeCreated c`.
> The binding variable `c` is only in scope where the test is provably true. No more redundant casts.

> **Card 2 — Sealed Classes:**  
> Use `sealed` to restrict which classes may implement/extend a type.
> The compiler knows every possible subtype → exhaustive switch expressions → no silent gaps when new types are added.

> **Card 3 — Features Compose:**  
> Records + Sealed + Pattern Matching + Switch Expressions form a coherent system.
> Algebraic domain modelling in Java is now first-class — no Lombok, no hacks, no enum overloading.

---

### 📚 Pre-read for Day 5 (Bonus)

> *"What are 'virtual threads'? How do they differ from platform threads?  
> Open `VirtualThreadsExamples.java` and read the header comment.  
> Bonus: how many virtual threads could you run on a modern server?"*

---

> **End of Day 4**  
> Source: `src/main/java/com/training/java817/module2/` + `module3/`  
> Tests: `src/test/java/com/training/java817/`  
> Run: `mvn test`
