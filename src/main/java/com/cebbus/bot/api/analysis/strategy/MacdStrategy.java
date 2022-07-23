package com.cebbus.bot.api.analysis.strategy;

import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.IntegerGene;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

public class MacdStrategy extends BaseCebStrategy {

    public MacdStrategy(BarSeries series) {
        super(series, new Number[]{9, 0});
        build();
    }

    @Override
    public void build() {
        int emaBarCount = this.parameters[0].intValue();
        int emaThreshold = this.parameters[1].intValue();

        ClosePriceIndicator cpi = new ClosePriceIndicator(this.series);

        MACDIndicator macd = new MACDIndicator(cpi);
        EMAIndicator signal = new EMAIndicator(macd, emaBarCount);

        Rule entryRule = new OverIndicatorRule(macd, signal)
                .and(new OverIndicatorRule(macd, emaThreshold));

        Rule exitRule = new UnderIndicatorRule(macd, signal);

        BaseStrategy strategy = new BaseStrategy(MACD_KEY, entryRule, exitRule);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put(MACD_KEY, new LinkedHashMap<>());
        indicators.get(MACD_KEY).put(MACD_KEY, macd);
        indicators.get(MACD_KEY).put(String.format(EMA_PARAM_KEY, emaBarCount), signal);

        this.builderResult = new BuilderResult(strategy, indicators);
    }

    @Override
    public Gene[] createGene(Configuration conf) throws InvalidConfigurationException {
        IntegerGene emaBarCount = new IntegerGene(conf, 1, 20);
        IntegerGene emaThreshold = new IntegerGene(conf, -10, 10);
        return new Gene[]{emaBarCount, emaThreshold};
    }

    @Override
    public Map<String, Number> getParameterMap() {
        Map<String, Number> map = new LinkedHashMap<>(this.parameters.length);
        map.put("EMA Bar Count", this.parameters[0]);
        map.put("EMA Threshold", this.parameters[1]);

        return map;
    }
}
