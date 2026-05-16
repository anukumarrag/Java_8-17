# ✅ Java 8 → 17 Migration Checklist
*10 steps to modernise a production Java 8 application*

---

## Before You Start

> **Goal:** Migrate to Java 17 (LTS) safely — without breaking existing behaviour.  
> **Strategy:** Incremental. Run your full test suite after each step.  
> **Recommended path:** Java 8 → 11 (LTS) → 17 (LTS) → 21 (LTS)

---

## The 10-Point Checklist

---

### ☐ Step 1 — Update Toolchain & Build
**Time estimate:** 30 min – 2 hours

**Actions:**
- Upgrade JDK to 17 (download from [adoptium.net](https://adoptium.net))
- Update `pom.xml` (Maven) or `build.gradle`:
  ```xml
  <maven.compiler.source>17</maven.compiler.source>
  <maven.compiler.target>17</maven.compiler.target>
  ```
- Upgrade Maven to 3.8+, Gradle to 7.3+
- Update IDE plugins (IntelliJ: 2022+, Eclipse: 2022-12+)
- Run `mvn compile` — fix any immediate compilation errors before proceeding

**Common error:** `--illegal-access` warnings become errors in Java 17.  
**Fix:** Add `--add-opens` JVM args as needed (Spring Boot Actuator auto-configures these).

---

### ☐ Step 2 — Update Dependencies
**Time estimate:** 1 – 4 hours

**Actions:**
- Run `mvn versions:display-dependency-updates` (or Gradle equivalent)
- Prioritise updates for:

| Dependency | Minimum Java 17-compatible version |
|------------|-----------------------------------|
| Spring Boot | 2.5+ (3.x for Java 17 first-class) |
| Hibernate | 5.6+ (6.x recommended) |
| Jackson | 2.13+ |
| Lombok | 1.18.22+ |
| Mockito | 4.x+ |
| JUnit | 5.8+ |
| Byte Buddy (used by Mockito, Hibernate) | 1.12+ |

- Run `mvn test` after each major dependency update
- Remove Lombok `@Data` / `@Value` annotations as you convert classes to records (Step 7)

---

### ☐ Step 3 — Replace `null` with `Optional`
**Time estimate:** 2 – 8 hours (depends on codebase size)

**Actions:**
- Find all `@Nullable` return types and convert to `Optional<T>`:
  ```bash
  # Find methods that return null in your source
  grep -rn "return null;" src/main/java/
  ```
- Replace defensive null-check chains with `.map().orElse()` pipelines
- Replace `if (x != null) { x.doSomething(); }` with `Optional.ofNullable(x).ifPresent(…)`
- Update callers: replace `!= null` checks with `Optional.isPresent()` or `.ifPresent()`

**Do NOT:**
- Use `Optional` as a field type or method parameter
- Call `.get()` without `.isPresent()` (defeats the purpose)

---

### ☐ Step 4 — Migrate Date/Time
**Time estimate:** 2 – 6 hours

**Actions:**
- Find `java.util.Date`, `java.util.Calendar`, `SimpleDateFormat` usages:
  ```bash
  grep -rn "java.util.Date\|Calendar\|SimpleDateFormat" src/main/java/
  ```
- Replace with `java.time` equivalents:

| Old | New |
|-----|-----|
| `new Date()` | `LocalDateTime.now()` or `Instant.now()` |
| `new Date(millis)` | `Instant.ofEpochMilli(millis)` |
| `Calendar.getInstance()` | `ZonedDateTime.now(ZoneId.of("UTC"))` |
| `SimpleDateFormat("dd/MM/yyyy")` | `DateTimeFormatter.ofPattern("dd/MM/yyyy")` |
| `Date.getTime()` | `Instant.toEpochMilli()` |

- Update JPA entities: use `@Column` with `LocalDate` / `LocalDateTime` (requires Hibernate 5.2+)

---

### ☐ Step 5 — Modernise Collections
**Time estimate:** 1 – 3 hours

**Actions:**
- Replace small fixed-content `ArrayList`/`HashSet`/`HashMap` initialisations with factory methods:
  ```bash
  # Find candidates: new ArrayList followed by add() calls
  grep -n "new ArrayList" src/main/java/ -r
  ```
  ```java
  // Before
  List<String> statuses = new ArrayList<>();
  statuses.add("ONBOARDING"); statuses.add("ACTIVE");
  
  // After
  List<String> statuses = List.of("ONBOARDING", "ACTIVE");
  ```
- Replace `Collections.unmodifiableList(list)` with `List.copyOf(list)` where a defensive copy is needed
- Replace `stream.collect(Collectors.toList())` with `stream.toList()` (Java 16, unmodifiable)

**Watch out:** `List.of()` rejects null elements — check for null before migrating.

---

### ☐ Step 6 — Apply `var` Selectively
**Time estimate:** 1 – 2 hours

**Actions:**
- Apply `var` where the type is **obvious from the right-hand side**:
  - Constructor calls: `var service = new EmployeeService()`
  - Long generic types: `var map = new HashMap<String, List<Employee>>()`
  - For-each loops: `for (var employee : employees)`
  - Try-with-resources: `try (var conn = dataSource.getConnection())`
- **Do not** apply `var` mechanically everywhere — readability must improve

**Quick win:**
```bash
# Find long generic declarations as candidates
grep -n "Map<.*Map<.*>" src/main/java/ -r
```

---

### ☐ Step 7 — Convert POJOs to Records
**Time estimate:** 2 – 8 hours

**Actions:**
- Identify classes that are **pure data carriers** (no mutable state, no inheritance):
  - DTOs, Value Objects, API request/response bodies, event objects
  ```bash
  # Find classes with only getters (no setters = immutable candidates)
  grep -rn "public.*get[A-Z]" src/main/java/ | cut -d: -f1 | sort | uniq -c | sort -rn
  ```
- For each candidate:
  1. Check it doesn't extend another class (records can't extend)
  2. Check all fields are `final` (or can be made so)
  3. Convert: replace class + all boilerplate with `record`
  4. Update callers: replace `getEmployee()` → `employee()` accessor names
- Remove Lombok `@Value` and `@Data` annotations after conversion

**Tip:** Rename accessors systematically with IDE refactoring — *Rename* each `getXxx()` to `xxx()`.

---

### ☐ Step 8 — Use Text Blocks for Embedded Strings
**Time estimate:** 1 – 3 hours

**Actions:**
- Find multi-line string concatenations (SQL, JSON, XML, HTML):
  ```bash
  grep -rn '"\s*+' src/main/java/ | grep -v "//\|test"
  ```
- Convert each to a text block with `.formatted()`:
  - SQL queries in repositories
  - JSON in integration/test classes
  - XML in FIX/FpML message builders
  - HTML in email templates

**Tip:** IntelliJ suggests text block conversion automatically when it detects `\n` + `+` patterns.

---

### ☐ Step 9 — Replace Enum-Based Type Dispatching with Sealed Classes
**Time estimate:** 2 – 6 hours

**Actions:**
- Find enum + `instanceof` hacks or marker interfaces with unlimited implementors:
  ```bash
  grep -rn "interface.*Event\|interface.*Command\|interface.*State" src/main/java/
  ```
- For each closed hierarchy (you own all subtypes):
  1. Add `sealed` to the interface/class
  2. Add `permits` clause listing all known subtypes
  3. Mark each permitted subtype as `final`, `sealed`, or `non-sealed`
  4. Replace if/instanceof chains with pattern matching (`instanceof X x`) or switch expressions
  5. Remove `default` cases from switch — let the compiler enforce exhaustiveness

---

### ☐ Step 10 — Refactor if/instanceof Chains to Pattern Matching
**Time estimate:** 2 – 4 hours

**Actions:**
- Find redundant `instanceof` + cast patterns:
  ```bash
  grep -A1 "instanceof" src/main/java/ -rn | grep -v "pattern\|//\|test"
  ```
- Convert each pair:
  ```java
  // Before
  if (event instanceof EmployeeHiredEvent) {
      EmployeeHiredEvent c = (EmployeeHiredEvent) event;  // redundant cast
      handle(c);
  }
  
  // After
  if (event instanceof EmployeeHiredEvent c) {
      handle(c);
  }
  ```
- For sealed types, consider upgrading to a switch expression (requires Java 17 + pattern matching, Java 21 for type patterns in switch)
- Add guard conditions with `&&` for additional filtering

---

## Post-Migration Verification

```bash
# Full build and test — must be all green
mvn clean test

# Check for any remaining deprecated APIs
mvn compile 2>&1 | grep -i "warning\|deprecated"

# Scan for remaining old Date/Calendar usage
grep -rn "java.util.Date\|SimpleDateFormat\|Calendar.getInstance" src/main/java/

# Confirm no test coverage was lost
mvn jacoco:report   # if JaCoCo is configured
```

---

## Common Pitfalls & Solutions

| Pitfall | Solution |
|---------|---------|
| `--illegal-access` becomes an error | Add specific `--add-opens` flags or upgrade the dependency that uses reflection |
| `List.of()` throws NPE | Check for null values before migration; use `new ArrayList<>()` then `Collections.unmodifiableList()` if nulls are valid |
| Record accessor names break callers | Use IDE rename refactoring: `getName()` → `name()` |
| `final` fields after record migration break JSON deserialisation | Add `@JsonCreator` or upgrade Jackson to 2.13+ (native record support) |
| Sealed class in a multi-module project | All permitted subtypes must be in the same package or module |
| `var` obscures the type in code review | Use explicit types for public-facing code; `var` only for local implementation details |
| Spring `@Transactional` on record | Records are `final` — Spring CGLib proxy cannot subclass them. Use interface-based proxying (`spring.aop.proxy-target-class=false`) or keep the service as a class |

---

## Next Step After Java 17 — Upgrade to Java 21

Once stable on Java 17, the path to Java 21 (next LTS) is short:

| New in 21 | Action |
|-----------|--------|
| Virtual Threads | Replace `newFixedThreadPool` with `newVirtualThreadPerTaskExecutor` for I/O-bound tasks |
| Sequenced Collections | Replace `list.get(list.size()-1)` with `list.getLast()` |
| Record Patterns | Upgrade pattern matching to destructure records inline |
| Pattern Matching in Switch | Upgrade sealed-type switch expressions to use type patterns |
