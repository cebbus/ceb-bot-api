package com.cebbus.bot.api.binance.listener.operation;

import com.binance.api.client.domain.event.CandlestickEvent;

public interface EventOperation {
    void operate(CandlestickEvent response);
}
