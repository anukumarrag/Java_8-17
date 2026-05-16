# ⚡ Java 8 → 21 Feature Cheat Sheet
*One-page quick reference — print and keep on your desk*

---

## Java 8 Foundation

| Feature | JEP | GA | What it does | Source |
|---------|-----|----|-------------|--------|
| **Lambda Expressions** | — | Java 8 | Pass behaviour as a value: `(a, b) -> a.compareTo(b)` | `module1/lambdas` |
| **Functional Interfaces** | — | Java 8 | `Predicate<T>`, `Function<T,R>`, `Consumer<T>`, `Supplier<T>`, `BiFunction<T,U,R>` | `module1/lambdas` |
| **Method References** | — | Java 8 | 4 kinds: `String::toUpperCase`, `obj::method`, `Type::new`, `Class::staticMethod` | `module1/lambdas` |
| **Stream API** | — | Java 8 | Lazy declarative pipeline: `source → filter → map → collect` | `module1/streams` |
| **Optional\<T\>** | — | Java 8 | Explicit nullability; replaces null-check boilerplate with `.map().orElse()` chains | `module1/optional` |
| **Date/Time API** | — | Java 8 | `LocalDate`, `LocalTime`, `ZonedDateTime`, `Duration`, `Period` — immutable, thread-safe | `module1/datetime` |
| **CompletableFuture** | — | Java 8 | Non-blocking async: `supplyAsync`, `thenApply`, `thenCombine`, `exceptionally`, `allOf` | `module1/concurrent` |
| **Default/Static Interface Methods** | — | Java 8 | Add default implementations to interfaces without breaking implementors | `module1/interfaces` |

---

## Java 9–14 Essentials

| Feature | JEP | GA | What it does | Source |
|---------|-----|----|-------------|--------|
| **Collection Factories** | — | Java 9 | `List.of()`, `Set.of()`, `Map.of()` — immutable, null-hostile, one-line | `module1/collections` |
| **`var` (Type Inference)** | 286 | Java 10 | Local variable only: `var map = new HashMap<String, List<Employee>>()` | `module1/var` |
| **`Collectors.toUnmodifiableList()`** | — | Java 10 | Stream to immutable list | `module1/collections` |
| **String API (`isBlank`, `strip`, `lines`, `repeat`)** | — | Java 11 | Unicode-aware blank check, whitespace strip, line stream, string repetition | `module1/string` |
| **`HttpClient`** | 321 | Java 11 | Built-in HTTP/1.1+2 client — sync and async, replaces `HttpURLConnection` | `module1/httpclient` |
| **`Stream.toList()`** | — | Java 16 | `stream.toList()` — unmodifiable, shorter than `collect(Collectors.toList())` | `module1/streams2` |
| **Switch Expressions** | 361 | Java 14 | Arrow `->` cases, no fall-through, returns a value; `yield` for block arms | `module2/switchexpressions` |
| **Helpful NullPointerException** | 358 | Java 14 (default Java 15) | JVM names the null variable: *"Cannot invoke 'Address.city()' because … is null"* | `module2/helpfulnpe` |

---

## Java 15–17 Core

| Feature | JEP | GA | What it does | Source |
|---------|-----|----|-------------|--------|
| **Text Blocks** | 378 | Java 15 | `"""…"""` — multi-line strings, strips incidental indent, `.formatted()` | `module2/textblocks` |
| **Records** | 395 | Java 16 | Immutable data carrier: compiler generates constructor, accessors, `equals`, `hashCode`, `toString` | `module2/records` |
| **Pattern Matching for `instanceof`** | 394 | Java 16 | Test + bind in one step: `if (e instanceof EmployeeHiredEvent c)` — no redundant cast | `module2/patternmatching` |
| **Sealed Classes** | 409 | Java 17 | `sealed interface EmployeeEvent permits EmployeeHiredEvent, EmployeeTerminatedEvent` — closed-world hierarchy | `module2/sealedclasses` |

---

## Java 18–21 — The Future Is Now

| Feature | JEP | GA | What it does | Source |
|---------|-----|----|-------------|--------|
| **Virtual Threads** | 444 | Java 21 | Lightweight JVM-managed threads; millions on a small pool; drop-in via `Executors.newVirtualThreadPerTaskExecutor()` | `bonus/virtualthreads` |
| **Sequenced Collections** | 431 | Java 21 | `getFirst()`, `getLast()`, `reversed()` — uniform API for ordered `List`, `Deque`, `LinkedHashSet`, `LinkedHashMap` | `bonus/sequencedcollections` |
| **Record Patterns** | 440 | Java 21 | Deconstruct a record in `instanceof`: `if (obj instanceof Employee(String id, _, Money(double amt, _)))` | `bonus/recordpatterns` |
| **Pattern Matching in Switch** | 441 | Java 21 | `switch(event) { case EmployeeHiredEvent c -> … }` — type patterns as case labels, `when` guards | `bonus/patternswitch` |
| **Structured Concurrency** | 453 | Java 21 (preview) | `StructuredTaskScope` — parent-bound task lifetime, `ShutdownOnFailure` / `ShutdownOnSuccess` | `bonus/structuredconcurrency` |
| **Unnamed Variables `_`** | 456 | Java 22 | `catch (IOException _)` — explicitly discard unused variables in catch, switch, lambda | `bonus/unnamedvariables` |

---

## Quick-Access: Key API Snippets

### Lambdas & Streams
```java
// Filter → sort → limit → join
String top3 = employees.stream()
    .filter(t -> "ACTIVE".equals(e.status()))
    .sorted(Comparator.comparingDouble(Employee::salary).reversed())
    .limit(3)
    .map(Employee::id)
    .collect(Collectors.joining(", "));

// Group by status
Map<String, List<Employee>> byStatus = employees.stream()
    .collect(Collectors.groupingBy(Employee::department));
```

### Optional
```java
String city = Optional.ofNullable(employee)
    .map(Employee::department)
    .map(Department::address)
    .map(Address::city)
    .orElse("UNKNOWN");
```

### Record with Validation
```java
public record EmployeeRecord(String employeeId, String name, double salary) {
    public EmployeeRecord {
        Objects.requireNonNull(employeeId, "employeeId required");
        if (salary < 0) throw new IllegalArgumentException("salary >= 0");
    }
    public boolean isHighSalary() { return salary > 100_000; }
}
```

### Text Block
```java
String sql = """
        SELECT t.id, e.name, e.salary
        FROM   employees t
        WHERE  e.status = '%s'
        ORDER  BY e.review_date ASC
        """.formatted(status);
```

### Sealed + Records
```java
sealed interface EmployeeEvent permits EmployeeHiredEvent, EmployeeTerminatedEvent {
    String employeeId();
}
record EmployeeHiredEvent(String employeeId, String name, double salary) implements EmployeeEvent {}
record EmployeeTerminatedEvent(String employeeId, String reason)                 implements EmployeeEvent {}
```

### Pattern Matching + Switch (Java 17)
```java
String result = switch (event) {
    case EmployeeHiredEvent c      -> "Hired: "      + c.employeeId();
    case EmployeeTerminatedEvent r -> "Terminated: " + r.reason();
};
```

### Virtual Threads (Java 21)
```java
try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
    List<Future<String>> futures = ids.stream()
        .map(id -> exec.submit(() -> enrich(id)))
        .toList();
    for (var f : futures) results.add(f.get());
}
```

### Structured Concurrency (Java 21 preview)
```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    var deptTask   = scope.fork(() -> fetchDepartment(id));
    var salaryTask = scope.fork(() -> fetchSalary(name));
    scope.join().throwIfFailed();
    return new EnrichedEmployee(deptTask.get(), salaryTask.get());
}
```

---

## Accessor Naming Quick Reference

| Java 7 POJO | Java 16 Record |
|-------------|----------------|
| `employee.getEmployeeId()` | `employee.employeeId()` |
| `employee.getName()` | `employee.name()` |
| `employee.getSalary()` | `employee.salary()` |

> Records use the **component name directly** — no `get` prefix.

---

## Run All Tests

```bash
# Main project (Java 17 features)
mvn test

# Bonus module (Java 21 features)
cd bonus && mvn test
```
