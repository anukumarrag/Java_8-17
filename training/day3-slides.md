# Day 3 — The Modern Java, Part 1
## *Records · Text Blocks · Helpful NullPointerExceptions*

**Duration:** 1.5 hours  
**Source files:** `module2/records` · `module2/textblocks` · `module2/helpfulnpe`

---

---

## 🎯 Opening Hook — *The 80-Line DTO Problem* `[5 min]`

> *"I'm going to show you a class that holds exactly 5 fields.  
> In Java 8 it takes 80 lines. Java 17 fixes this in **one line**."*

### The 80-Line DTO (show full `TradeDetailsPojo` from `RecordsExample.java`)

```java
public class TradeDetailsPojo {
    private final String tradeId;
    private final String symbol;
    private final double notional;
    private final String counterpartyId;
    private final LocalDate settlementDate;
    private final String status;

    // 6-param constructor…
    // 6 getters…
    // equals() — 12 lines…
    // hashCode() — 6 lines…
    // toString() — 8 lines…
}
// Total: ~80 lines. For FIVE fields.
```

### The Java 17 Answer

```java
public record TradeDetailsRecord(
        String tradeId, String symbol, double notional,
        String counterpartyId, LocalDate settlementDate, String status) {
    // compact constructor for validation
    public TradeDetailsRecord {
        Objects.requireNonNull(tradeId, "tradeId required");
        if (notional < 0) throw new IllegalArgumentException("notional must be >= 0");
    }
}
// Total: ~8 lines. The compiler writes everything else.
```

> **That's a 90 % reduction in boilerplate — for the same functionality.**

---

---

## 📖 Theory Block 1 — Records `[30 min]`

**JEP 395, GA in Java 16**

### What the Compiler Generates for Free

```java
public record TradeDetailsRecord(String tradeId, String symbol, double notional,
                                  String counterpartyId, LocalDate settlementDate,
                                  String status) {}
```

The compiler automatically creates:

| Generated member | What it looks like |
|------------------|--------------------|
| Canonical constructor | `public TradeDetailsRecord(String tradeId, String symbol, …)` |
| Accessor for each field | `tradeId()`, `symbol()`, `notional()` — **no `get` prefix** |
| `equals(Object)` | Based on ALL components |
| `hashCode()` | Based on ALL components |
| `toString()` | `TradeDetailsRecord[tradeId=T001, symbol=AAPL, …]` |

---

### Accessor Naming — A Small but Important Shift

```java
// Java 7 POJO — getter convention
trade.getTradeId();   trade.getSymbol();   trade.getNotional();

// Java 16 Record — accessor (same name as the component)
trade.tradeId();      trade.symbol();      trade.notional();
```

> This aligns with method references:
> `trades.stream().map(Trade::symbol)` instead of `Trade::getSymbol`

---

### The Compact Constructor — Validation Without Repetition

```java
public record TradeDetailsRecord(
        String tradeId, String symbol, double notional,
        String counterpartyId, LocalDate settlementDate, String status) {

    // Compact constructor: no parameter list, no explicit assignments
    // The compiler assigns all fields AFTER this block runs
    public TradeDetailsRecord {
        Objects.requireNonNull(tradeId,        "tradeId required");
        Objects.requireNonNull(symbol,         "symbol required");
        Objects.requireNonNull(counterpartyId, "counterpartyId required");
        Objects.requireNonNull(settlementDate, "settlementDate required");
        Objects.requireNonNull(status,         "status required");
        if (notional < 0) throw new IllegalArgumentException("notional must be >= 0");
    }
}
```

> **Key:** You write the validation; the compiler writes `this.tradeId = tradeId;` etc.

---

### What You CAN Add to a Record

```java
public record TradeDetailsRecord(…) {

    // ✅ Custom instance method (derived value)
    public boolean isHighValue() {
        return notional > 1_000_000.0;
    }

    // ✅ Static factory method
    public static TradeDetailsRecord draft(String tradeId, String symbol,
                                           double notional, String counterpartyId) {
        return new TradeDetailsRecord(tradeId, symbol, notional,
                counterpartyId, LocalDate.now(), "DRAFT");
    }

    // ✅ Static field (constant)
    public static final double HIGH_VALUE_THRESHOLD = 1_000_000.0;
}
```

---

### What You CANNOT Do with a Record

| Restriction | Reason |
|-------------|--------|
| Cannot extend another class | Already implicitly extends `java.lang.Record` |
| Cannot declare instance fields | All state must be in the record components |
| Cannot be `abstract` | Records are always `final` (implicitly) |
| Cannot mutate components | All fields are `private final` |
| Cannot add a non-canonical constructor without delegating | Must call canonical constructor |

> **Records CAN implement interfaces** — this is the key extension point.

---

### Records + Interfaces

```java
public interface Auditable {
    String auditSummary();
}

public record AuditedTrade(String tradeId, String action, String performedBy)
        implements Auditable {
    @Override
    public String auditSummary() {
        return "[AUDIT] %s performed '%s' on trade %s"
                .formatted(performedBy, action, tradeId);
    }
}
```

**Source:** `RecordsExample.AuditedTrade`

---

### Generic Records and Nested Records

```java
// Generic Pair record
public record Pair<A, B>(A first, B second) {}

Pair<String, Double> priceQuote = new Pair<>("AAPL", 182.50);
System.out.println(priceQuote.first());   // → "AAPL"

// Nested records compose naturally
public record TradeConfirmation(
        TradeDetailsRecord trade,
        String confirmationRef,
        LocalDate confirmedAt
) {}
```

**Source:** `RecordsExample.Pair`, `RecordsExample.TradeConfirmation`

---

## 💻 Hands-On 1 — DTO → Record Conversion `[15 min]`

> **Convert the following `CustomerDTO` POJO to a record with validation.**

```java
// Before — CustomerDTO.java (POJO, ~60 lines)
public class CustomerDTO {
    private final String customerId;
    private final String name;
    private final String email;
    private final String tier;   // "GOLD", "SILVER", "BRONZE"

    public CustomerDTO(String customerId, String name, String email, String tier) {
        if (customerId == null || customerId.isBlank()) throw new …;
        // ... 3 more null checks ...
        this.customerId = customerId; this.name = name;
        this.email = email; this.tier = tier;
    }
    public String getCustomerId() { return customerId; }
    public String getName()       { return name; }
    public String getEmail()      { return email; }
    public String getTier()       { return tier; }

    public boolean isGoldTier()   { return "GOLD".equals(tier); }

    // equals, hashCode, toString …
}
```

> **Your task:**
> 1. Convert to a `record CustomerDTO`
> 2. Move validation into a compact constructor
> 3. Keep the `isGoldTier()` convenience method

<details>
<summary>💡 Reveal solution</summary>

```java
public record CustomerDTO(
        String customerId,
        String name,
        String email,
        String tier) {

    public CustomerDTO {
        if (customerId == null || customerId.isBlank())
            throw new IllegalArgumentException("customerId required");
        Objects.requireNonNull(name,  "name required");
        Objects.requireNonNull(email, "email required");
        if (!Set.of("GOLD", "SILVER", "BRONZE").contains(tier))
            throw new IllegalArgumentException("Invalid tier: " + tier);
    }

    public boolean isGoldTier() {
        return "GOLD".equals(tier);
    }
}
```
</details>

---

---

## 📖 Theory Block 2 — Text Blocks `[20 min]`

**JEP 378, GA in Java 15**

### The Problem — String Noise

```java
// SQL — hard to read, easy to break (missing space before FROM)
String sql = "SELECT t.trade_id, t.symbol, t.notional, " +
             "       t.counterparty_id, t.settlement_date " +
             "FROM   trades t " +
             "JOIN   counterparties c ON c.id = t.counterparty_id " +
             "WHERE  t.status = '" + status + "' " +
             "  AND  t.settlement_date >= CURRENT_DATE " +
             "ORDER  BY t.settlement_date ASC";
```

```java
// JSON — quote escaping makes this nearly unreadable
String json = "{\n" +
              "  \"tradeId\": \"" + tradeId + "\",\n" +
              "  \"symbol\": \"" + symbol + "\",\n" +
              "  \"notional\": " + notional + "\n" +
              "}";
```

---

### The Solution — Text Block

```java
// SQL — reads exactly like it looks in a SQL editor
String sql = """
        SELECT t.trade_id,
               t.symbol,
               t.notional,
               t.counterparty_id,
               t.settlement_date
        FROM   trades t
        JOIN   counterparties c ON c.id = t.counterparty_id
        WHERE  t.status = '%s'
          AND  t.settlement_date >= CURRENT_DATE
        ORDER  BY t.settlement_date ASC
        """.formatted(status);
```

```java
// JSON — no escaping needed for internal quotes
String json = """
        {
          "tradeId": "%s",
          "symbol": "%s",
          "notional": %s,
          "status": "PENDING"
        }
        """.formatted(tradeId, symbol, notional);
```

**Source:** `TextBlockExamples.buildTradeQuery_After`, `TextBlockExamples.buildTradeJson_After`

---

### How Incidental Whitespace Works

```
Line in source:           |        SELECT *
                          |        FROM trades
                          |        """;
                          ^--------^ (8 spaces of indentation)
```

The compiler strips the 8 leading spaces from every line automatically — the **incidental whitespace** is the amount of leading whitespace common to all content lines.

> **Rule:** The position of the closing `"""` controls the indent stripping.
> Move it left → keep more whitespace. Move it to column 0 → keep all.

---

### Trailing Newline Behaviour

```java
// Closing """ on its own line → trailing newline included
String withNewline = """
        Hello
        """;
// result: "Hello\n"

// Closing """ at end of last content line → no trailing newline
String noNewline = """
        Hello""";
// result: "Hello"
```

---

### New String Methods (Java 15) — Companion to Text Blocks

```java
// indent(n) — add n spaces to every line
String indented = query.indent(4);

// stripIndent() — remove common leading whitespace (same algorithm as text block)
String clean = rawFromFile.stripIndent();

// translateEscapes() — interpret \n, \t etc. in a regular string
String withEscapes = "line1\\nline2".translateEscapes();  // → "line1\nline2"
```

---

### Line Continuation Escape `\`

```java
// Without continuation — multiline string
String multiline = """
        SELECT trade_id
        FROM trades
        """;

// With \ — the newline is suppressed, result is a single line
String singleLine = """
        SELECT trade_id \
        FROM trades \
        WHERE status = 'ACTIVE'
        """;
// → "SELECT trade_id FROM trades WHERE status = 'ACTIVE'\n"
```

---

## 💻 Hands-On 2 — String Concatenation → Text Block `[10 min]`

> **Convert these 3 concatenated strings to text blocks + `.formatted()`**

**Task A — HTML template**
```java
// Before
String html = "<html>\n" +
              "  <body>\n" +
              "    <h1>Trade Confirmation</h1>\n" +
              "    <p>Trade ID: " + tradeId + "</p>\n" +
              "    <p>Status: " + status + "</p>\n" +
              "  </body>\n" +
              "</html>";
```

**Task B — XML FpML message** (see `TextBlockExamples.buildFpmlMessage_After` for reference)

**Task C — INSERT SQL**
```java
// Before
String insert = "INSERT INTO trades (trade_id, symbol, notional, status) " +
                "VALUES ('" + id + "', '" + symbol + "', " + notional + ", 'PENDING')";
```

<details>
<summary>💡 Reveal Task A solution</summary>

```java
String html = """
        <html>
          <body>
            <h1>Trade Confirmation</h1>
            <p>Trade ID: %s</p>
            <p>Status: %s</p>
          </body>
        </html>
        """.formatted(tradeId, status);
```
</details>

---

---

## 📖 Theory Block 3 — Helpful NullPointerExceptions `[10 min]`

**JEP 358, preview in Java 14, default-on from Java 15**

### The Old NPE — No Context

```
Exception in thread "main" java.lang.NullPointerException
    at com.bank.trading.TradeProcessor.process(TradeProcessor.java:47)
```

> You see the line number. But on line 47 you have:
> ```java
> String city = trade.counterparty().address().city();
> ```
> Which one is null? `trade`? `counterparty()`? `address()`?  
> Open debugger. Set breakpoint. Reproduce. **5 minutes lost.**

---

### The New NPE — Precise Context

```
Exception in thread "main" java.lang.NullPointerException:
Cannot invoke "Address.city()" because the return value of
"Counterparty.address()" is null
    at com.bank.trading.TradeProcessor.process(TradeProcessor.java:47)
```

> Immediately clear: `address()` returned null. No debugger needed.

---

### Real Examples from `HelpfulNpeExamples.java`

```java
// Array access
int[] data = null;
int val = data[0];
// → "Cannot load from int array because "data" is null"

// Method call on null return value
trade.counterparty().address().city();
// → "Cannot invoke "Address.city()" because the return value of
//     "Counterparty.address()" is null"

// Null assignment to unboxed variable
Integer boxed = null;
int primitive = boxed;
// → "Cannot unbox because "boxed" is null"
```

---

### Zero Code Changes Required

> This is a **JVM enhancement**, not a language change.
> Your code stays exactly as-is. The JVM computes the precise description at runtime.

**Enabling:**
- Java 14–14: opt-in with `-XX:+ShowCodeDetailsInExceptionMessages`
- **Java 15+: on by default** — nothing to do

---

---

## 🔑 Day 3 Takeaways `[5 min]`

> **Card 1 — Records:**  
> A record is an immutable data carrier. The compiler generates the constructor, all accessors (no `get` prefix), `equals`, `hashCode`, and `toString`. Use a compact constructor for validation. Records completely replace Lombok `@Data` / `@Value` for data-only classes.

> **Card 2 — Text Blocks:**  
> Use `"""` delimiters for any multi-line string — SQL, JSON, HTML, XML. Use `.formatted()` for interpolation. The closing `"""` placement controls the trailing newline.

> **Card 3 — Helpful NPE:**  
> From Java 15+ the JVM tells you exactly which variable was null in a method chain. Zero code changes needed. This alone saves significant debugging time in production.

---

### 📚 Pre-read for Day 4

> *"What is a `sealed` interface? Look at `SealedClassesExample.java` and count how many  
> classes are allowed to implement `TradeEvent`. What happens if you try to add a new one outside the file?"*

---

> **End of Day 3**  
> Source: `src/main/java/com/training/java817/module2/`  
> Tests: `src/test/java/com/training/java817/module2/`  
> Run: `mvn test -Dtest="Records*,TextBlock*,HelpfulNpe*"`
