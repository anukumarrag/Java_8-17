# 🔬 Hands-On Lab Guide
## Modernising with Java 17 — All 5 Days

**Repository:** `src/main/java/com/training/java817/` (Days 1–4) | `bonus/src/main/java/com/training/bonus/` (Day 5)  
**Verify your work:** `mvn test` from the project root

---

## 🚦 Environment Setup

```bash
# 1. Confirm your Java version
java -version          # must show 17 or higher
mvn -version           # must show 3.6+

# 2. Build and verify all tests pass before starting
cd /path/to/Java_8-17
mvn test               # all tests should be GREEN

# 3. Open the project in your IDE
# IntelliJ: File → Open → select the pom.xml
# Eclipse:  File → Import → Maven → Existing Maven Projects
```

---

---

## 📅 Day 1 Labs — The Functional Revolution

**Source package:** `com.training.java817.module1.lambdas` · `.streams` · `.optional`

---

### Lab 1A — Lambda Rewrite `[15 min]`

**File to edit:** Create a new class `module1/lambdas/LambdaLab.java`

**Instructions:**

1. Open `LambdaExamples.java` and study the `_Before` methods.
2. In your new `LambdaLab` class, implement the following three methods using lambdas and method references (no for-loops, no anonymous inner classes):

```java
// Task A: Sort a List<String> alphabetically using a lambda
public List<String> sortAlphabetically(List<String> items)

// Task B: Filter strings that start with a given prefix using stream().filter()
public List<String> filterByPrefix(List<String> items, String prefix)

// Task C: Convert all strings to uppercase using a method reference
public List<String> toUpperCase(List<String> items)
```

3. Also implement:

```java
// Task D: Compose two Functions:
//   step 1: trim whitespace
//   step 2: convert to uppercase
// Use Function.andThen()
public String trimAndUpperCase(String input)

// Task E: Create a Predicate that is true when an employee name has length > 4 AND starts with 'A'
// Use Predicate.and()
public Predicate<String> longAName()
```

**Expected outputs:**
```
sortAlphabetically(["ENGINEERING","MARKETING","SALES"]) → ["ENGINEERING","MARKETING","SALES"]
filterByPrefix(["ACTIVE_E1","ONBOARDING_E2","ACTIVE_E3"], "ACTIVE") → ["ACTIVE_E1","ACTIVE_E3"]
toUpperCase(["alice","bob"]) → ["ALICE","BOB"]
trimAndUpperCase("  alice  ") → "ALICE"
longAName().test("ENGINEERING") → true,  longAName().test("HR") → false (length <= 4),  longAName().test("ABB") → false (length <= 4)
```

---

### Lab 1B — Stream Pipeline Kata `[15 min]`

**File to edit:** Create `module1/streams/StreamLab.java`

**Setup — use this employee list:**
```java
List<Employee> employees = List.of(
    new Employee("E001", "Alice",   "ENGINEERING", 95_000,  "ACTIVE"),
    new Employee("E002", "Bob",     "MARKETING",   45_000,  "ACTIVE"),
    new Employee("E003", "Charlie", "ENGINEERING",  72_000,  "ONBOARDING"),
    new Employee("E004", "Diana",   "SALES",        81_000,  "ACTIVE"),
    new Employee("E005", "Eve",     "HR",          120_000, "ACTIVE"),
    new Employee("E006", "Frank",   "FINANCE",      63_000,  "ACTIVE"),
    new Employee("E007", "Grace",   "MARKETING",    55_000,  "RESIGNED")
);
```

**Implement these 5 methods using a single stream pipeline each:**

```java
// 1. IDs of ACTIVE employees sorted by salary DESC, top 3, comma-separated
public String topThreeActiveIds(List<Employee> employees)
// Expected: "E005, E001, E004"

// 2. Total salary of all ACTIVE employees
public double totalActiveSalary(List<Employee> employees)
// Expected: 404_000.0

// 3. Distinct departments where salary > 70_000, sorted alphabetically
public List<String> highSalaryDepartments(List<Employee> employees)
// Expected: ["ENGINEERING", "HR", "SALES"]

// 4. Map of status → count of employees
public Map<String, Long> countByStatus(List<Employee> employees)
// Expected: {ACTIVE=5, ONBOARDING=1, RESIGNED=1}

// 5. Is there any employee with salary > 110_000?
public boolean hasVeryHighSalary(List<Employee> employees)
// Expected: true (Eve = 120K)
```

---

### Lab 1C — Optional Chain `[10 min]`

**File to edit:** Create `module1/optional/OptionalLab.java`

```java
// Given these records
record Address(String street, String city) {}
record Department(String id, String name, Address address) {}
record Employee(String id, Department department) {}

// Implement WITHOUT any null checks (use Optional chaining):

// 1. Get city, return "UNKNOWN" if any link is null
public String getCity(Employee employee)

// 2. Get department name from a repository method that returns Optional<Department>
//    Return "NOT_FOUND" if absent
public String getDepartmentName(String id)

// 3. Resolve a name: strip the input, if blank return "DEFAULT_NAME"
//    Use Optional + filter + orElseGet
public String resolveName(String raw)

// 4. Throw IllegalArgumentException("Department not found: " + id)
//    if department is absent
public Department requireDepartment(String id)
```

---

---

## 📅 Day 2 Labs — The Evolution Years

**Source package:** `com.training.java817.module1.var` · `.string` · `.collections` · `module2.switchexpressions`

---

### Lab 2A — `var` Adoption `[5 min]`

**Instructions:** Open `LocalVarInferenceExamples.java`. Find `groupByDepartment_Before` and rewrite it as `groupByDepartment_After` using `var` for all local variables. Ensure:
- `var result = new HashMap<…>()`  
- `var trade : trades` in the for-each  
- `var group = result.computeIfAbsent(…)`

Then identify 3 places in the method where `var` would be **inappropriate** (e.g., method parameters) and write a comment explaining why.

---

### Lab 2B — Collection Modernisation `[8 min]`

**Create:** `module1/collections/CollectionLab.java`

```java
// Convert each of these to use Java 9+ factory methods:

// Task A: Create an immutable list of 4 trade statuses
// BEFORE:
List<String> statuses = new ArrayList<>();
statuses.add("ONBOARDING"); statuses.add("ACTIVE");
statuses.add("ON_LEAVE"); statuses.add("RESIGNED");
statuses = Collections.unmodifiableList(statuses);

// Task B: Create an immutable Set of valid asset classes
// BEFORE: 5 add() calls + unmodifiableSet()

// Task C: Create an immutable Map of asset class → review days
// EQUITY→2, FIXED_INCOME→1, FOREX→1, COMMODITY→3
// BEFORE: 4 put() calls + unmodifiableMap()

// Task D: Use stream().collect(Collectors.toUnmodifiableList())
// to filter ACTIVE employees and return an immutable list
```

---

### Lab 2C — String API Modernisation `[5 min]`

**Instructions:** Find and replace the following patterns in a given string-processing snippet:

```java
// Replace each "Before" idiom with the modern Java 11 equivalent:

// 1. Before: name != null && !name.trim().isEmpty()
//    After:  ??? (hint: isBlank + strip)

// 2. Before: name.trim().toUpperCase()
//    After:  ???

// 3. Before: String.join("\n", lineList)  where lineList was built with a loop
//    After:  ??? (hint: lines())

// 4. Before: String separator = ""; for loop building a separator line
//    After:  ??? (hint: repeat())
```

---

### Lab 2D — Switch Expression Refactor `[12 min]`

**Create:** `module2/switchexpressions/SwitchLab.java`

```java
// Refactor these if-else chains to switch expressions:

// Task A: Map EmployeeStatus to an HTTP status code
// DRAFT→202, PENDING→202, EXECUTED→200, SETTLED→200,
// REJECTED→422, CANCELLED→410
public int toHttpStatus(EmployeeStatus status)  // use switch expression

// Task B: Map EmployeeLevel to a notification priority string
// JUNIOR→"LOW", MID→"MEDIUM", SENIOR→"HIGH",
// LEAD→"HIGH", MANAGER→"CRITICAL"
public String notificationPriority(EmployeeLevel level)  // use switch expression

// Task C: Yield multi-line result
// For REJECTED: include "Review required – contact operations"
// For CANCELLED: include "Audit trail preserved"
// For all others: "Status: " + status.name()
public String statusDetail(EmployeeStatus status)  // use yield in block arms
```

---

---

## 📅 Day 3 Labs — The Modern Java, Part 1

**Source package:** `com.training.java817.module2.records` · `.textblocks` · `.helpfulnpe`

---

### Lab 3A — Record Conversion `[15 min]`

**Task:** Convert the following POJO to a record with full validation.

```java
// BEFORE — EmployeeDTO (copy this to a new file EmployeeRecord.java and convert it)
public class EmployeeDTO {
    private final String employeeId;   // must not be blank
    private final String name;         // must not be null
    private final String email;        // must contain "@"
    private final String department;   // must be ENGINEERING, MARKETING, SALES, FINANCE, or HR

    // Full constructor, 4 getters, equals, hashCode, toString
    // + boolean isSeniorDept()      { return Set.of("ENGINEERING","FINANCE").contains(department); }
    // + boolean isRemoteEligible()  { return !"SALES".equals(department); }
}
```

**Requirements for `EmployeeRecord`:**
1. One-line record declaration (4 components)
2. Compact constructor with all validation (department must be in `Set.of("ENGINEERING","MARKETING","SALES","FINANCE","HR")`)
3. Retain `isSeniorDept()` and `isRemoteEligible()` as instance methods
4. Add a static factory: `EmployeeRecord.hr(String id, String name, String email)` that creates an HR employee

**Verify:** Record's `equals` works correctly for two instances with same values.

---

### Lab 3B — Text Block Conversion `[15 min]`

**Task:** Convert the following concatenated strings to text blocks.

```java
// A — Elasticsearch query JSON (add index + query for status filter)
String esQuery = "{\n" +
                 "  \"query\": {\n" +
                 "    \"bool\": {\n" +
                 "      \"must\": [\n" +
                 "        { \"term\": { \"status\": \"" + status + "\" } },\n" +
                 "        { \"range\": { \"salary\": { \"gte\": " + minSalary + " } } }\n" +
                 "      ]\n" +
                 "    }\n" +
                 "  }\n" +
                 "}";

// B — Spring @Query-style JPQL
String jpql = "SELECT e FROM Employee e " +
              "WHERE e.status = :status " +
              "AND e.name IN :names " +
              "ORDER BY e.reviewDate ASC";

// C — HTML email template with employeeId, name, salary, status variables
String html = "<html>...(build it)...</html>";
```

---

### Lab 3C — Spot the NPE `[5 min]`

**Run the following snippet with Java 15+ and read the helpful NPE message:**

```java
record Address(String city) {}
record Dept(String name, Address address) {}
record Employee(String id, Dept department) {}

Employee e = new Employee("E001", new Dept("Engineering", null));  // address is null
System.out.println(e.department().address().city());
```

**Questions:**
1. What does the NPE message say?
2. Which specific call is null?
3. Rewrite the lookup using `Optional` to avoid the NPE entirely.

---

---

## 📅 Day 4 Labs — Full Workshop (Module 3)

**Source:** `src/main/java/com/training/java817/module3/`  
**Verify:** `mvn test -Dtest="WorkshopTest"`

---

### Workshop Task 1 — DTO → Record `[10 min]`

**Reference:** `module3/before/Employee.java` → `module3/after/EmployeeRecord.java`

**Steps:**
1. Open `module3/before/Employee.java` — study the fields and constructor validation.
2. Create the equivalent `EmployeeRecord` as a record.
3. Move all constructor validation into a compact constructor.
4. Keep `isHighSalary()` and `toDisplayString()` as instance methods.
5. Run `WorkshopTest.testTask1_RecordCreation` — it must pass.

---

### Workshop Task 2 — String Concatenation → Text Block `[5 min]`

**Reference:** `module3/before/TransactionService.buildSearchQuery()` → `module3/after/ModernTransactionService.buildSearchQuery()`

**Steps:**
1. Find `buildEmployeeQuery(String status, String name)` in `TransactionService.java`.
2. Replace the multi-line concatenation with a single text block.
3. Use `.formatted(status, name)` for parameter injection.
4. Run `WorkshopTest.testTask2_TextBlockQuery` — it must pass.

---

### Workshop Task 3 — Enum → Sealed Hierarchy `[10 min]`

**Reference:** `module3/before/TransactionService.EmployeeEventLegacy` → `module3/after/EmployeeEvent.java` + event records

**Steps:**
1. In `module3/after/`, the sealed interface `EmployeeEvent` and its 4 record implementations already exist.
2. Study each record: `EmployeeHiredEvent`, `EmployeeUpdatedEvent`, `EmployeePromotedEvent`, `EmployeeTerminatedEvent`.
3. Note what data each carries that was impossible with the legacy enum.
4. Run `WorkshopTest.testTask3_SealedEvents` — it must pass.

**Reflection question:** What happens at compile time if you add a `EmployeePricedEvent` to the `permits` list but forget to add a case in `processEvent`?

---

### Workshop Task 4 — Pattern Matching + Switch Expression `[15 min]`

**Reference:** `module3/before/TransactionService.processEventLegacy()` → `module3/after/ModernTransactionService.processEvent()`

**Steps:**
1. Open `TransactionService.processEventLegacy` — count the number of casts.
2. Open `ModernTransactionService.processEvent` — verify zero casts.
3. Extend `processEvent` to add a guard: if `EmployeeHiredEvent` has salary > 5,000,000, prepend `"[LARGE TRADE] "` to the message.
4. Run `WorkshopTest.testTask4_PatternMatchingDispatch` — it must pass.

---

---

## 📅 Day 5 Labs — Bonus: The Future Is Now

**Source:** `bonus/src/main/java/com/training/bonus/`  
**Build:** `cd bonus && mvn test`

---

### Lab 5A — Virtual Threads `[10 min]`

**File:** `bonus/virtualthreads/VirtualThreadsExamples.java`

1. Run `VirtualThreadsExamples.main()` and observe the output.
2. In a new class `VirtualThreadLab`, implement:

```java
// Spawn 1,000 virtual threads, each printing "Hello from virtual thread N"
// Use Thread.ofVirtual().start()
// Join all threads before returning
public void helloFromThousandThreads() throws InterruptedException

// Verify that tasks submitted to newVirtualThreadPerTaskExecutor
// run on virtual threads (use Thread.currentThread().isVirtual())
public boolean confirmVirtualExecution() throws Exception
```

---

### Lab 5B — Record Patterns `[10 min]`

**File:** `bonus/recordpatterns/RecordPatternsExamples.java`

1. Study `RecordPatternsExamples.describeEmployeeCity_After` — notice how it deconstructs three levels of nesting in one `instanceof`.
2. Write a method `classifyEmployee(Object obj)` that:
   - Deconstructs `Employee(String id, String name, Money(double amt, String cur), Department dept)`
   - Returns `"Large USD employee in " + city` if salary > 100K and currency is USD
   - Returns `"Standard employee"` otherwise
   - Uses `when` guards and nested record patterns

---

### Lab 5C — Sequenced Collections `[5 min]`

**File:** `bonus/sequencedcollections/SequencedCollectionsExamples.java`

```java
// Replace the verbose Java 17 idioms with Java 21 equivalents:

// 1. Get the first employee from a List<Employee>
// Before: employees.get(0)
// After:  ???

// 2. Get the last employee from a List<Employee>
// Before: employees.get(employees.size() - 1)
// After:  ???

// 3. Iterate a LinkedHashMap in reverse insertion order
// Before: manual reversal with ArrayList + Collections.reverse
// After:  ???
```

---

---

## ✅ Lab Completion Checklist

| Day | Lab | Key Test / Verification |
|-----|-----|------------------------|
| 1 | 1A Lambda Rewrite | Manual: expected outputs match |
| 1 | 1B Stream Kata | Manual: 5 pipeline results correct |
| 1 | 1C Optional Chain | Manual: no null checks, Optional only |
| 2 | 2A var Adoption | Manual: compiles, correct types |
| 2 | 2B Collection Modernisation | Manual: immutable, factory methods |
| 2 | 2C String API | Manual: strip/isBlank/repeat used |
| 2 | 2D Switch Expression | Manual: no `break`, no if-else |
| 3 | 3A Record Conversion | Manual: equals + compact constructor |
| 3 | 3B Text Block | Manual: no `\n` or `+` concatenation |
| 3 | 3C Spot the NPE | Manual: identifies `address()` as null |
| 4 | Workshop Task 1 | `WorkshopTest.testTask1_RecordCreation` ✅ |
| 4 | Workshop Task 2 | `WorkshopTest.testTask2_TextBlockQuery` ✅ |
| 4 | Workshop Task 3 | `WorkshopTest.testTask3_SealedEvents` ✅ |
| 4 | Workshop Task 4 | `WorkshopTest.testTask4_PatternMatchingDispatch` ✅ |
| 5 | 5A Virtual Threads | Manual: `isVirtual()` returns `true` |
| 5 | 5B Record Patterns | Manual: correct classification |
| 5 | 5C Sequenced Collections | Manual: `getFirst()` / `getLast()` / `reversed()` |

---

> **Pro tip:** Each source file has a `main()` method — run it directly to see the before/after output  
> without writing a test.
