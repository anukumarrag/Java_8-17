package com.training.java817.module1.optional;

import java.util.Optional;

/**
 * =============================================================================
 * MODULE 1 – OPTIONAL (Java 8)
 * =============================================================================
 *
 * THEORY
 * ------
 * Optional<T> is a container object that may or may not contain a non-null
 * value.  It forces the caller to explicitly handle the "absent" case, making
 * the possibility of a missing value part of the API contract rather than a
 * silent runtime surprise.
 *
 * PROBLEM SOLVED
 * --------------
 * 1. NullPointerException is the most common runtime exception in Java.
 *    Optional makes nullability visible in the type system.
 * 2. Removes defensive null-check boilerplate scattered throughout business logic.
 * 3. Enables fluent, functional chaining (map / flatMap / filter) on potentially
 *    absent values.
 *
 * BEST PRACTICES
 * --------------
 *  ✔ Use as a return type when a value may legitimately be absent.
 *  ✔ Use map/flatMap/filter for transformations.
 *  ✔ Use orElse/orElseGet/orElseThrow for defaults.
 *  ✘ Do NOT use as a field type or method parameter (serialisation issues).
 *  ✘ Do NOT call get() without isPresent() – defeats the purpose.
 */
public class OptionalExamples {

    // =========================================================================
    // Domain model
    // =========================================================================

    public record Address(String street, String city) {}
    public record Counterparty(String id, String name, Address address) {}
    public record Trade(String id, Counterparty counterparty) {}

    // =========================================================================
    // BEFORE – Null-check hell
    // =========================================================================

    /**
     * Get the city of the counterparty of a trade.
     * Fails with NPE if any link in the chain is null.
     */
    public String getTradeCity_Before(Trade trade) {
        if (trade != null) {
            Counterparty cp = trade.counterparty();
            if (cp != null) {
                Address addr = cp.address();
                if (addr != null) {
                    return addr.city();
                }
            }
        }
        return "UNKNOWN";
    }

    /**
     * Find a counterparty by ID from an in-memory store – imperative null check.
     */
    public String findCounterpartyName_Before(String id) {
        Counterparty cp = findById(id);   // could return null
        if (cp != null) {
            return cp.name();
        }
        return "NOT_FOUND";
    }

    // =========================================================================
    // AFTER – Optional
    // =========================================================================

    /**
     * Same city lookup using Optional chaining.
     * Each step is either mapped over or short-circuits to "UNKNOWN".
     */
    public String getTradeCity_After(Trade trade) {
        return Optional.ofNullable(trade)
                .map(Trade::counterparty)
                .map(Counterparty::address)
                .map(Address::city)
                .orElse("UNKNOWN");
    }

    /**
     * Find counterparty name – repository method now returns Optional<Counterparty>.
     */
    public String findCounterpartyName_After(String id) {
        return findByIdOptional(id)
                .map(Counterparty::name)
                .orElse("NOT_FOUND");
    }

    // =========================================================================
    // Key Optional methods demonstrated
    // =========================================================================

    /** orElseGet – evaluate a Supplier only when absent (cheaper than orElse). */
    public String resolveSymbol(String rawSymbol) {
        return Optional.ofNullable(rawSymbol)
                .filter(s -> !s.isBlank())
                .orElseGet(() -> generateDefaultSymbol());
    }

    /** orElseThrow – throw a domain exception when the value MUST be present. */
    public Counterparty requireCounterparty(String id) {
        return findByIdOptional(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Counterparty not found: " + id));
    }

    /** ifPresent – execute a Consumer only when value is present (no if-check needed). */
    public void logIfPresent(Optional<Trade> trade) {
        trade.ifPresent(t -> System.out.println("Trade found: " + t.id()));
    }

    /**
     * ifPresentOrElse (Java 9+) – cleaner than isPresent() + get() + else branch.
     */
    public void processTradeOrWarn(Optional<Trade> trade) {
        trade.ifPresentOrElse(
                t  -> System.out.println("Processing: " + t.id()),
                () -> System.out.println("No trade to process")
        );
    }

    /**
     * flatMap – when the mapping itself returns an Optional, avoids Optional<Optional<T>>.
     */
    public Optional<String> getCounterpartyCity(Optional<Trade> trade) {
        return trade
                .flatMap(t -> Optional.ofNullable(t.counterparty()))
                .flatMap(cp -> Optional.ofNullable(cp.address()))
                .map(Address::city);
    }

    /**
     * or (Java 9+) – fallback to another Optional when empty.
     */
    public Optional<Counterparty> resolveCounterparty(String primaryId, String fallbackId) {
        return findByIdOptional(primaryId)
                .or(() -> findByIdOptional(fallbackId));
    }

    // =========================================================================
    // Simulated repository methods
    // =========================================================================

    private Counterparty findById(String id) {
        if ("CP001".equals(id)) {
            return new Counterparty("CP001", "Acme Corp",
                    new Address("123 Wall St", "New York"));
        }
        return null;
    }

    private Optional<Counterparty> findByIdOptional(String id) {
        if ("CP001".equals(id)) {
            return Optional.of(new Counterparty("CP001", "Acme Corp",
                    new Address("123 Wall St", "New York")));
        }
        return Optional.empty();
    }

    private String generateDefaultSymbol() {
        return "DEFAULT_SYM";
    }

    // demo main
    public static void main(String[] args) {
        OptionalExamples ex = new OptionalExamples();

        Trade tradeWithCity = new Trade("T001",
                new Counterparty("CP001", "Acme", new Address("123 Wall St", "New York")));
        Trade tradeNullCp = new Trade("T002", null);

        System.out.println("City (full chain) : " + ex.getTradeCity_After(tradeWithCity));
        System.out.println("City (null cp)    : " + ex.getTradeCity_After(tradeNullCp));
        System.out.println("City (null trade) : " + ex.getTradeCity_After(null));

        System.out.println("CP name found     : " + ex.findCounterpartyName_After("CP001"));
        System.out.println("CP name missing   : " + ex.findCounterpartyName_After("UNKNOWN"));

        System.out.println("Symbol            : " + ex.resolveSymbol(null));
        System.out.println("Symbol non-blank  : " + ex.resolveSymbol("AAPL"));
    }
}
