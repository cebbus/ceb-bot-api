package com.cebbus.bot.api.dto;

import java.time.Duration;

public enum CsIntervalAdapter {
    ONE_MINUTE("10 0/1 * * * ?", "30 0/1 * * * ?", Duration.ofMinutes(1L)),
    THREE_MINUTES("10 0/3 * * * ?", "30 0/3 * * * ?", Duration.ofMinutes(3L)),
    FIVE_MINUTES("10 0/5 * * * ?", "30 0/5 * * * ?", Duration.ofMinutes(5L)),
    FIFTEEN_MINUTES("10 0/15 * * * ?", "30 0/15 * * * ?", Duration.ofMinutes(15L)),
    HALF_HOURLY("10 0/30 * * * ?", "30 0/30 * * * ?", Duration.ofMinutes(30L)),
    HOURLY("10 0 0/1 * * ?", "0 5 0/1 * * ?", Duration.ofHours(1L)),
    TWO_HOURLY("10 0 0/2 * * ?", "0 5 0/2 * * ?", Duration.ofHours(2L)),
    FOUR_HOURLY("10 0 0/4 * * ?", "0 5 0/4 * * ?", Duration.ofHours(4L)),
    SIX_HOURLY("10 0 0/6 * * ?", "0 5 0/6 * * ?", Duration.ofHours(6L)),
    EIGHT_HOURLY("10 0 0/8 * * ?", "0 5 0/8 * * ?", Duration.ofHours(8L)),
    TWELVE_HOURLY("10 0 0/12 * * ?", "0 5 0/12 * * ?", Duration.ofHours(12L)),
    DAILY("10 0 0 * * ?", "0 10 0 * * ?", Duration.ofDays(1L)),
    THREE_DAILY("10 0 0 1/3 * ?", "0 10 0 1/3 * ?", Duration.ofDays(3L)),
    WEEKLY("10 0 0 1/7 * ?", "0 10 0 1/7 * ?", Duration.ofDays(7L)),
    MONTHLY("10 0 0 L * ?", "0 10 0 L * ?", Duration.ofDays(30L));

    private final String cron;
    private final String radarCron;
    private final Duration duration;

    CsIntervalAdapter(String cron, String radarCron, Duration duration) {
        this.cron = cron;
        this.radarCron = radarCron;
        this.duration = duration;
    }

    public String getCron() {
        return cron;
    }

    public String getRadarCron() {
        return radarCron;
    }

    public Duration getDuration() {
        return duration;
    }
}
