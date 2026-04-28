package com.training.java817.module3.after;

/** Fired when a new trade is submitted to the system. */
public record TradeCreatedEvent(
        String tradeId,
        String symbol,
        double notional,
        String counterpartyId
) implements TradeEvent {}
