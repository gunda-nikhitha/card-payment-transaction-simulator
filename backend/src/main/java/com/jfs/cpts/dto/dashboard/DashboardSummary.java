package com.jfs.cpts.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dashboard summary DTO with aggregate metrics.
 */
public record DashboardSummary(
    long totalTransactions,
    long approvedTransactions,
    long declinedTransactions,
    long timeoutTransactions,
    double approvalRate,
    BigDecimal totalAmountSimulated,
    List<RecentTransaction> recentTransactions
) {
    public record RecentTransaction(
        String transactionReference,
        String merchantName,
        BigDecimal amount,
        String currency,
        String status,
        String createdAt
    ) {}
}
