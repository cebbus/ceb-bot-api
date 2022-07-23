package com.cebbus.bot.api.binance;

import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.SymbolInfo;
import com.cebbus.bot.api.client.BinanceClient;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SymbolLoader {

    private static final BinanceClient CLIENT = new BinanceClient(false);

    public static Set<String> getSymbolList() {
        ExchangeInfo exchangeInfo = CLIENT.getExchangeInfo();

        return exchangeInfo.getSymbols().stream()
                .map(SymbolInfo::getBaseAsset)
                .collect(Collectors.toSet());
    }

    public static List<SymbolInfo> getSymbolListByQuoteAsset(String quote) {
        ExchangeInfo exchangeInfo = CLIENT.getExchangeInfo();

        return exchangeInfo.getSymbols().stream()
                .filter(s -> s.getQuoteAsset().equalsIgnoreCase(quote))
                .sorted(Comparator.comparing(SymbolInfo::getBaseAsset))
                .collect(Collectors.toList());
    }

}
