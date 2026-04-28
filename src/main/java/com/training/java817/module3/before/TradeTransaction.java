package com.training.java817.module3.before;

import java.time.LocalDate;
import java.util.Objects;

/**
 * =============================================================================
 * MODULE 3 – WORKSHOP: BEFORE STATE
 * Task 1: Legacy TradeTransaction POJO (to be refactored into a Record)
 * =============================================================================
 *
 * Problem: This 100-line POJO exists solely to carry data. Every field requires:
 *  - A private final field declaration
 *  - A constructor parameter + assignment
 *  - A getter method
 *  - Manual equals/hashCode/toString
 *
 * Lombok or IDE generation are often used to reduce this – but they add a
 * compile-time annotation processor dependency and hide the logic.
 * Java 17 Records solve this at the language level.
 */
public class TradeTransaction {

    private final String   tradeId;
    private final String   symbol;
    private final double   notional;
    private final String   counterpartyId;
    private final String   traderId;
    private final String   currency;
    private final String   assetClass;
    private final LocalDate tradeDate;
    private final LocalDate settlementDate;
    private final String   status;

    public TradeTransaction(String tradeId,
                            String symbol,
                            double notional,
                            String counterpartyId,
                            String traderId,
                            String currency,
                            String assetClass,
                            LocalDate tradeDate,
                            LocalDate settlementDate,
                            String status) {
        this.tradeId        = Objects.requireNonNull(tradeId,        "tradeId required");
        this.symbol         = Objects.requireNonNull(symbol,         "symbol required");
        this.notional       = notional;
        this.counterpartyId = Objects.requireNonNull(counterpartyId, "counterpartyId required");
        this.traderId       = Objects.requireNonNull(traderId,       "traderId required");
        this.currency       = Objects.requireNonNull(currency,       "currency required");
        this.assetClass     = Objects.requireNonNull(assetClass,     "assetClass required");
        this.tradeDate      = Objects.requireNonNull(tradeDate,      "tradeDate required");
        this.settlementDate = Objects.requireNonNull(settlementDate, "settlementDate required");
        this.status         = Objects.requireNonNull(status,         "status required");
    }

    public String    getTradeId()        { return tradeId; }
    public String    getSymbol()         { return symbol; }
    public double    getNotional()       { return notional; }
    public String    getCounterpartyId() { return counterpartyId; }
    public String    getTraderId()       { return traderId; }
    public String    getCurrency()       { return currency; }
    public String    getAssetClass()     { return assetClass; }
    public LocalDate getTradeDate()      { return tradeDate; }
    public LocalDate getSettlementDate() { return settlementDate; }
    public String    getStatus()         { return status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TradeTransaction)) return false;
        TradeTransaction that = (TradeTransaction) o;
        return Double.compare(that.notional, notional) == 0
                && Objects.equals(tradeId,        that.tradeId)
                && Objects.equals(symbol,         that.symbol)
                && Objects.equals(counterpartyId, that.counterpartyId)
                && Objects.equals(traderId,        that.traderId)
                && Objects.equals(currency,        that.currency)
                && Objects.equals(assetClass,      that.assetClass)
                && Objects.equals(tradeDate,       that.tradeDate)
                && Objects.equals(settlementDate,  that.settlementDate)
                && Objects.equals(status,          that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeId, symbol, notional, counterpartyId, traderId,
                currency, assetClass, tradeDate, settlementDate, status);
    }

    @Override
    public String toString() {
        return "TradeTransaction{" +
                "tradeId='" + tradeId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", notional=" + notional +
                ", counterpartyId='" + counterpartyId + '\'' +
                ", traderId='" + traderId + '\'' +
                ", currency='" + currency + '\'' +
                ", assetClass='" + assetClass + '\'' +
                ", tradeDate=" + tradeDate +
                ", settlementDate=" + settlementDate +
                ", status='" + status + '\'' +
                '}';
    }
}
