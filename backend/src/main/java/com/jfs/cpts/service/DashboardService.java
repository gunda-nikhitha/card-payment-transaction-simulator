package com.jfs.cpts.service;

import com.jfs.cpts.dto.dashboard.DashboardSummary;
import com.jfs.cpts.dto.dashboard.StatusDistribution;
import com.jfs.cpts.dto.dashboard.TrendDataPoint;
import com.jfs.cpts.entity.TransactionStatus;
import com.jfs.cpts.mapper.EntityMapper;
import com.jfs.cpts.repository.TransactionRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard analytics service with caching support.
 */
@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final EntityMapper mapper;

    public DashboardService(TransactionRepository transactionRepository, EntityMapper mapper) {
        this.transactionRepository = transactionRepository;
        this.mapper = mapper;
    }

    /**
     * Get dashboard summary for a specific user.
     */
    @Transactional(readOnly = true)
    public DashboardSummary getUserDashboardSummary(Long userId) {
        long total = transactionRepository.countByUserId(userId);
        long approved = transactionRepository.countByUserIdAndStatus(userId, TransactionStatus.APPROVED);
        long declined = transactionRepository.countByUserIdAndStatus(userId, TransactionStatus.DECLINED);
        long timeout = transactionRepository.countByUserIdAndStatus(userId, TransactionStatus.TIMEOUT);
        BigDecimal totalAmount = transactionRepository.sumAmountByUserId(userId);

        double approvalRate = total > 0 ? (double) approved / total * 100 : 0.0;
        approvalRate = BigDecimal.valueOf(approvalRate).setScale(1, RoundingMode.HALF_UP).doubleValue();

        var recentTxns = transactionRepository.findRecentByUserId(userId, PageRequest.of(0, 5))
            .stream().map(mapper::toRecentTransaction).toList();

        return new DashboardSummary(total, approved, declined, timeout, approvalRate,
            totalAmount != null ? totalAmount : BigDecimal.ZERO, recentTxns);
    }

    /**
     * Get admin dashboard summary (all users).
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "adminDashboard", key = "'summary'")
    public DashboardSummary getAdminDashboardSummary() {
        long total = transactionRepository.count();
        long approved = transactionRepository.countByStatus(TransactionStatus.APPROVED);
        long declined = transactionRepository.countByStatus(TransactionStatus.DECLINED);
        long timeout = transactionRepository.countByStatus(TransactionStatus.TIMEOUT);
        BigDecimal totalAmount = transactionRepository.sumAllAmount();

        double approvalRate = total > 0 ? (double) approved / total * 100 : 0.0;
        approvalRate = BigDecimal.valueOf(approvalRate).setScale(1, RoundingMode.HALF_UP).doubleValue();

        var recentTxns = transactionRepository.findRecentTransactions(PageRequest.of(0, 10))
            .stream().map(mapper::toRecentTransaction).toList();

        return new DashboardSummary(total, approved, declined, timeout, approvalRate,
            totalAmount != null ? totalAmount : BigDecimal.ZERO, recentTxns);
    }

    /**
     * Get status distribution for charts.
     */
    @Transactional(readOnly = true)
    public List<StatusDistribution> getStatusDistribution(Long userId) {
        List<StatusDistribution> result = new ArrayList<>();
        for (TransactionStatus status : List.of(TransactionStatus.APPROVED, TransactionStatus.DECLINED, TransactionStatus.TIMEOUT)) {
            long count;
            if (userId != null) {
                count = transactionRepository.countByUserIdAndStatus(userId, status);
            } else {
                count = transactionRepository.countByStatus(status);
            }
            result.add(new StatusDistribution(status.name(), count, BigDecimal.ZERO));
        }
        return result;
    }

    /**
     * Get daily transaction trends.
     */
    @Transactional(readOnly = true)
    public List<TrendDataPoint> getTrends(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Object[]> rows;

        if (userId != null) {
            rows = transactionRepository.findDailyTrendsByUserId(userId, since);
        } else {
            rows = transactionRepository.findDailyTrendsAll(since);
        }

        return rows.stream().map(row -> new TrendDataPoint(
            row[0].toString(),
            ((Number) row[1]).longValue(),
            row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO,
            ((Number) row[3]).longValue(),
            ((Number) row[4]).longValue(),
            ((Number) row[5]).longValue()
        )).toList();
    }
}
