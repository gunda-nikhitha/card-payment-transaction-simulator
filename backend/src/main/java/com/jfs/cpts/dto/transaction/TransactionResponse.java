package com.jfs.cpts.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction response DTO — safe projection of the transaction entity.
 */
public record TransactionResponse(
    Long id,
    String transactionReference,
    String merchantName,
    BigDecimal amount,
    String currency,
    String maskedCardSuffix,
    String cardholderName,
    String status,
    String reasonCode,
    String statusMessage,
    String description,
    Long processingDurationMs,
    LocalDateTime createdAt,
    LocalDateTime processedAt
) {}
