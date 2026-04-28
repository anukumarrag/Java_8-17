package com.training.java817.module3.after;

/** Fired when an existing trade's details are amended. */
public record TradeUpdatedEvent(
        String tradeId,
        double newNotional,
        String updateReason
) implements TradeEvent {}
