package com.jfs.cpts.dto.dashboard;

import java.math.BigDecimal;

/**
 * Trend data point for time-series charts.
 */
public record TrendDataPoint(
    String date,
    long count,
    BigDecimal totalAmount,
    long approved,
    long declined,
    long timeout
) {}
