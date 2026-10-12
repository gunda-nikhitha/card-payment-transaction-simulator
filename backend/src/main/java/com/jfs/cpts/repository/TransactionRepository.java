package com.jfs.cpts.repository;

import com.jfs.cpts.entity.Transaction;
import com.jfs.cpts.entity.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByTransactionReference(String reference);

    Optional<Transaction> findByIdempotencyKeyAndUserId(String idempotencyKey, Long userId);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    // ========== Dashboard Queries ==========

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId AND t.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TransactionStatus status);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId")
    BigDecimal sumAmountByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    // ========== Admin Dashboard Queries ==========

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = :status")
    long countByStatus(@Param("status") TransactionStatus status);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t")
    BigDecimal sumAllAmount();

    @Query("SELECT t FROM Transaction t ORDER BY t.createdAt DESC")
    List<Transaction> findRecentTransactions(Pageable pageable);

    // ========== Trend Queries ==========

    @Query(value = "SELECT DATE(t.created_at) as date, COUNT(*) as total, " +
           "SUM(t.amount) as amount, " +
           "SUM(CASE WHEN t.status = 'APPROVED' THEN 1 ELSE 0 END) as approved, " +
           "SUM(CASE WHEN t.status = 'DECLINED' THEN 1 ELSE 0 END) as declined, " +
           "SUM(CASE WHEN t.status = 'TIMEOUT' THEN 1 ELSE 0 END) as timeout " +
           "FROM transactions t WHERE t.user_id = :userId AND t.created_at >= :since " +
           "GROUP BY DATE(t.created_at) ORDER BY DATE(t.created_at)",
           nativeQuery = true)
    List<Object[]> findDailyTrendsByUserId(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query(value = "SELECT DATE(t.created_at) as date, COUNT(*) as total, " +
           "SUM(t.amount) as amount, " +
           "SUM(CASE WHEN t.status = 'APPROVED' THEN 1 ELSE 0 END) as approved, " +
           "SUM(CASE WHEN t.status = 'DECLINED' THEN 1 ELSE 0 END) as declined, " +
           "SUM(CASE WHEN t.status = 'TIMEOUT' THEN 1 ELSE 0 END) as timeout " +
           "FROM transactions t WHERE t.created_at >= :since " +
           "GROUP BY DATE(t.created_at) ORDER BY DATE(t.created_at)",
           nativeQuery = true)
    List<Object[]> findDailyTrendsAll(@Param("since") LocalDateTime since);
}
