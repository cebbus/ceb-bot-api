package com.cebbus.bot.api.util;

import com.cebbus.bot.api.exception.StrategyNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReflectionUtilTest {

    @Test
    void initStrategy() {
        assertThrows(StrategyNotFoundException.class, () -> ReflectionUtil.initStrategy(null, "TestStrategy"));
    }

    @Test
    void testInitStrategy() {
        assertThrows(StrategyNotFoundException.class, () -> ReflectionUtil.initStrategy(null, Double.class));
    }

    @Test
    void listStrategyClasses() {
        assertEquals(11, ReflectionUtil.listStrategyClasses().size());
    }
}