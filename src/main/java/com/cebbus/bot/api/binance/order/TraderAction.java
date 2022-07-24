package com.cebbus.bot.api.binance.order;

import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.general.FilterType;
import com.binance.api.client.domain.general.SymbolFilter;
import com.binance.api.client.domain.general.SymbolInfo;
import com.cebbus.bot.api.Speculator;
import com.cebbus.bot.api.analysis.TheOracle;
import com.cebbus.bot.api.client.BinanceClient;
import com.cebbus.bot.api.dto.TradeDto;
import com.cebbus.bot.api.exception.OrderNotFilledException;
import com.cebbus.bot.api.properties.Symbol;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.List;

import static java.math.RoundingMode.DOWN;

@Slf4j
public abstract class TraderAction {

    static final int SCALE = 8;

    final Speculator speculator;
    final Symbol symbol;
    final TheOracle theOracle;
    final BinanceClient marketClient;

    TraderAction(Speculator speculator) {
        this.speculator = speculator;
        this.symbol = speculator.getSymbol();
        this.theOracle = speculator.getTheOracle();
        this.marketClient = (BinanceClient) speculator.getMarketClient();
    }

    TradeDto createBacktestRecord() {
        return this.theOracle.newTrade(false, null);
    }

    TradeDto createTradeRecord(NewOrderResponse response) {
        if (!this.marketClient.isOrderFilled(response)) {
            throw new OrderNotFilledException();
        }

        Order order = this.marketClient.findOrder(response);
        Pair<Number, Number> priceAmount = getPriceAmountPair(order);

        return this.theOracle.newTrade(true, priceAmount);
    }

    List<SymbolFilter> getLotSizeFilterList() {
        String name = this.symbol.getName();
        SymbolInfo symbolInfo = this.marketClient.getSymbolInfo(name);
        SymbolFilter lotSize = symbolInfo.getSymbolFilter(FilterType.LOT_SIZE);
        SymbolFilter marketLotSize = symbolInfo.getSymbolFilter(FilterType.MARKET_LOT_SIZE);

        return List.of(lotSize, marketLotSize);
    }

    private Pair<Number, Number> getPriceAmountPair(Order order) {
        BigDecimal amount = strToBd(order.getExecutedQty());

        BigDecimal quote = strToBd(order.getCummulativeQuoteQty());
        BigDecimal price = quote.divide(amount, SCALE, DOWN);

        return Pair.of(price, amount);
    }

    private BigDecimal strToBd(String value) {
        return new BigDecimal(value).setScale(SCALE, DOWN);
    }
}
