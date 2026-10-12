package com.jfs.cpts.dto.dashboard;

import java.math.BigDecimal;

/**
 * Status distribution data point for charts.
 */
public record StatusDistribution(
    String status,
    long count,
    BigDecimal totalAmount
) {}
