package com.cebbus.bot.api.properties;

import com.cebbus.bot.api.binance.order.TradeStatus;
import com.cebbus.bot.api.dto.CsIntervalAdapter;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Symbol {
    private final int id;
    private final double weight;
    private final String base;
    private final String quote;
    private final String strategy;
    private final CsIntervalAdapter interval;
    private final TradeStatus status;
    private final Integer cacheSize;

    public String getName() {
        return this.base + this.quote;
    }

    public Symbol changeStrategy(String newStrategy) {
        return new Symbol(
                this.id,
                this.weight,
                this.base,
                this.quote,
                newStrategy,
                this.interval,
                this.status,
                this.cacheSize
        );
    }

    public Symbol changeLimit(Integer limit) {
        return new Symbol(
                this.id,
                this.weight,
                this.base,
                this.quote,
                this.strategy,
                this.interval,
                this.status,
                limit
        );
    }
}
