package com.cebbus.bot.api.analysis.strategy;

import lombok.Data;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.InvalidConfigurationException;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.Map;

public interface CebStrategy {
    void build();

    void rebuild(Number... parameters);

    boolean shouldEnter(TradingRecord tradingRecord);

    boolean shouldExit(TradingRecord tradingRecord);

    CebStrategy and(CebStrategy other);

    CebStrategy or(CebStrategy other);

    BarSeries getSeries();

    Strategy getStrategy();

    Map<String, Map<String, CachedIndicator<Num>>> getIndicators();

    Gene[] createGene(Configuration conf) throws InvalidConfigurationException;

    Number[] getParameters();

    Map<String, Number> getParameterMap();

    @Data
    final class BuilderResult {
        private final Strategy strategy;
        private final Map<String, Map<String, CachedIndicator<Num>>> indicators;
    }
}
