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
| 1 — DTO | `Employee.java` (80-line POJO) | `EmployeeRecord.java` | Records |
| 2 — SQL | String concatenation in `buildSearchQuery` | Text block + `.formatted()` | Text Blocks |
| 3 — Events | `EmployeeEventLegacy` enum hack | `sealed` interface + record subtypes | Sealed Classes |
| 4 — Dispatch | `if/instanceof` chain | Pattern matching + switch expression | Pattern Matching + Switch |

> Each task is independent — finish early? Move to the next. All verified by `WorkshopTest`.

---

---

## 📖 Theory Block 1 — Pattern Matching for `instanceof` `[20 min]`

**JEP 394, GA in Java 16**

### The Redundant Cast Problem

```java
// Java 7–15 — you check the type, then immediately cast to the same type
if (event instanceof EmployeeHiredEvent) {
    EmployeeHiredEvent c = (EmployeeHiredEvent) event;  // why cast when we just proved the type?
    log("New trade: " + c.employeeId());
}
```

> The cast is **redundant information**. The compiler already proved `event` is a `EmployeeHiredEvent`.
> Pattern matching eliminates this noise.

---

### The Pattern Matching Solution

```java
// Java 16+ — test AND bind in one expression
if (event instanceof EmployeeHiredEvent c) {
    log("New trade: " + c.employeeId());   // 'c' is in scope as EmployeeHiredEvent
}
```

**What the compiler guarantees:**
- `c` is in scope **only** in the true branch (where `event` is definitely a `EmployeeHiredEvent`)
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
    } else if (event instanceof EmployeeEvent) {
        EmployeeEvent te = (EmployeeEvent) event;      // cast 2
        return "TRADE | id=" + te.employeeId();
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
    } else if (event instanceof EmployeeEvent te) {
        return "TRADE | id=" + te.employeeId();
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
if (event instanceof EmployeeEvent te && te.amount() > 100_000) {
    return "HIGH-VALUE TRADE: " + te.employeeId() + " = " + te.amount();
} else if (event instanceof EmployeeEvent te) {
    return "Standard trade: " + te.employeeId();
}
```

```java
// Security and compliance monitoring
for (AuditEvent event : events) {
    if (event instanceof LoginEvent le && !le.success()) {
        System.out.println("SECURITY ALERT: Failed login from " + le.ipAddress());
    } else if (event instanceof EmployeeEvent te && te.amount() > 5_000_000) {
        System.out.println("COMPLIANCE ALERT: Large trade " + te.employeeId());
    } else if (event instanceof AlertEvent ae && "CRITICAL".equals(ae.severity())) {
        System.out.println("CRITICAL ALERT: " + ae.message());
    }
}
```

**Source:** `PatternMatchingExamples.describeHighSalaryEmployee`, `processAuditLog`

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
    case EmployeeHiredEvent c  -> "New trade: " + c.employeeId();
    case EmployeeTerminatedEvent r -> "Rejected: " + r.rejectionReason();
    case EmployeePromotedEvent e -> "Executed at: " + e.executedPrice();
    case EmployeeUpdatedEvent u  -> "Updated salary: " + u.newNotional();
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
public interface EmployeeEvent {
    String employeeId();
}

// Therefore, every switch/if-else MUST have a default — or silently miss new types
public String handle(EmployeeEvent event) {
    if (event instanceof EmployeeHiredEvent) { … }
    else if (event instanceof EmployeePromotedEvent) { … }
    else {
        // This silently ignores new event types added by other teams!
        return "Unknown";
    }
}
```

---

### The Sealed Solution — Closed-World Modelling

```java
// Only these 4 types can implement EmployeeEvent — enforced by the compiler
public sealed interface EmployeeEvent
        permits EmployeeHiredEvent, EmployeeUpdatedEvent, EmployeePromotedEvent, EmployeeTerminatedEvent {
    String employeeId();
}

// Each permitted type declares itself final, sealed, or non-sealed
public record EmployeeHiredEvent(String employeeId, String name, double salary)
        implements EmployeeEvent {}          // implicitly final (record)

public record EmployeeTerminatedEvent(String employeeId, String terminationReason)
        implements EmployeeEvent {}
```

**Source:** `SealedClassesExample.EmployeeEvent`, `EmployeeHiredEvent`, `EmployeeTerminatedEvent`, `EmployeePromotedEvent`, `EmployeeUpdatedEvent`

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

### Sealed Class Hierarchy — Employee State Machine

```java
public abstract sealed class EmployeeState
        permits EmployeeState.Applied, EmployeeState.Onboarding,
                EmployeeState.Active, EmployeeState.Resigned {

    public final class Applied    extends EmployeeState { … }
    public final class Onboarding extends EmployeeState { … }
    public final class Active     extends EmployeeState { … }
    public final class Resigned   extends EmployeeState { … }
}
```

> Each state carries **only the data relevant to that state** — not a single bloated class
> with optional fields that mean different things in different states.

**Source:** `SealedClassesExample.EmployeeState`

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

Result<EmployeeDetails> result = …;
if (result instanceof Result.Success<EmployeeDetails> s) {
    process(s.value());
} else if (result instanceof Result.Failure<EmployeeDetails> f) {
    log("Failed: " + f.reason());
}
```

---

### Design Guidelines — When to Use Sealed Types

| ✅ Ideal Use Case | ❌ Wrong Use Case |
|------------------|-----------------|
| Domain event hierarchies (`EmployeeHiredEvent`, `EmployeeTerminatedEvent`, …) | Types that genuinely need open extension |
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

**Before:** `module3/before/Employee.java` (~80 lines: constructor, 8 getters, `equals`, `hashCode`, `toString`)

**Goal:** Produce `module3/after/EmployeeRecord.java`

```java
// Step 1: Create the record declaration (one line)
public record EmployeeRecord(
        String transactionId, String employeeId, String name,
        double salary, String departmentId,
        LocalDateTime hireDate, String status, String venue) { }

// Step 2: Add a compact constructor for validation
public EmployeeRecord {
    Objects.requireNonNull(transactionId, "transactionId required");
    Objects.requireNonNull(employeeId, "employeeId required");
    // … remaining fields
}

// Step 3: Add any domain methods (isHighSalary, etc.)
public boolean isHighSalary() { return salary > 100_000; }
```

> Verify: `WorkshopTest.testTask1_RecordCreation` passes.

---

### Task 2 — String Concatenation → Text Block `[5 min]`

**Before:** `module3/before/TransactionService.buildSearchQuery`

```java
// Before — hard to read, easy to break
private String buildEmployeeQuery(String status, String name) {
    return "SELECT tx.transaction_id, tx.employee_id, tx.name, " +
           "tx.salary, tx.department_id, tx.hire_date, " +
           "tx.status, tx.venue " +
           "FROM employee_transactions tx " +
           "WHERE tx.status = '" + status + "' " +
           "AND tx.name = '" + name + "' " +
           "ORDER BY tx.hire_date DESC";
}
```

**Goal:** Rewrite using a text block + `.formatted()`

> Reference: `module3/after/ModernTransactionService.buildSearchQuery`  
> Verify: `WorkshopTest.testTask2_TextBlockQuery` passes.

---

### Task 3 — Enum Hack → Sealed Hierarchy `[10 min]`

**Before:** `module3/before/TransactionService.EmployeeEventLegacy` — a plain enum that tries to carry different data for each event type (impossible cleanly with enums)

```java
// The enum hack — one class tries to represent all event shapes
public enum EmployeeEventLegacy {
    CREATED, UPDATED, EXECUTED, REJECTED;
    // No per-event data possible — callers use convention / casting
}
```

**Goal:** Replace with sealed interface + record subtypes

```java
// After: each event carries exactly the data it needs
public sealed interface EmployeeEvent
        permits EmployeeHiredEvent, EmployeeUpdatedEvent,
                EmployeePromotedEvent, EmployeeTerminatedEvent {
    String employeeId();
}
```

> Reference files: `module3/after/EmployeeEvent.java`, `EmployeeHiredEvent.java`,
> `EmployeeUpdatedEvent.java`, `EmployeePromotedEvent.java`, `EmployeeTerminatedEvent.java`  
> Verify: `WorkshopTest.testTask3_SealedEvents` passes.

---

### Task 4 — if/instanceof Chain → Pattern Matching + Switch `[15 min]`

**Before:** `TransactionService.processEventLegacy` — long if/instanceof chain with redundant casts

```java
// Before — 3 casts, silently ignores new event types
public String processEventLegacy(Object event) {
    if (event instanceof EmployeeHiredEvent_Before) {
        EmployeeHiredEvent_Before c = (EmployeeHiredEvent_Before) event;   // cast!
        return "Created: " + c.employeeId();
    } else if (event instanceof EmployeePromotedEvent_Before) {
        EmployeePromotedEvent_Before e = (EmployeePromotedEvent_Before) event; // cast!
        return "Executed: " + e.employeeId();
    } else if (event instanceof EmployeeTerminatedEvent_Before) {
        EmployeeTerminatedEvent_Before r = (EmployeeTerminatedEvent_Before) event; // cast!
        return "Rejected: " + r.reason();
    } else {
        return "Unknown event"; // silently misses new types
    }
}
```

**Goal:** Refactor using pattern matching + switch expression

```java
// After: ModernTransactionService.processEvent
public String processEvent(EmployeeEvent event) {
    return switch (event) {
        case EmployeeHiredEvent c      -> "Hired: "      + c.employeeId() + " | " + c.name();
        case EmployeeUpdatedEvent u    -> "Updated: "    + u.employeeId() + " salary=" + u.newSalary();
        case EmployeePromotedEvent e   -> "Promoted: "   + e.employeeId() + " at " + e.promotionDate();
        case EmployeeTerminatedEvent r -> "Terminated: " + r.employeeId() + " – " + r.terminationReason();
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
> - Record subtypes → perfect `EmployeeEvent` implementations
> - Sealed interface → the switch knows every case
> - Switch expression → returns a value, no fall-through
> - Pattern matching → no casts needed

---

---

## 🔑 Day 4 Takeaways `[5 min]`

> **Card 1 — Pattern Matching for `instanceof`:**  
> Combine the type test and variable binding in one step: `event instanceof EmployeeHiredEvent c`.
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
