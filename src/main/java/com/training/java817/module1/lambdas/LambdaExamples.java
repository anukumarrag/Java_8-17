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
    public List<String> sortEmployees_Before(List<String> employeeIds) {
        List<String> mutable = new java.util.ArrayList<>(employeeIds);
        mutable.sort(new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return a.compareTo(b);
            }
        });
        return mutable;
    }

    /** Filter a list using a hand-written loop + anonymous Predicate (Java 7 style). */
    public List<String> filterActiveEmployees_Before(List<String> statuses) {
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
    public List<String> sortEmployees_After(List<String> employeeIds) {
        List<String> mutable = new java.util.ArrayList<>(employeeIds);
        mutable.sort((a, b) -> a.compareTo(b));
        // Even cleaner with a method reference:
        // mutable.sort(String::compareTo);
        return mutable;
    }

    /** Filter using a Predicate lambda + Stream (shown fully in StreamExamples). */
    public List<String> filterActiveEmployees_After(List<String> statuses) {
        return statuses.stream()
                .filter(s -> s.startsWith("ACTIVE"))
                .collect(java.util.stream.Collectors.toList());
    }

    // =========================================================================
    // Built-in functional interface showcase
    // =========================================================================

    /** Predicate – tests a condition. */
    public boolean isHighSalary(double amount) {
        Predicate<Double> highSalary = value -> value > 100_000.0;
        return highSalary.test(amount);
    }

    /** Function – transforms one type to another. */
    public String formatEmployeeId(int rawId) {
        Function<Integer, String> formatter = id -> "EMP-" + String.format("%06d", id);
        return formatter.apply(rawId);
    }

    /** Consumer – acts on a value, returns nothing. */
    public void printEmployee(String employeeId) {
        Consumer<String> printer = id -> System.out.println("Processing employee: " + id);
        printer.accept(employeeId);
    }

    /** Supplier – provides a value lazily. */
    public String getDefaultDepartment() {
        Supplier<String> defaultDept = () -> "UNKNOWN_DEPT";
        return defaultDept.get();
    }

    /** BiFunction – takes two arguments. */
    public String buildEmployeeKey(String name, int version) {
        BiFunction<String, Integer, String> keyBuilder =
                (n, ver) -> n + "_v" + ver;
        return keyBuilder.apply(name, version);
    }

    /** Method reference – delegates directly to an existing method. */
    public List<String> upperCaseNames(List<String> names) {
        return names.stream()
                .map(String::toUpperCase)   // equivalent to: s -> s.toUpperCase()
                .collect(java.util.stream.Collectors.toList());
    }

    // =========================================================================
    // Composing functions
    // =========================================================================

    /**
     * Function composition with andThen / compose.
     * Problem context: sanitise then format an employee name.
     */
    public String sanitizeAndFormat(String raw) {
        Function<String, String> trim      = String::trim;
        Function<String, String> upperCase = String::toUpperCase;
        Function<String, String> pipeline  = trim.andThen(upperCase);
        return pipeline.apply(raw);
    }

    /**
     * Predicate composition with and / or / negate.
     * Problem context: find employees that are active AND have a long name.
     */
    public Predicate<String> activeAndLongName() {
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

        List<String> ids = Arrays.asList("EMP-003", "EMP-001", "EMP-002");
        System.out.println("Before sort : " + ids);
        System.out.println("After sort  : " + ex.sortEmployees_After(ids));

        System.out.println("Format      : " + ex.formatEmployeeId(42));
        System.out.println("Key         : " + ex.buildEmployeeKey("Alice", 3));
        System.out.println("Sanitize    : " + ex.sanitizeAndFormat("  alice  "));
    }
}
