package com.training.java817.module1.lambdas;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * =============================================================================
 * MODULE 1 – LAMBDAS & FUNCTIONAL INTERFACES (Java 8)
 * =============================================================================
 *
 * THEORY
 * ------
 * Before Java 8 the only way to pass behaviour as an argument was to wrap it
 * inside an anonymous inner class.  That pattern is verbose, noisy, and hides
 * the intent of the code.
 *
 * Java 8 introduced:
 *  - Lambda expressions  : concise inline implementations of @FunctionalInterface
 *  - Functional interfaces: any interface with exactly ONE abstract method
 *    (java.util.function.*  ships dozens of ready-made ones)
 *  - Method references   : even more compact syntax when the lambda only delegates
 *
 * PROBLEM SOLVED
 * --------------
 * 1. Removes ~90 % of anonymous-inner-class boilerplate.
 * 2. Allows passing behaviour (strategy pattern) without extra classes.
 * 3. Enables the Stream API, CompletableFuture, and the whole functional stack.
 *
 * KEY BUILT-IN FUNCTIONAL INTERFACES
 * -----------------------------------
 *  Predicate<T>        T -> boolean          – filtering / testing
 *  Function<T,R>       T -> R                – mapping / transforming
 *  Consumer<T>         T -> void             – side effects (print, save…)
 *  Supplier<T>         () -> T               – lazy value production
 *  BiFunction<T,U,R>   (T,U) -> R            – two inputs, one output
 *  Runnable            () -> void            – background tasks
 *  Comparator<T>       (T,T) -> int          – sorting
 */
public class LambdaExamples {

    // =========================================================================
    // BEFORE Java 8 – Anonymous inner classes
    // =========================================================================

    /** Sort a list using an anonymous Comparator (Java 7 style). */
    public List<String> sortTrades_Before(List<String> tradeIds) {
        List<String> mutable = new java.util.ArrayList<>(tradeIds);
        mutable.sort(new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return a.compareTo(b);
            }
        });
        return mutable;
    }

    /** Filter a list using a hand-written loop + anonymous Predicate (Java 7 style). */
    public List<String> filterActiveTrades_Before(List<String> statuses) {
        List<String> result = new java.util.ArrayList<>();
        for (String s : statuses) {
            if (s.startsWith("ACTIVE")) {
                result.add(s);
            }
        }
        return result;
    }

    // =========================================================================
    // AFTER Java 8 – Lambda expressions
    // =========================================================================

    /** Sort a list using a lambda Comparator. */
    public List<String> sortTrades_After(List<String> tradeIds) {
        List<String> mutable = new java.util.ArrayList<>(tradeIds);
        mutable.sort((a, b) -> a.compareTo(b));
        // Even cleaner with a method reference:
        // mutable.sort(String::compareTo);
        return mutable;
    }

    /** Filter using a Predicate lambda + Stream (shown fully in StreamExamples). */
    public List<String> filterActiveTrades_After(List<String> statuses) {
        return statuses.stream()
                .filter(s -> s.startsWith("ACTIVE"))
                .collect(java.util.stream.Collectors.toList());
    }

    // =========================================================================
    // Built-in functional interface showcase
    // =========================================================================

    /** Predicate – tests a condition. */
    public boolean isHighValue(double amount) {
        Predicate<Double> highValue = value -> value > 1_000_000.0;
        return highValue.test(amount);
    }

    /** Function – transforms one type to another. */
    public String formatTradeId(int rawId) {
        Function<Integer, String> formatter = id -> "TRD-" + String.format("%06d", id);
        return formatter.apply(rawId);
    }

    /** Consumer – acts on a value, returns nothing. */
    public void printTrade(String tradeId) {
        Consumer<String> printer = id -> System.out.println("Processing trade: " + id);
        printer.accept(tradeId);
    }

    /** Supplier – provides a value lazily. */
    public String getDefaultCounterparty() {
        Supplier<String> defaultCp = () -> "UNKNOWN_CP";
        return defaultCp.get();
    }

    /** BiFunction – takes two arguments. */
    public String buildTradeKey(String symbol, int version) {
        BiFunction<String, Integer, String> keyBuilder =
                (sym, ver) -> sym + "_v" + ver;
        return keyBuilder.apply(symbol, version);
    }

    /** Method reference – delegates directly to an existing method. */
    public List<String> upperCaseSymbols(List<String> symbols) {
        return symbols.stream()
                .map(String::toUpperCase)   // equivalent to: s -> s.toUpperCase()
                .collect(java.util.stream.Collectors.toList());
    }

    // =========================================================================
    // Composing functions
    // =========================================================================

    /**
     * Function composition with andThen / compose.
     * Problem context: sanitise then format a trade symbol.
     */
    public String sanitizeAndFormat(String raw) {
        Function<String, String> trim      = String::trim;
        Function<String, String> upperCase = String::toUpperCase;
        Function<String, String> pipeline  = trim.andThen(upperCase);
        return pipeline.apply(raw);
    }

    /**
     * Predicate composition with and / or / negate.
     * Problem context: find trades that are active AND high-value.
     */
    public Predicate<String> activeAndLongSymbol() {
        Predicate<String> isActive = s -> s.startsWith("ACTIVE");
        Predicate<String> isLong   = s -> s.length() > 10;
        return isActive.and(isLong);
    }

    // =========================================================================
    // Effectively final variables in lambdas
    // =========================================================================

    /**
     * A lambda can capture local variables from the enclosing scope as long as
     * they are effectively final (never reassigned after initial assignment).
     */
    public List<String> filterByPrefix(List<String> items, String prefix) {
        // 'prefix' is effectively final – captured by the lambda
        return items.stream()
                .filter(item -> item.startsWith(prefix))
                .collect(java.util.stream.Collectors.toList());
    }

    // demo main
    public static void main(String[] args) {
        LambdaExamples ex = new LambdaExamples();

        List<String> ids = Arrays.asList("TRD003", "TRD001", "TRD002");
        System.out.println("Before sort : " + ids);
        System.out.println("After sort  : " + ex.sortTrades_After(ids));

        System.out.println("Format      : " + ex.formatTradeId(42));
        System.out.println("Key         : " + ex.buildTradeKey("AAPL", 3));
        System.out.println("Sanitize    : " + ex.sanitizeAndFormat("  aapl  "));
    }
}
