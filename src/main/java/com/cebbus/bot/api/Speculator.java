package com.cebbus.bot.api;

import com.binance.api.client.domain.event.CandlestickEvent;
import com.cebbus.bot.api.analysis.TheOracle;
import com.cebbus.bot.api.binance.listener.CandlestickEventListener;
import com.cebbus.bot.api.binance.listener.operation.EventOperation;
import com.cebbus.bot.api.binance.listener.operation.TradeOperation;
import com.cebbus.bot.api.binance.listener.operation.UpdateSeriesOperation;
import com.cebbus.bot.api.binance.order.TradeStatus;
import com.cebbus.bot.api.client.MarketClient;
import com.cebbus.bot.api.dto.CandleDto;
import com.cebbus.bot.api.dto.CsIntervalAdapter;
import com.cebbus.bot.api.dto.TradeDto;
import com.cebbus.bot.api.properties.Symbol;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Data
@Slf4j
public class Speculator {

    private final Symbol symbol;
    private final MarketClient marketClient;

    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private final CandlestickEventListener listener = new CandlestickEventListener();

    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private final List<Consumer<Boolean>> manualTradeListeners = new CopyOnWriteArrayList<>();

    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private final List<Consumer<TradeStatus>> statusChangeListeners = new CopyOnWriteArrayList<>();

    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private final List<Consumer<Speculator>> parameterChangeListeners = new CopyOnWriteArrayList<>();

    private TradeStatus status;
    private TheOracle theOracle;

    public Speculator(Symbol symbol) {
        this(symbol, null);
    }

    public Speculator(Symbol symbol, MarketClient marketClient) {
        this.symbol = symbol;
        this.status = symbol.getStatus();
        this.marketClient = marketClient;

        if (this.marketClient != null) {
            String name = symbol.getName();
            CsIntervalAdapter interval = symbol.getInterval();
            Integer limit = symbol.getCacheSize();

            List<TradeDto> tradeList = marketClient.loadTradeHistory(name);
            List<CandleDto> candleList = marketClient.loadCandleHistory(name, interval, limit);

            this.theOracle = new TheOracle(symbol, tradeList, candleList);

            this.listener.addOperation(new UpdateSeriesOperation(this));
            this.listener.addOperation(new TradeOperation(this));
        }
    }

    public int addCandlestickEventOperation(EventOperation operation) {
        return this.listener.addOperation(operation);
    }

    public void removeCandlestickEventOperation(int index) {
        this.listener.removeOperation(index);
    }

    public int addStatusChangeListener(Consumer<TradeStatus> operation) {
        this.statusChangeListeners.add(operation);
        return this.statusChangeListeners.size() - 1;
    }

    public void removeStatusChangeListener(int index) {
        this.statusChangeListeners.remove(index);
    }

    public void addParameterChangeListener(Consumer<Speculator> operation) {
        this.parameterChangeListeners.add(operation);
    }

    public void clearParameterChangeListener() {
        this.parameterChangeListeners.clear();
    }

    public int addManualTradeListeners(Consumer<Boolean> operation) {
        this.manualTradeListeners.add(operation);
        return this.manualTradeListeners.size() - 1;
    }

    public void removeManualTradeListeners(int index) {
        this.manualTradeListeners.remove(index);
    }

    public boolean buy() {
        return trade(true);
    }

    public boolean sell() {
        return trade(false);
    }

    public void activate() {
        this.status = TradeStatus.ACTIVE;
        this.statusChangeListeners.forEach(o -> o.accept(this.status));
    }

    public void deactivate() {
        this.status = TradeStatus.INACTIVE;
        this.statusChangeListeners.forEach(o -> o.accept(this.status));
    }

    public boolean isActive() {
        return status == null || status == TradeStatus.ACTIVE;
    }

    public List<Pair<String, String>> calcStrategies() {
        Objects.requireNonNull(this.theOracle);
        DecimalFormat format = new DecimalFormat("#,###.0000");

        List<Pair<String, Double>> pairs = theOracle.calcStrategies();
        return pairs.stream().map(p -> Pair.of(p.getKey(), format.format(p.getValue()))).collect(Collectors.toList());
    }

    public void changeParameters(Number... parameters) {
        Objects.requireNonNull(this.theOracle);
        this.theOracle.changeProphesyParameters(parameters);
        this.parameterChangeListeners.forEach(o -> o.accept(this));
    }

    public void changeStrategy(String strategy) {
        Objects.requireNonNull(this.theOracle);
        this.theOracle = this.theOracle.changeStrategy(strategy);
    }

    public void triggerListener(CandlestickEvent event) {
        this.listener.onResponse(event);
    }

    private boolean trade(boolean isBuy) {
        TradeOperation trader = new TradeOperation(this);
        boolean success = isBuy ? trader.manualEnter() : trader.manualExit();

        this.manualTradeListeners.forEach(o -> o.accept(success));
        return success;
    }
}
