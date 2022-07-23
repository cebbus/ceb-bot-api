package com.cebbus.bot.api.analysis;

import com.cebbus.bot.api.binance.order.TradeStatus;
import com.cebbus.bot.api.dto.CsIntervalAdapter;
import com.cebbus.bot.api.properties.Symbol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SymbolTest {

    private Symbol symbol;

    @BeforeEach
    void setUp() {
        this.symbol = Symbol.builder()
                .id(0)
                .weight(0d)
                .base("BASE")
                .quote("QUOTE")
                .strategy("Junk")
                .interval(CsIntervalAdapter.ONE_MINUTE)
                .status(TradeStatus.INACTIVE)
                .cacheSize(5)
                .build();
    }

    @Test
    void getId() {
        assertEquals(0, this.symbol.getId());
    }

    @Test
    void getWeight() {
        assertEquals(0d, this.symbol.getWeight());
    }

    @Test
    void getBase() {
        assertEquals("BASE", this.symbol.getBase());
    }

    @Test
    void getQuote() {
        assertEquals("QUOTE", this.symbol.getQuote());
    }

    @Test
    void getStrategy() {
        assertEquals("Junk", this.symbol.getStrategy());
    }

    @Test
    void getInterval() {
        assertEquals(CsIntervalAdapter.ONE_MINUTE, this.symbol.getInterval());
    }

    @Test
    void getStatus() {
        assertEquals(TradeStatus.INACTIVE, this.symbol.getStatus());
    }

    @Test
    void getName() {
        assertEquals("BASEQUOTE", this.symbol.getName());
    }

    @Test
    void getCacheSize() {
        assertEquals(5, this.symbol.getCacheSize());
    }
}