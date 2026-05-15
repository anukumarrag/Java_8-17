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
    public record Department(String id, String name, Address address) {}
    public record Employee(String id, Department department) {}

    // =========================================================================
    // BEFORE – Null-check hell
    // =========================================================================

    /**
     * Get the city of the department of an employee.
     * Fails with NPE if any link in the chain is null.
     */
    public String getEmployeeCity_Before(Employee employee) {
        if (employee != null) {
            Department dept = employee.department();
            if (dept != null) {
                Address addr = dept.address();
                if (addr != null) {
                    return addr.city();
                }
            }
        }
        return "UNKNOWN";
    }

    /**
     * Find a department by ID from an in-memory store – imperative null check.
     */
    public String findDepartmentName_Before(String id) {
        Department dept = findById(id);   // could return null
        if (dept != null) {
            return dept.name();
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
    public String getEmployeeCity_After(Employee employee) {
        return Optional.ofNullable(employee)
                .map(Employee::department)
                .map(Department::address)
                .map(Address::city)
                .orElse("UNKNOWN");
    }

    /**
     * Find department name – repository method now returns Optional<Department>.
     */
    public String findDepartmentName_After(String id) {
        return findByIdOptional(id)
                .map(Department::name)
                .orElse("NOT_FOUND");
    }

    // =========================================================================
    // Key Optional methods demonstrated
    // =========================================================================

    /** orElseGet – evaluate a Supplier only when absent (cheaper than orElse). */
    public String resolveName(String rawName) {
        return Optional.ofNullable(rawName)
                .filter(s -> !s.isBlank())
                .orElseGet(() -> generateDefaultName());
    }

    /** orElseThrow – throw a domain exception when the value MUST be present. */
    public Department requireDepartment(String id) {
        return findByIdOptional(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Department not found: " + id));
    }

    /** ifPresent – execute a Consumer only when value is present (no if-check needed). */
    public void logIfPresent(Optional<Employee> employee) {
        employee.ifPresent(e -> System.out.println("Employee found: " + e.id()));
    }

    /**
     * ifPresentOrElse (Java 9+) – cleaner than isPresent() + get() + else branch.
     */
    public void processEmployeeOrWarn(Optional<Employee> employee) {
        employee.ifPresentOrElse(
                e  -> System.out.println("Processing: " + e.id()),
                () -> System.out.println("No employee to process")
        );
    }

    /**
     * flatMap – when the mapping itself returns an Optional, avoids Optional<Optional<T>>.
     */
    public Optional<String> getDepartmentCity(Optional<Employee> employee) {
        return employee
                .flatMap(e -> Optional.ofNullable(e.department()))
                .flatMap(dept -> Optional.ofNullable(dept.address()))
                .map(Address::city);
    }

    /**
     * or (Java 9+) – fallback to another Optional when empty.
     */
    public Optional<Department> resolveDepartment(String primaryId, String fallbackId) {
        return findByIdOptional(primaryId)
                .or(() -> findByIdOptional(fallbackId));
    }

    // =========================================================================
    // Simulated repository methods
    // =========================================================================

    private Department findById(String id) {
        if ("D001".equals(id)) {
            return new Department("D001", "Engineering",
                    new Address("10 Tech Park", "Bangalore"));
        }
        return null;
    }

    private Optional<Department> findByIdOptional(String id) {
        if ("D001".equals(id)) {
            return Optional.of(new Department("D001", "Engineering",
                    new Address("10 Tech Park", "Bangalore")));
        }
        return Optional.empty();
    }

    private String generateDefaultName() {
        return "DEFAULT_NAME";
    }

    // demo main
    public static void main(String[] args) {
        OptionalExamples ex = new OptionalExamples();

        Employee employeeWithCity = new Employee("E001",
                new Department("D001", "Engineering", new Address("10 Tech Park", "Bangalore")));
        Employee employeeNullDept = new Employee("E002", null);

        System.out.println("City (full chain) : " + ex.getEmployeeCity_After(employeeWithCity));
        System.out.println("City (null dept)  : " + ex.getEmployeeCity_After(employeeNullDept));
        System.out.println("City (null emp)   : " + ex.getEmployeeCity_After(null));

        System.out.println("Dept name found   : " + ex.findDepartmentName_After("D001"));
        System.out.println("Dept name missing : " + ex.findDepartmentName_After("UNKNOWN"));

        System.out.println("Name              : " + ex.resolveName(null));
        System.out.println("Name non-blank    : " + ex.resolveName("Alice"));
    }
}
