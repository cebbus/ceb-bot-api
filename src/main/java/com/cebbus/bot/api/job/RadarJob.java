package com.cebbus.bot.api.job;

import com.binance.api.client.domain.general.SymbolInfo;
import com.cebbus.bot.api.Speculator;
import com.cebbus.bot.api.analysis.TheOracle;
import com.cebbus.bot.api.analysis.strategy.BaseCebStrategy;
import com.cebbus.bot.api.binance.SymbolLoader;
import com.cebbus.bot.api.binance.order.TradeStatus;
import com.cebbus.bot.api.client.MarketClient;
import com.cebbus.bot.api.dto.CandleDto;
import com.cebbus.bot.api.dto.CriterionResultDto;
import com.cebbus.bot.api.dto.CsIntervalAdapter;
import com.cebbus.bot.api.dto.TradeRowDto;
import com.cebbus.bot.api.notification.Notifier;
import com.cebbus.bot.api.properties.Radar;
import com.cebbus.bot.api.properties.Symbol;
import com.cebbus.bot.api.util.ReflectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.*;

@Slf4j
public class RadarJob implements Job {

    private static final int RADAR_LIMIT = 360;

    @Override
    public void execute(JobExecutionContext context) {
        Map<String, List<String>> symbolMap = new TreeMap<>();

        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Radar radar = (Radar) dataMap.get("radar");
        Notifier notifier = (Notifier) dataMap.get("notifier");
        MarketClient marketClient = (MarketClient) dataMap.get("marketClient");

        String quote = radar.getQuote();
        CsIntervalAdapter interval = radar.getInterval();

        log.info("radar started!");

        List<Class<? extends BaseCebStrategy>> strategyList = ReflectionUtil.listStrategyClasses();

        List<SymbolInfo> symbolList = SymbolLoader.getSymbolListByQuoteAsset(quote);
        log.info("radar detected {} symbols!", symbolList.size());

        for (SymbolInfo symbolInfo : symbolList) {
            Speculator speculator = createSpeculator(symbolInfo, interval, marketClient);

            List<String> inPositionStrategyList = new ArrayList<>();
            for (Class<? extends BaseCebStrategy> strategyClazz : strategyList) {
                String strategy = strategyClazz.getSimpleName();
                speculator.changeStrategy(strategy);

                TheOracle theOracle = speculator.getTheOracle();
                CandleDto lastCandle = theOracle.getLastCandle();
                Optional<TradeRowDto> lastTrade = theOracle.getLastTradeRow(true);
                List<CriterionResultDto> criterionResultList = theOracle.getCriterionResultList(true);

                boolean isInPosition = theOracle.isInPosition(speculator.isActive());
                boolean isProfitable = (double) criterionResultList.get(2).getValue() > 1;
                boolean hasGoodWinRatio = (double) criterionResultList.get(5).getValue() > 0.5;
                boolean isTrendsUp = lastTrade.isPresent() && lastCandle.getClose().doubleValue() > (lastTrade.get().getPrice().doubleValue() * 0.8);

                if (isInPosition && isProfitable && hasGoodWinRatio && isTrendsUp) {
                    inPositionStrategyList.add(strategy);
                }
            }

            if (inPositionStrategyList.size() >= 2) {
                symbolMap.put(speculator.getSymbol().getName(), inPositionStrategyList);
            }
        }

        sendNotification(notifier, symbolMap);

        log.info("radar finished!");
    }

    private Speculator createSpeculator(SymbolInfo symbolInfo, CsIntervalAdapter interval, MarketClient marketClient) {
        String base = symbolInfo.getBaseAsset();
        String quote = symbolInfo.getQuoteAsset();
        String junkStrategy = "JunkStrategy";

        Symbol symbol = Symbol.builder()
                .id(-1)
                .weight(0)
                .base(base)
                .quote(quote)
                .strategy(junkStrategy)
                .interval(interval)
                .cacheSize(RADAR_LIMIT)
                .status(TradeStatus.INACTIVE)
                .build();

        return new Speculator(symbol, marketClient);
    }

    private void sendNotification(Notifier notifier, Map<String, List<String>> symbolMap) {
        StringBuilder message = new StringBuilder();

        symbolMap.forEach((symbol, strategyList) -> message.append("check this symbol ")
                .append(symbol)
                .append(" - ")
                .append("strategy list ")
                .append(strategyList)
                .append(System.lineSeparator()));


        if (message.length() == 0) {
            notifier.send("market is fully with shit coins... nothing notable...");
        } else {
            notifier.send(message.toString());
        }
    }
}
