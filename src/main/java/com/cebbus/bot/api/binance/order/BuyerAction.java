package com.cebbus.bot.api.binance.order;

import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.general.SymbolInfo;
import com.cebbus.bot.api.Speculator;
import com.cebbus.bot.api.dto.TradeDto;
import com.cebbus.bot.api.exception.ZeroWeightException;
import com.cebbus.bot.api.util.SpeculatorHolder;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

                NewOrderResponse orderResponse = buy(specHolder);
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
        if (isSpecActive) {
            String quote = this.symbol.getQuote();
            BigDecimal freeBalance = this.marketClient.getFreeBalance(quote);

            if (freeBalance.doubleValue() <= 0) {
                log.info("{} - you have no balance!", quote);
                return false;
            }
        }

        return this.theOracle.shouldEnter(isSpecActive, isManual);
    }

    private NewOrderResponse buy(SpeculatorHolder specHolder) {
        double weight = specHolder.calculateWeight(this.speculator);
        if (weight == 0) {
            throw new ZeroWeightException("weight must be greater than zero");
        }

        String name = this.symbol.getName();
        String quote = this.symbol.getQuote();
        SymbolInfo symbolInfo = this.marketClient.getSymbolInfo(name);
        BigDecimal freeBalance = this.marketClient.getFreeBalance(quote);

        BigDecimal quantity = freeBalance
                .multiply(BigDecimal.valueOf(weight))
                .setScale(symbolInfo.getQuotePrecision(), RoundingMode.DOWN);

        return this.marketClient.marketBuy(name, quantity);
    }

}
