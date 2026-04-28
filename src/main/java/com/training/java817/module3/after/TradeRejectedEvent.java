package com.training.java817.module3.after;

/** Fired when a trade is rejected by the risk or compliance system. */
public record TradeRejectedEvent(
        String tradeId,
        String rejectionCode,
        String rejectionReason
) implements TradeEvent {}
