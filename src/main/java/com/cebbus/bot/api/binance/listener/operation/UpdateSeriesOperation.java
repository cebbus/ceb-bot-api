package com.cebbus.bot.api.binance.listener.operation;

import com.binance.api.client.domain.event.CandlestickEvent;
import com.cebbus.bot.api.analysis.TheOracle;
import com.cebbus.bot.api.Speculator;
import com.cebbus.bot.api.binance.mapper.CandlestickMapper;
import com.cebbus.bot.api.dto.CandleDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateSeriesOperation implements EventOperation {

    private final TheOracle theOracle;

    public UpdateSeriesOperation(Speculator speculator) {
        this.theOracle = speculator.getTheOracle();
    }

    @Override
    public void operate(CandlestickEvent response) {
        CandleDto newBar = CandlestickMapper.eventToDto(response);
        this.theOracle.addBar(newBar);
    }
}
