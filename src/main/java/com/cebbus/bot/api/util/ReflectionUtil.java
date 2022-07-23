package com.cebbus.bot.api.util;

import com.cebbus.bot.api.analysis.strategy.*;
import com.cebbus.bot.api.exception.StrategyNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.BarSeries;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Slf4j
public class ReflectionUtil {

    private ReflectionUtil() {
    }

    public static CebStrategy initStrategy(BarSeries series, String strategy) {
        try {
            Class<?> strategyClazz = Class.forName("com.cebbus.bot.api.analysis.strategy." + strategy.trim());
            return initStrategy(series, strategyClazz);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new StrategyNotFoundException();
        }
    }

    public static CebStrategy initStrategy(BarSeries series, Class<?> strategyClazz) {
        Object[] initArgs = new Object[]{series};
        Class<?>[] parameterTypes = new Class<?>[]{BarSeries.class};

        try {
            return (CebStrategy) strategyClazz.getDeclaredConstructor(parameterTypes).newInstance(initArgs);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            log.error(e.getMessage(), e);
            throw new StrategyNotFoundException();
        }
    }

    public static List<Class<? extends BaseCebStrategy>> listStrategyClasses() {
        return List.of(
                AdxStrategy.class,
                CciCorrectionStrategy.class,
                DummyStrategy.class,
                GlobalExtremaStrategy.class,
                GoldenCrossStrategy.class,
                MacdStrategy.class,
                MovingMomentumStrategy.class,
                ObvStrategy.class,
                Rsi2Strategy.class,
                ScalpingStrategy.class,
                RsiStrategy.class);
    }
}
