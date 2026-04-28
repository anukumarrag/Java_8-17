package com.training.java817.module3.after;

/** Fired when a trade is executed on a venue. */
public record TradeExecutedEvent(
        String tradeId,
        String executionVenue,
        double executedPrice,
        long   executedQuantity
) implements TradeEvent {}
