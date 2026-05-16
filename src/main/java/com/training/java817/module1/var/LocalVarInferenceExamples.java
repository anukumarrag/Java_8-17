package com.training.java817.module1.var;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * =============================================================================
 * MODULE 1 – LOCAL VARIABLE TYPE INFERENCE (var, Java 10 – JEP 286)
 * =============================================================================
 *
 * THEORY
 * ------
 * Java 10 introduced the `var` keyword for LOCAL VARIABLE TYPE INFERENCE.
 * The compiler infers the type from the initialiser expression – you write less
 * without losing static type safety.
 *
 * IMPORTANT RULES
 * ---------------
 *  • Only applies to LOCAL variables with an initialiser.
 *  • Cannot be used for:
 *      – method parameters
 *      – return types
 *      – fields
 *      – catch variables (until Java 10 this was not allowed)
 *  • The type is still STATIC – the variable is not dynamically typed.
 *  • Cannot be initialised to null (the compiler cannot infer the type).
 *  • Cannot be used without an initialiser.
 *
 * JAVA 11 EXTENSION
 * -----------------
 *  • var is allowed in lambda parameter lists:
 *      (var x, var y) -> x + y
 *    This enables annotations on lambda parameters:
 *      (@NonNull var name) -> name.toUpperCase()
 *
 * WHEN TO USE var
 * ---------------
 *  ✔ When the type is obvious from the right-hand side (constructor call, literal).
 *  ✔ When the type name is long / repetitive (generics, anonymous class types).
 *  ✔ In short, readable for-each loops.
 *  ✘ When the type is NOT obvious – var can reduce readability.
 *  ✘ In public API signatures (parameters, return types – not supported anyway).
 *
 * PROBLEM SOLVED
 * --------------
 * 1. Reduces redundant type declarations (the type appears twice in verbose code).
 * 2. Improves readability for complex generic types.
 * 3. Makes refactoring easier – change the type on the right, var adapts.
 */
public class LocalVarInferenceExamples {

    public record Employee(String id, String name, String department, double salary, String status) {}

    // =========================================================================
    // BEFORE – Verbose explicit types (Java 9 style)
    // =========================================================================

    public Map<String, List<Employee>> groupByDepartment_Before(List<Employee> employees) {
        Map<String, List<Employee>> result = new HashMap<>();
        for (Employee employee : employees) {
            List<Employee> group = result.computeIfAbsent(employee.department(), k -> new ArrayList<>());
            group.add(employee);
        }
        return result;
    }

    // =========================================================================
    // AFTER – var (Java 10)
    // =========================================================================

    public Map<String, List<Employee>> groupByDepartment_After(List<Employee> employees) {
        var result = new HashMap<String, List<Employee>>();    // type inferred: HashMap<String, List<Employee>>
        for (var employee : employees) {                       // type inferred: Employee
            var group = result.computeIfAbsent(employee.department(), k -> new ArrayList<>());
            group.add(employee);
        }
        return result;
    }

    // =========================================================================
    // var with primitives and literals
    // =========================================================================

    public double calculateTotalSalary(int headcount, double avgSalary) {
        var total    = headcount * avgSalary;   // inferred: double
        var rounded  = Math.round(total);       // inferred: long
        var location = "BANGALORE";             // inferred: String
        System.out.println(location + " " + rounded);
        return total;
    }

    // =========================================================================
    // var reduces noise with long generic types
    // =========================================================================

    public Map<String, Map<String, List<String>>> buildNestedMap() {
        // Before: Map<String, Map<String, List<String>>> outer = new HashMap<>();
        var outer = new HashMap<String, Map<String, List<String>>>();
        var inner = new HashMap<String, List<String>>();
        inner.put("departments", new ArrayList<>(List.of("ENGINEERING", "MARKETING")));
        outer.put("europe", inner);
        return outer;
    }

    // =========================================================================
    // var in try-with-resources
    // =========================================================================

    public List<String> readLines(String content) throws Exception {
        var lines = new ArrayList<String>();
        try (var reader = new BufferedReader(new StringReader(content))) {  // var for AutoCloseable
            var line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        }
        return lines;
    }

    // =========================================================================
    // var in for-each (most common use case)
    // =========================================================================

    public double sumHighSalaryEmployees(List<Employee> employees) {
        double sum = 0;
        for (var e : employees) {                             // concise loop variable
            if (e.salary() > 100_000) {
                sum += e.salary();
            }
        }
        return sum;
    }

    // =========================================================================
    // Java 11 – var in lambda parameters (enables annotations)
    // =========================================================================

    public List<String> processNames(List<String> names) {
        // var in lambda parameters allows attaching annotations like @NonNull
        // (annotation support shown conceptually – requires annotation library)
        return names.stream()
                .filter((var s) -> s != null && !s.isBlank())
                .map((var s)    -> s.trim().toUpperCase())
                .toList();
    }

    // =========================================================================
    // What var CANNOT do – documented for clarity
    // =========================================================================

    /** This method CANNOT use var for its parameter (compile error if tried). */
    public String formatEmployee(String employeeId, double salary) {
        // var employeeId – NOT allowed: method parameter
        // var salary     – NOT allowed: method parameter
        var formatted = "EMP[" + employeeId + "]=" + salary;   // OK: local var
        return formatted;
    }

    // demo main
    public static void main(String[] args) throws Exception {
        LocalVarInferenceExamples ex = new LocalVarInferenceExamples();

        var employees = List.of(
                new Employee("E1", "Alice",   "ENGINEERING", 50_000,  "ACTIVE"),
                new Employee("E2", "Bob",     "ENGINEERING", 150_000, "ACTIVE"),
                new Employee("E3", "Charlie", "MARKETING",   80_000,  "ONBOARDING")
        );

        var grouped = ex.groupByDepartment_After(employees);
        System.out.println("Grouped keys  : " + grouped.keySet());

        var sum = ex.sumHighSalaryEmployees(employees);
        System.out.println("High-salary sum: " + sum);

        var lines = ex.readLines("line1\nline2\nline3");
        System.out.println("Lines read    : " + lines);

        var names = ex.processNames(List.of("alice", " bob ", null, ""));
        System.out.println("Names         : " + names);
    }
}
