package com.cebbus.bot.api.binance;

import com.cebbus.bot.api.Speculator;
import com.cebbus.bot.api.binance.order.TradeStatus;
import com.cebbus.bot.api.dto.CsIntervalAdapter;
import com.cebbus.bot.api.properties.Symbol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpeculatorIT {

    private Speculator speculator;

    @BeforeEach
    void setUp() {
        Symbol symbol = Symbol.builder()
                .base("BNB")
                .quote("USDT")
                .cacheSize(90)
                .status(TradeStatus.ACTIVE)
                .interval(CsIntervalAdapter.ONE_MINUTE)
                .build();

        this.speculator = new Speculator(symbol);
    }

    @Test
    void activate() {
        this.speculator.activate();
        assertEquals(TradeStatus.ACTIVE, this.speculator.getStatus());
    }

    @Test
    void deactivate() {
        this.speculator.deactivate();
        assertEquals(TradeStatus.INACTIVE, this.speculator.getStatus());
    }

    @Test
    void isActive() {
        assertTrue(this.speculator.isActive());
    }

    @Test
    void isActiveStatusNull() {
        this.speculator.setStatus(null);
        assertTrue(this.speculator.isActive());
    }

}
