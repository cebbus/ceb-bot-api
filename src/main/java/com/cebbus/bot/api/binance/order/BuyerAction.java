package com.cebbus.bot.api.binance.order;

import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.general.SymbolFilter;
import com.binance.api.client.domain.general.SymbolInfo;
import com.binance.api.client.domain.market.Candlestick;
import com.cebbus.bot.api.Speculator;
import com.cebbus.bot.api.dto.CsIntervalAdapter;
import com.cebbus.bot.api.dto.TradeDto;
import com.cebbus.bot.api.exception.ZeroWeightException;
import com.cebbus.bot.api.util.SpeculatorHolder;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
public class BuyerAction extends TraderAction {

    public BuyerAction(Speculator speculator) {
        super(speculator);
    }

    public TradeDto enter() {
        if (this.speculator.isActive()) {
            SpeculatorHolder specHolder = SpeculatorHolder.getInstance();

            try {
                specHolder.lock(this.speculator);

                NewOrderResponse orderResponse = buy();
                return createTradeRecord(orderResponse);
            } finally {
                specHolder.releaseLock(this.speculator);
            }
        } else {
            return createBacktestRecord();
        }
    }

    public boolean enterable(boolean isManual) {
        boolean isSpecActive = this.speculator.isActive();
        if (!this.theOracle.isNewPosition(isSpecActive)) {
            log.info("{} - you are already in a position!", this.symbol.getName());
            return false;
        }

        if (isSpecActive) {
            String quote = this.symbol.getQuote();
            BigDecimal freeBalance = calculateFreeBalance();

            if (freeBalance.doubleValue() <= 0) {
                log.info("{} - you have no balance!", quote);
                return false;
            } else {
                String name = this.symbol.getName();
                CsIntervalAdapter interval = this.symbol.getInterval();

                Candlestick lastCandle = this.marketClient.getLastCandle(name, interval);
                BigDecimal closePrice = new BigDecimal(lastCandle.getClose());
                BigDecimal prospectQuantity = freeBalance.divide(closePrice, RoundingMode.DOWN);

                List<SymbolFilter> filterList = getLotSizeFilterList();
                for (SymbolFilter filter : filterList) {
                    BigDecimal minQuantity = new BigDecimal(filter.getMinQty());

                    if (prospectQuantity.compareTo(minQuantity) < 0) {
                        log.info("{} - you have no enough balance! " +
                                        "free balance: {}, quantity: {}, min quantity: {}",
                                name, freeBalance, prospectQuantity, minQuantity);

                        return false;
                    }
                }
            }
        }

        return this.theOracle.shouldEnter(isSpecActive, isManual);
    }

    private NewOrderResponse buy() {
        String name = this.symbol.getName();
        BigDecimal freeBalance = calculateFreeBalance();

        return this.marketClient.marketBuy(name, freeBalance);
    }

    private BigDecimal calculateFreeBalance() {
        SpeculatorHolder specHolder = SpeculatorHolder.getInstance();
        double weight = specHolder.calculateWeight(this.speculator);
        if (weight == 0) {
            throw new ZeroWeightException("weight must be greater than zero");
        }

        String name = this.symbol.getName();
        String quote = this.symbol.getQuote();
        SymbolInfo symbolInfo = this.marketClient.getSymbolInfo(name);
        BigDecimal freeBalance = this.marketClient.getFreeBalance(quote);

        return freeBalance
                .multiply(BigDecimal.valueOf(weight))
                .setScale(symbolInfo.getQuotePrecision(), RoundingMode.DOWN);
    }

}
