package com.cebbus.bot.api.client;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.account.*;
import com.binance.api.client.domain.account.request.AllOrdersRequest;
import com.binance.api.client.domain.account.request.OrderStatusRequest;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.SymbolInfo;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.bot.api.binance.mapper.CandlestickMapper;
import com.cebbus.bot.api.binance.mapper.TradeMapper;
import com.cebbus.bot.api.dto.CandleDto;
import com.cebbus.bot.api.dto.CsIntervalAdapter;
import com.cebbus.bot.api.dto.TradeDto;
import com.cebbus.bot.api.exception.OrderNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.binance.api.client.domain.OrderStatus.*;
import static java.math.RoundingMode.DOWN;

@Slf4j
public class BinanceClient implements MarketClient {

    private final BinanceApiRestClient restClient;

    public BinanceClient(boolean test) {
        this(null, null, test);
    }

    public BinanceClient(String key, String secret, boolean test) {
        BinanceApiClientFactory clientFactory = BinanceApiClientFactory.newInstance(key, secret, test, test);
        this.restClient = clientFactory.newRestClient();
    }

    public ExchangeInfo getExchangeInfo() {
        return this.restClient.getExchangeInfo();
    }

    public SymbolInfo getSymbolInfo(String symbol) {
        return getExchangeInfo().getSymbolInfo(symbol);
    }

    public Candlestick getLastCandle(String symbol, CsIntervalAdapter interval) {
        CandlestickInterval csInterval = CandlestickInterval.valueOf(interval.name());
        List<Candlestick> bars = this.restClient.getCandlestickBars(symbol, csInterval, 2, null, null);
        return bars.get(0);
    }

    public NewOrderResponse marketSell(String symbol, BigDecimal quantity) {
        NewOrder sellOrder = NewOrder.marketSell(symbol, quantity.toPlainString());
        return this.restClient.newOrder(sellOrder);
    }

    public NewOrderResponse marketBuy(String symbol, BigDecimal quantity) {
        NewOrder buyOrder = NewOrder.marketBuy(symbol, null).quoteOrderQty(quantity.toPlainString());
        return this.restClient.newOrder(buyOrder);
    }

    public boolean isOrderFilled(NewOrderResponse response) {
        Long orderId = response.getOrderId();
        OrderStatus status = response.getStatus();

        List<OrderStatus> invalidStatusList = List.of(CANCELED, PENDING_CANCEL, REJECTED);

        if (invalidStatusList.contains(status)) {
            log.error("Order not filled! Order Id: {}, Status: {}", orderId, status);
            return false;
        }

        for (int i = 0; i < 5; i++) {
            if (status != FILLED) {
                log.warn("Order not filled! Order Id: {}, Status: {}, Attempt: {}", orderId, status, i);
                sleepThread();

                status = getOrderStatus(response);
            }
        }

        return true;
    }

    //order not returns immediately, that's why wait a second before retry
    public Order findOrder(NewOrderResponse response) {
        Long id = response.getOrderId();
        String symbol = response.getSymbol();

        for (int i = 0; i < 5; i++) {
            List<Order> orders = this.restClient.getAllOrders(new AllOrdersRequest(symbol));
            Optional<Order> order = orders.stream().filter(o -> o.getOrderId().equals(id)).findFirst();

            if (order.isPresent()) {
                return order.get();
            } else {
                log.warn("Order not found! Order Id: {}, Attempt: {}", id, i);
                sleepThread();
            }
        }

        log.error("Order not found! Order Id: {}", id);
        throw new OrderNotFoundException();
    }

    @Override
    public BigDecimal getFreeBalance(String symbol) {
        Account account = this.restClient.getAccount();
        AssetBalance balance = account.getAssetBalance(symbol);

        return new BigDecimal(balance.getFree());
    }

    @Override
    public BigDecimal getFreeBalance(String symbol, int scale) {
        return getFreeBalance(symbol).setScale(scale, DOWN);
    }

    @Override
    public List<TradeDto> loadTradeHistory(String symbol) {
        try {
            List<Trade> tradeList = this.restClient.getMyTrades(symbol);
            return TradeMapper.tradeToDto(tradeList);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    @Override
    public List<CandleDto> loadCandleHistory(String symbol, CsIntervalAdapter interval, Integer limit) {
        CandlestickInterval csInterval = CandlestickInterval.valueOf(interval.name());

        List<Candlestick> bars = this.restClient.getCandlestickBars(symbol, csInterval, limit, null, null);
        if (bars.size() > 1) {
            bars.remove(bars.size() - 1);
        }

        return CandlestickMapper.candleToDto(bars);
    }

    private OrderStatus getOrderStatus(NewOrderResponse response) {
        Long id = response.getOrderId();
        String symbol = response.getSymbol();

        OrderStatusRequest request = new OrderStatusRequest(symbol, id);
        Order order = this.restClient.getOrderStatus(request);

        return order != null ? order.getStatus() : PARTIALLY_FILLED;
    }

    private void sleepThread() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }
}
