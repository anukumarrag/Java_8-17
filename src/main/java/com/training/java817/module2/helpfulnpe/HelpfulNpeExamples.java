package com.training.java817.module2.helpfulnpe;

/**
 * =============================================================================
 * MODULE 2 – HELPFUL NullPointerExceptions (JEP 358, Java 14 GA)
 * =============================================================================
 *
 * THEORY
 * ------
 * Before Java 14, a NullPointerException message only said:
 *   "Cannot invoke ... because ... is null"
 * But when you had a chained expression like:
 *   order.getCustomer().getAddress().getCity()
 * the stack trace told you the LINE, but not WHICH reference was null.
 * You had to add logging, set breakpoints, or manually split the chain.
 *
 * Java 14 (enabled by default from Java 15+) enhances the JVM to compute a
 * precise, human-readable description of WHICH variable was null and WHAT
 * operation was being attempted.
 *
 * BEFORE (Java < 14):
 *   NullPointerException (no message)
 *
 * AFTER (Java 14+):
 *   Cannot invoke "com.example.Address.getCity()" because the return value
 *   of "com.example.Customer.getAddress()" is null
 *
 * PROBLEM SOLVED
 * --------------
 * 1. Dramatically reduces debugging time for chained method calls.
 * 2. No code changes required – just JVM improvement.
 * 3. Removes guesswork in production log analysis.
 * 4. Especially valuable for complex DTO graphs from ORMs (JPA entities).
 *
 * HOW TO ENABLE (if not already default)
 * ---------------------------------------
 *   JVM flag: -XX:+ShowCodeDetailsInExceptionMessages
 *   (default ON from Java 15 onwards)
 *
 * NOTE
 * ----
 * This class intentionally triggers NPEs to demonstrate the messages.
 * In real code, use Optional or null-safe methods to avoid them.
 */
public class HelpfulNpeExamples {

    // =========================================================================
    // Domain model (representing a typical ORM entity graph)
    // =========================================================================

    public static class Order {
        private Customer customer;
        private String orderId;

        public Order(String orderId, Customer customer) {
            this.orderId  = orderId;
            this.customer = customer;
        }
        public Customer getCustomer() { return customer; }
        public String   getOrderId()  { return orderId; }
    }

    public static class Customer {
        private String name;
        private Address address;
        private ContactInfo contactInfo;

        public Customer(String name, Address address) {
            this.name    = name;
            this.address = address;
        }
        public String      getName()        { return name; }
        public Address     getAddress()     { return address; }
        public ContactInfo getContactInfo() { return contactInfo; }
    }

    public static class Address {
        private String street;
        private String city;
        private String postalCode;

        public Address(String street, String city, String postalCode) {
            this.street     = street;
            this.city       = city;
            this.postalCode = postalCode;
        }
        public String getStreet()     { return street; }
        public String getCity()       { return city; }
        public String getPostalCode() { return postalCode; }
    }

    public static class ContactInfo {
        private String email;
        public ContactInfo(String email) { this.email = email; }
        public String getEmail() { return email; }
    }

    // =========================================================================
    // BEFORE Java 14: opaque NPE – you know the line, but not WHICH null
    // =========================================================================

    /**
     * If customer.getAddress() returns null, the old JVM just said:
     *   NullPointerException
     * No message, no clue whether 'customer' or 'address' was null.
     */
    public String getCity_Before(Order order) {
        // Pre-Java 14 style: bare call chain, NPE message was useless
        return order.getCustomer().getAddress().getCity();
    }

    // =========================================================================
    // AFTER Java 14: precise NPE
    // =========================================================================

    /**
     * Exact same code – but Java 14+ produces:
     *
     *   Cannot invoke "com.training.java817.module2.helpfulnpe.HelpfulNpeExamples$Address.getCity()"
     *   because the return value of
     *   "com.training.java817.module2.helpfulnpe.HelpfulNpeExamples$Customer.getAddress()"
     *   is null
     *
     * You now immediately know that getAddress() returned null.
     */
    public String getCity_After(Order order) {
        return order.getCustomer().getAddress().getCity();
    }

    // =========================================================================
    // Scenarios that produce informative messages
    // =========================================================================

    /** Null local variable. */
    public void nullLocalVariable() {
        String value = null;
        value.length();       // "Cannot invoke ... because 'value' is null"
    }

    /** Null array element. */
    public void nullArrayElement() {
        String[] arr = new String[3];   // all elements null
        arr[1].length();                // "Cannot load from ... because 'arr[1]' is null"
    }

    /** Null in a chained read expression. */
    public String nullInChain(Order order) {
        return order.getCustomer().getContactInfo().getEmail();
        // "... because the return value of Customer.getContactInfo() is null"
    }

    // =========================================================================
    // Best practice: prevent NPEs with Optional rather than depending on the
    // improved message for production resilience.
    // =========================================================================

    public java.util.Optional<String> getCitySafe(Order order) {
        return java.util.Optional.ofNullable(order)
                .map(Order::getCustomer)
                .map(Customer::getAddress)
                .map(Address::getCity);
    }

    // demo main – catches and prints NPE messages to show the improvement
    public static void main(String[] args) {
        HelpfulNpeExamples ex = new HelpfulNpeExamples();

        // Scenario 1: getAddress() returns null
        Order order1 = new Order("O001", new Customer("Alice", null));
        System.out.println("=== Scenario 1: address is null ===");
        try {
            ex.getCity_After(order1);
        } catch (NullPointerException npe) {
            System.out.println("NPE message: " + npe.getMessage());
        }

        // Scenario 2: customer itself is null
        Order order2 = new Order("O002", null);
        System.out.println("=== Scenario 2: customer is null ===");
        try {
            ex.getCity_After(order2);
        } catch (NullPointerException npe) {
            System.out.println("NPE message: " + npe.getMessage());
        }

        // Scenario 3: contactInfo is null
        Order order3 = new Order("O003",
                new Customer("Bob", new Address("1 Main St", "NYC", "10001")));
        System.out.println("=== Scenario 3: contactInfo is null ===");
        try {
            ex.nullInChain(order3);
        } catch (NullPointerException npe) {
            System.out.println("NPE message: " + npe.getMessage());
        }

        // Safe alternative
        System.out.println("=== Safe Optional version ===");
        System.out.println(ex.getCitySafe(order1));  // Optional.empty
        Order order4 = new Order("O004",
                new Customer("Carol", new Address("2 Wall St", "NYC", "10005")));
        System.out.println(ex.getCitySafe(order4));  // Optional[NYC]
    }
}
