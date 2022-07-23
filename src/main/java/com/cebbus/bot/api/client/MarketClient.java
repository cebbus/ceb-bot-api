package com.cebbus.bot.api.client;

import com.cebbus.bot.api.dto.CandleDto;
import com.cebbus.bot.api.dto.CsIntervalAdapter;
import com.cebbus.bot.api.dto.TradeDto;

import java.math.BigDecimal;
import java.util.List;

public interface MarketClient {

    BigDecimal getFreeBalance(String symbol);

    BigDecimal getFreeBalance(String symbol, int scale);

    List<TradeDto> loadTradeHistory(String symbol);

    List<CandleDto> loadCandleHistory(String symbol, CsIntervalAdapter interval, Integer limit);
}
