package com.cebbus.bot.api.job;

import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;
import com.cebbus.bot.api.Speculator;
import com.cebbus.bot.api.binance.mapper.CandlestickMapper;
import com.cebbus.bot.api.client.BinanceClient;
import com.cebbus.bot.api.dto.CsIntervalAdapter;
import com.cebbus.bot.api.properties.Symbol;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

public class SpeculatorJob implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Speculator speculator = (Speculator) dataMap.get("speculator");
        BinanceClient marketClient = (BinanceClient) speculator.getMarketClient();

        Symbol symbol = speculator.getSymbol();
        String name = symbol.getName();
        CsIntervalAdapter interval = symbol.getInterval();

        Candlestick bar = marketClient.getLastCandle(name, interval);
        CandlestickEvent event = CandlestickMapper.stickToEvent(bar, name);
        speculator.triggerListener(event);
    }
}
