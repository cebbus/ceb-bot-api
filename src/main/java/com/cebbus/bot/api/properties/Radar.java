package com.cebbus.bot.api.properties;

import com.cebbus.bot.api.dto.CsIntervalAdapter;
import lombok.Data;

@Data
public class Radar {
    private final boolean active;
    private final String quote;
    private final CsIntervalAdapter interval;
}