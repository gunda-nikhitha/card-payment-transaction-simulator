package com.jfs.cpts.security;

import com.jfs.cpts.exception.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lightweight in-memory sliding-window rate limiter for protecting key actions
 * (login brute force, spam transaction simulation, AI requests).
 */
@Service
public class RateLimiterService {

    @Value("${app.rate-limit.login-attempts-per-minute:10}")
    private int maxLoginAttemptsPerMinute;

    @Value("${app.rate-limit.transaction-per-minute:20}")
    private int maxTransactionsPerMinute;

    @Value("${app.rate-limit.ai-requests-per-minute:10}")
    private int maxAiRequestsPerMinute;

    private static class WindowCounter {
        long windowStartMs;
        AtomicInteger count;

        WindowCounter(long start) {
            this.windowStartMs = start;
            this.count = new AtomicInteger(1);
        }
    }

    private final Map<String, WindowCounter> requestCounts = new ConcurrentHashMap<>();

    public void checkLoginRateLimit(String clientIdentifier) {
        checkLimit("LOGIN:" + clientIdentifier, maxLoginAttemptsPerMinute, "Too many login attempts. Please wait 1 minute.");
    }

    public void checkTransactionRateLimit(String clientIdentifier) {
        checkLimit("TXN:" + clientIdentifier, maxTransactionsPerMinute, "Transaction rate limit exceeded. Please wait a moment.");
    }

    public void checkAiRateLimit(String clientIdentifier) {
        checkLimit("AI:" + clientIdentifier, maxAiRequestsPerMinute, "AI request rate limit exceeded. Please wait a moment.");
    }

    private void checkLimit(String key, int maxRequests, String errorMessage) {
        long now = System.currentTimeMillis();
        long windowLengthMs = 60_000L; // 1 minute

        requestCounts.compute(key, (k, counter) -> {
            if (counter == null || now - counter.windowStartMs > windowLengthMs) {
                return new WindowCounter(now);
            }
            if (counter.count.incrementAndGet() > maxRequests) {
                throw new RateLimitExceededException(errorMessage);
            }
            return counter;
        });

        // Periodic cleanup when map grows
        if (requestCounts.size() > 5000) {
            requestCounts.entrySet().removeIf(e -> now - e.getValue().windowStartMs > windowLengthMs);
        }
    }
}
