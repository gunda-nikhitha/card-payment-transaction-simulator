package com.jfs.cpts.repository;

import com.jfs.cpts.entity.Transaction;
import com.jfs.cpts.entity.TransactionStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Specifications for type-safe, composable transaction filtering.
 * Prevents unsafe field injection by whitelisting filter fields.
 */
public class TransactionSpecifications {

    private TransactionSpecifications() {}

    public static Specification<Transaction> belongsToUser(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Transaction> hasStatus(TransactionStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Transaction> merchantContains(String merchant) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("merchantName")), "%" + merchant.toLowerCase() + "%");
    }

    public static Specification<Transaction> referenceContains(String reference) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("transactionReference")), "%" + reference.toLowerCase() + "%");
    }

    public static Specification<Transaction> searchByQuery(String searchQuery) {
        return (root, query, cb) -> cb.or(
            cb.like(cb.lower(root.get("merchantName")), "%" + searchQuery.toLowerCase() + "%"),
            cb.like(cb.lower(root.get("transactionReference")), "%" + searchQuery.toLowerCase() + "%")
        );
    }

    public static Specification<Transaction> createdAfter(LocalDateTime from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<Transaction> createdBefore(LocalDateTime to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    public static Specification<Transaction> amountGreaterThanOrEqual(BigDecimal minAmount) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), minAmount);
    }

    public static Specification<Transaction> amountLessThanOrEqual(BigDecimal maxAmount) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("amount"), maxAmount);
    }
}
