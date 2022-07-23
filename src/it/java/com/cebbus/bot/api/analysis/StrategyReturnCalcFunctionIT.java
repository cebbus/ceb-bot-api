package com.cebbus.bot.api.analysis;

import com.cebbus.bot.api.analysis.mapper.BarMapper;
import com.cebbus.bot.api.analysis.strategy.MacdStrategy;
import com.cebbus.bot.api.binance.order.TradeStatus;
import com.cebbus.bot.api.client.BinanceClient;
import com.cebbus.bot.api.client.MarketClient;
import com.cebbus.bot.api.dto.CandleDto;
import com.cebbus.bot.api.dto.CsIntervalAdapter;
import com.cebbus.bot.api.properties.Symbol;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StrategyReturnCalcFunctionIT {

    private static final String KEY = "kjPvnb6TMJvaR33nXoVUtudaRBHAJKHDssurQkgZtikkuAtNbwuleJLJe3RJkPCI";
    private static final String SECRET = "yuUBaD6IwszVn8KGqpCRyYKgmc2ImUKeqaqI9ipBf9MF3rO0k3kp9nx7pEazRruY";

    private StrategyReturnCalcFunction func;

    @BeforeEach
    void setUp() {
        Symbol symbol = Symbol.builder()
                .base("BNB")
                .quote("USDT")
                .cacheSize(90)
                .status(TradeStatus.ACTIVE)
                .interval(CsIntervalAdapter.ONE_MINUTE)
                .build();

        String name = symbol.getName();
        Integer limit = symbol.getCacheSize();
        CsIntervalAdapter interval = symbol.getInterval();

        MarketClient marketClient = new BinanceClient(KEY, SECRET, true);
        List<CandleDto> stickList = marketClient.loadCandleHistory(name, interval, limit);

        List<Bar> barList = BarMapper.dtoToBar(stickList, interval);

        BarSeries series = new BaseBarSeriesBuilder().withBars(barList).build();
        this.func = new StrategyReturnCalcFunction(symbol, series);
    }

    @Test
    void apply() {
        Pair<String, Double> actual = this.func.apply(MacdStrategy.class);

        assertNotNull(actual);
        assertEquals("MacdStrategy", actual.getKey());
    }

}
