package com.training.java817.module3.after;

/**
 * =============================================================================
 * MODULE 3 – WORKSHOP: AFTER STATE
 * Task 3: Sealed TradeEvent interface
 * =============================================================================
 *
 * What changed:
 *  - TradeEventLegacy interface is now `sealed`.
 *  - `permits` lists every allowed implementation – no surprise subclasses.
 *  - Implementations are records: final, immutable, no boilerplate.
 *  - The compiler can now prove exhaustiveness in pattern-matching switch.
 *
 * Benefits:
 *  - Fine-grained control over the domain model.
 *  - Adding a new event type requires updating `permits` – intentional, auditable.
 *  - IDE & compiler warn if you miss a case in a switch.
 */
public sealed interface TradeEvent
        permits TradeCreatedEvent, TradeUpdatedEvent, TradeExecutedEvent, TradeRejectedEvent {

    /** The trade this event relates to. */
    String tradeId();
}
