package com.jfs.cpts.dto.transaction;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Request DTO for simulating a payment transaction.
 */
public record TransactionRequest(
    @NotBlank(message = "Merchant name is required")
    @Size(max = 200, message = "Merchant name must not exceed 200 characters")
    String merchantName,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @DecimalMax(value = "1000000.00", message = "Amount exceeds simulation limit")
    BigDecimal amount,

    @Size(max = 3, message = "Currency code must be 3 characters")
    String currency,

    @Size(max = 100, message = "Cardholder name must not exceed 100 characters")
    String cardholderName,

    @Pattern(regexp = "^(\\*{4}\\s?\\*{4}\\s?\\*{4}\\s?\\d{4}|\\d{4})$",
             message = "Card suffix must be in format '**** **** **** 1234' or '1234'")
    String maskedCardSuffix,

    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,

    @Size(max = 64, message = "Idempotency key must not exceed 64 characters")
    String idempotencyKey
) {}
