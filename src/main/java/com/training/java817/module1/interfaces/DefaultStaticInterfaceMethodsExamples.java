package com.training.java817.module1.interfaces;

import java.util.List;
import java.util.function.Function;

/**
 * =============================================================================
 * MODULE 1 – DEFAULT & STATIC METHODS IN INTERFACES (Java 8 / Java 9)
 * =============================================================================
 *
 * THEORY
 * ------
 * Before Java 8, interfaces could only declare abstract methods and constants.
 * Adding a new method to a published interface was a BREAKING CHANGE – every
 * implementing class had to add the method or fail to compile.
 *
 * Java 8 introduced:
 *   • default methods  – concrete method implementations in an interface.
 *     Implementors inherit the default body unless they override it.
 *   • static methods   – utility methods scoped to the interface (not inherited).
 *
 * Java 9 added:
 *   • private methods  – shared helpers for default methods (no API leakage).
 *   • private static methods – same, for static context.
 *
 * PROBLEM SOLVED
 * --------------
 * 1. Add new behaviour to existing interfaces without breaking backward
 *    compatibility (all existing implementations inherit the default).
 * 2. Replaces companion utility classes (e.g. Collections class for Collection).
 * 3. Enables richer interface contracts without single-inheritance constraint.
 * 4. Facilitates multiple-interface composition (mixin-like patterns).
 *
 * DIAMOND PROBLEM
 * ---------------
 * If a class implements two interfaces that both provide a default method with
 * the same signature, the class MUST override it to resolve the ambiguity.
 */
public class DefaultStaticInterfaceMethodsExamples {

    // =========================================================================
    // BEFORE – Companion utility class pattern (Java 7 style)
    // =========================================================================

    /** Old-style interface – no default or static methods allowed. */
    public interface TradeValidator_Before {
        boolean validate(String tradeId);
    }

    /**
     * Companion utility class: common logic that cannot live in the interface.
     * Forces callers to depend on a separate class, cluttering the API surface.
     */
    public static class TradeValidatorUtils {
        public static boolean isValidFormat(String tradeId) {
            return tradeId != null && tradeId.matches("TRD-\\d{6}");
        }
    }

    // =========================================================================
    // AFTER – Default & Static methods (Java 8) + Private methods (Java 9)
    // =========================================================================

    /**
     * Payment processor interface demonstrating all interface method types.
     */
    public interface PaymentProcessor {

        // --- Abstract method: implementors MUST provide this ---
        String process(double amount, String currency);

        // --- Default method: concrete behaviour, can be overridden ---
        default String processWithFee(double amount, String currency) {
            double fee   = calculateFee(amount);   // calls private helper
            double total = amount + fee;
            return process(total, currency) + " [fee=" + String.format("%.2f", fee) + "]";
        }

        // --- Another default – validates before processing ---
        default String safeProcess(double amount, String currency) {
            if (amount <= 0) return "REJECTED: non-positive amount";
            if (currency == null || currency.isBlank()) return "REJECTED: missing currency";
            return process(amount, currency);
        }

        // --- Static factory / utility method – not inherited by implementing classes ---
        static PaymentProcessor noOp() {
            return (amount, currency) -> "NOOP: " + amount + " " + currency;
        }

        static boolean isSupportedCurrency(String currency) {
            return List.of("USD", "EUR", "GBP", "JPY").contains(currency);
        }

        // --- Private helper (Java 9) – shared logic, not part of the public API ---
        private double calculateFee(double amount) {
            return amount * 0.0025;   // 25 basis points
        }
    }

    // =========================================================================
    // Implementations: override none, some, or all default methods
    // =========================================================================

    /** Standard FX payment – inherits all defaults. */
    public static class FxPaymentProcessor implements PaymentProcessor {
        @Override
        public String process(double amount, String currency) {
            return "FX_PROCESSED: " + amount + " " + currency;
        }
    }

    /** Institutional processor – overrides processWithFee with a lower rate. */
    public static class InstitutionalPaymentProcessor implements PaymentProcessor {
        @Override
        public String process(double amount, String currency) {
            return "INST_PROCESSED: " + amount + " " + currency;
        }

        /** Override default: institutional clients get lower fee (10 bps). */
        @Override
        public String processWithFee(double amount, String currency) {
            double fee   = amount * 0.001;
            double total = amount + fee;
            return process(total, currency) + " [inst-fee=" + String.format("%.2f", fee) + "]";
        }
    }

    // =========================================================================
    // Diamond problem resolution
    // =========================================================================

    public interface Auditable {
        default String auditLabel() { return "AUDITABLE"; }
    }

    public interface Loggable {
        default String auditLabel() { return "LOGGABLE"; }
    }

    /**
     * AuditedProcessor implements BOTH Auditable and Loggable, which share the
     * same default method name – must override to resolve the ambiguity.
     */
    public static class AuditedProcessor implements Auditable, Loggable, PaymentProcessor {
        @Override
        public String process(double amount, String currency) {
            return "AUDITED: " + amount + " " + currency;
        }

        /** Explicitly resolve diamond conflict by calling both via InterfaceName.super. */
        @Override
        public String auditLabel() {
            return Auditable.super.auditLabel() + "+" + Loggable.super.auditLabel();
        }
    }

    // =========================================================================
    // Functional interface with default composition method
    // =========================================================================

    /**
     * Transformer<T> – single-abstract-method interface with default andThen.
     * Demonstrates why default methods power the entire java.util.function package.
     */
    @FunctionalInterface
    public interface Transformer<T> {
        T transform(T input);

        /** Default compose: run this transformer, then apply next. */
        default Transformer<T> andThen(Transformer<T> next) {
            return input -> next.transform(this.transform(input));
        }

        /** Static factory: wrap a java.util.function.Function as a Transformer. */
        static <T> Transformer<T> of(Function<T, T> fn) {
            return fn::apply;
        }
    }

    // =========================================================================
    // Demonstration methods (called from tests)
    // =========================================================================

    public String processPayment(double amount, String currency) {
        PaymentProcessor standard      = new FxPaymentProcessor();
        PaymentProcessor institutional = new InstitutionalPaymentProcessor();
        return standard.processWithFee(amount, currency)
                + " | " + institutional.processWithFee(amount, currency);
    }

    public String safeProcessPayment(double amount, String currency) {
        PaymentProcessor proc = new FxPaymentProcessor();
        return proc.safeProcess(amount, currency);
    }

    public boolean currencySupported(String currency) {
        return PaymentProcessor.isSupportedCurrency(currency);
    }

    public String applyTransformerPipeline(String input) {
        Transformer<String> trim      = String::trim;
        Transformer<String> upper     = String::toUpperCase;
        Transformer<String> addPrefix = s -> "TRD-" + s;
        Transformer<String> pipeline  = trim.andThen(upper).andThen(addPrefix);
        return pipeline.transform(input);
    }

    public String resolveAuditLabel() {
        return new AuditedProcessor().auditLabel();
    }

    // demo main
    public static void main(String[] args) {
        DefaultStaticInterfaceMethodsExamples ex = new DefaultStaticInterfaceMethodsExamples();

        System.out.println("Payment       : " + ex.processPayment(10_000, "USD"));
        System.out.println("Safe (bad)    : " + ex.safeProcessPayment(-100, "USD"));
        System.out.println("Safe (ok)     : " + ex.safeProcessPayment(5_000, "EUR"));
        System.out.println("USD supported : " + ex.currencySupported("USD"));
        System.out.println("CNY supported : " + ex.currencySupported("CNY"));
        System.out.println("Pipeline      : " + ex.applyTransformerPipeline("  aapl  "));
        System.out.println("Audit label   : " + ex.resolveAuditLabel());

        PaymentProcessor noop = PaymentProcessor.noOp();
        System.out.println("NoOp          : " + noop.process(999, "USD"));
    }
}
