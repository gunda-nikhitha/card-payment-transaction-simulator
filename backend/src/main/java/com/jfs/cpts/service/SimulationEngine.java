package com.jfs.cpts.service;

import com.jfs.cpts.entity.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;

/**
 * Simulation engine that determines transaction outcomes.
 * This is NOT a real payment gateway — all results are simulated for educational purposes.
 */
@Service
public class SimulationEngine {

    private static final Logger log = LoggerFactory.getLogger(SimulationEngine.class);
    private final SecureRandom random = new SecureRandom();

    @Value("${app.simulation.approve-probability:0.75}")
    private double approveProbability;

    @Value("${app.simulation.decline-probability:0.20}")
    private double declineProbability;

    @Value("${app.simulation.processing-delay-ms:500}")
    private long processingDelayMs;

    @Value("${app.simulation.max-amount:1000000.00}")
    private BigDecimal maxAmount;

    /**
     * Generate a unique transaction reference: TXN-YYYY-XXXXXXXX
     */
    public String generateTransactionReference() {
        String year = String.valueOf(Year.now().getValue());
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "TXN-" + year + "-" + suffix;
    }

    /**
     * Simulate the payment processing and return the result.
     * Uses configurable probabilities with deterministic test scenarios.
     */
    public SimulationResult simulate(BigDecimal amount, String merchantName, String maskedCardSuffix) {
        long startTime = System.currentTimeMillis();

        // Deterministic test scenarios based on card suffix
        if (maskedCardSuffix != null) {
            String suffix = maskedCardSuffix.replaceAll("[^0-9]", "");
            if (suffix.endsWith("0000")) {
                return buildResult(TransactionStatus.DECLINED, "TEST_DECLINE",
                    "Simulated decline: Test card ending in 0000", startTime);
            }
            if (suffix.endsWith("9999")) {
                return buildResult(TransactionStatus.TIMEOUT, "TEST_TIMEOUT",
                    "Simulated timeout: Test card ending in 9999", startTime);
            }
            if (suffix.endsWith("1111")) {
                return buildResult(TransactionStatus.APPROVED, "TEST_APPROVED",
                    "Simulated approval: Test card ending in 1111", startTime);
            }
        }

        // Deterministic test scenarios based on amount
        if (amount.compareTo(maxAmount) > 0) {
            return buildResult(TransactionStatus.DECLINED, "AMOUNT_EXCEEDED",
                "Simulated decline: Amount exceeds simulation limit", startTime);
        }

        // Amount-based test triggers
        if (amount.stripTrailingZeros().toPlainString().endsWith(".99")) {
            return buildResult(TransactionStatus.DECLINED, "AMOUNT_TRIGGER_DECLINE",
                "Simulated decline: Amount ending in .99 triggers decline scenario", startTime);
        }

        // Probabilistic outcome
        double roll = random.nextDouble();
        if (roll < approveProbability) {
            return buildResult(TransactionStatus.APPROVED, "SIM_APPROVED",
                "Transaction approved (simulated)", startTime);
        } else if (roll < approveProbability + declineProbability) {
            String[] declineReasons = {
                "Insufficient simulated funds",
                "Simulated fraud detection triggered",
                "Simulated card restriction",
                "Simulated daily limit exceeded"
            };
            String reason = declineReasons[random.nextInt(declineReasons.length)];
            return buildResult(TransactionStatus.DECLINED, "SIM_DECLINED", reason, startTime);
        } else {
            return buildResult(TransactionStatus.TIMEOUT, "SIM_TIMEOUT",
                "Simulated processing timeout", startTime);
        }
    }

    private SimulationResult buildResult(TransactionStatus status, String reasonCode,
                                          String statusMessage, long startTime) {
        // Simulate processing delay
        try {
            Thread.sleep(Math.min(processingDelayMs, 2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Simulation result: status={}, reasonCode={}, duration={}ms", status, reasonCode, duration);

        return new SimulationResult(status, reasonCode, statusMessage, duration, LocalDateTime.now());
    }

    /**
     * Immutable simulation result.
     */
    public record SimulationResult(
        TransactionStatus status,
        String reasonCode,
        String statusMessage,
        long processingDurationMs,
        LocalDateTime processedAt
    ) {}
}
