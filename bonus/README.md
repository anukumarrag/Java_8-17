# Java 18–21+ Bonus Module

This folder is a **standalone Maven project** covering Java features released
**after Java 17**. It requires **Java 21 (LTS)** or later to compile and run.

---

## How to build and run

```bash
# Make sure Java 21 is your active JDK
java -version   # should show 21.x

cd bonus
mvn clean test
```

For features that are still **preview** (Structured Concurrency, Unnamed
Variables in Java 21), add the preview flag:

```bash
mvn clean test -Dmaven.compiler.compilerArgs="--enable-preview" \
               -Dmaven.surefire.argLine="--enable-preview"
```

---

## Features covered

| Feature | JEP | Status | Java Version |
|---|---|---|---|
| Pattern Matching for Switch | JEP 441 | GA | Java 21 |
| Record Patterns | JEP 440 | GA | Java 21 |
| Virtual Threads | JEP 444 | GA | Java 21 |
| Sequenced Collections | JEP 431 | GA | Java 21 |
| Structured Concurrency | JEP 453 | Preview | Java 21 / 22 |
| Unnamed Variables & Patterns | JEP 456 | Preview→GA | Java 21–22 |

---

## Module structure

```
bonus/
├── pom.xml                              ← standalone Maven project (Java 21)
└── src/
    ├── main/java/com/training/bonus/
    │   ├── patternswitch/               ← Pattern Matching for Switch (JEP 441)
    │   │   └── PatternMatchingSwitchExamples.java
    │   ├── recordpatterns/              ← Record Patterns (JEP 440)
    │   │   └── RecordPatternsExamples.java
    │   ├── virtualthreads/              ← Virtual Threads (JEP 444)
    │   │   └── VirtualThreadsExamples.java
    │   ├── sequencedcollections/        ← Sequenced Collections (JEP 431)
    │   │   └── SequencedCollectionsExamples.java
    │   ├── structuredconcurrency/       ← Structured Concurrency (JEP 453)
    │   │   └── StructuredConcurrencyExamples.java
    │   └── unnamedvariables/            ← Unnamed Variables & Patterns (JEP 456)
    │       └── UnnamedVariablesExamples.java
    └── test/java/com/training/bonus/
        ├── patternswitch/               ← Tests for Pattern Matching for Switch
        ├── recordpatterns/              ← Tests for Record Patterns
        ├── virtualthreads/              ← Tests for Virtual Threads
        └── sequencedcollections/        ← Tests for Sequenced Collections
```

---

## Quick feature summaries

### Pattern Matching for Switch (Java 21)
Extends switch to support **type patterns** and **guarded patterns**. Works with
sealed interfaces for exhaustive, compiler-checked dispatch without if-else
chains.

```java
String result = switch (event) {
    case TradeEvent.Created c                 -> "CREATED "  + c.symbol();
    case TradeEvent.Executed e when e.price() > 500 -> "PREMIUM";
    case TradeEvent.Rejected r                -> "REJECTED: " + r.reason();
    case null                                 -> "NULL_EVENT";
    default                                   -> "OTHER";
};
```

### Record Patterns (Java 21)
Deconstruct records **inline** in instanceof and switch, binding components
directly without calling accessor methods.

```java
if (obj instanceof Trade(String id, _, Money(double amt, String cur), _)) {
    System.out.println("Trade " + id + ": " + amt + " " + cur);
}
```

### Virtual Threads (Java 21)
Lightweight, JVM-managed threads. Swap your thread pool executor for
`Executors.newVirtualThreadPerTaskExecutor()` to handle millions of concurrent
I/O-bound tasks without reactive frameworks.

```java
try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
    var f = exec.submit(() -> fetchFromDatabase(id));
    return f.get();
}
```

### Sequenced Collections (Java 21)
New `SequencedCollection`, `SequencedSet`, and `SequencedMap` interfaces add
`getFirst()`, `getLast()`, `addFirst()`, `addLast()`, `removeFirst()`,
`removeLast()`, and `reversed()` to all ordered collection types.

```java
list.getFirst();         // no more list.get(0)
list.getLast();          // no more list.get(list.size()-1)
list.reversed();         // O(1) view, no copy
linkedHashSet.getLast(); // O(1) now, not O(n)!
```

### Structured Concurrency (Java 21 preview)
`StructuredTaskScope` groups concurrent tasks so that cancellation and
error propagation happen automatically when the scope exits.

```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    var cp    = scope.fork(() -> fetchCounterparty(id));
    var price = scope.fork(() -> fetchPrice(symbol));
    scope.join().throwIfFailed();
    return new EnrichedTrade(cp.get(), price.get());
}
```

### Unnamed Variables & Patterns (Java 22)
Use `_` to explicitly mark variables that are required by syntax but not used.

```java
for (var _ : trades) count++;             // loop without needing the element
catch (Exception _) { return false; }    // swallow exception without naming it
if (obj instanceof Trade(String id, _, _, _)) { ... }  // bind only id
```
