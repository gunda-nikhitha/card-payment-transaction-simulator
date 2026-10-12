package com.jfs.cpts.service;

import com.jfs.cpts.dto.transaction.TransactionRequest;
import com.jfs.cpts.dto.transaction.TransactionResponse;
import com.jfs.cpts.entity.Transaction;
import com.jfs.cpts.entity.TransactionStatus;
import com.jfs.cpts.entity.User;
import com.jfs.cpts.exception.ResourceNotFoundException;
import com.jfs.cpts.mapper.EntityMapper;
import com.jfs.cpts.repository.TransactionRepository;
import com.jfs.cpts.repository.TransactionSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Transaction service handling simulation, persistence, and querying.
 */
@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    // Whitelisted sort fields to prevent injection
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "createdAt", "amount", "merchantName", "status", "transactionReference"
    );

    private final TransactionRepository transactionRepository;
    private final SimulationEngine simulationEngine;
    private final EntityMapper mapper;

    @Value("${app.simulation.allowed-currencies:INR,USD,EUR,GBP}")
    private String allowedCurrencies;

    @Value("${app.simulation.default-currency:INR}")
    private String defaultCurrency;

    public TransactionService(TransactionRepository transactionRepository,
                               SimulationEngine simulationEngine,
                               EntityMapper mapper) {
        this.transactionRepository = transactionRepository;
        this.simulationEngine = simulationEngine;
        this.mapper = mapper;
    }

    /**
     * Simulate a transaction with idempotency support.
     */
    @Transactional
    public TransactionResponse simulateTransaction(TransactionRequest request, User user) {
        // Idempotency check
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            Optional<Transaction> existing = transactionRepository
                .findByIdempotencyKeyAndUserId(request.idempotencyKey(), user.getId());
            if (existing.isPresent()) {
                log.info("Idempotent request detected for key: {}", request.idempotencyKey());
                return mapper.toTransactionResponse(existing.get());
            }
        }

        // Validate currency
        String currency = request.currency() != null ? request.currency().toUpperCase() : defaultCurrency;
        List<String> allowed = Arrays.asList(allowedCurrencies.split(","));
        if (!allowed.contains(currency)) {
            throw new IllegalArgumentException("Unsupported currency: " + currency + ". Allowed: " + allowedCurrencies);
        }

        // Create transaction in RECEIVED state
        Transaction transaction = new Transaction();
        transaction.setTransactionReference(simulationEngine.generateTransactionReference());
        transaction.setMerchantName(request.merchantName().trim());
        transaction.setAmount(request.amount());
        transaction.setCurrency(currency);
        transaction.setMaskedCardSuffix(request.maskedCardSuffix());
        transaction.setCardholderName(request.cardholderName());
        String cleanIdempotencyKey = (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) 
                ? request.idempotencyKey().trim() : null;
        transaction.setIdempotencyKey(cleanIdempotencyKey);
        transaction.setUser(user);
        transaction.setStatus(TransactionStatus.RECEIVED);

        // Save in RECEIVED state
        transaction = transactionRepository.save(transaction);

        // Transition to PROCESSING
        transaction.transitionTo(TransactionStatus.PROCESSING);
        transaction = transactionRepository.save(transaction);

        // Run simulation
        SimulationEngine.SimulationResult result = simulationEngine.simulate(
            request.amount(), request.merchantName(), request.maskedCardSuffix()
        );

        // Apply result
        transaction.transitionTo(result.status());
        transaction.setReasonCode(result.reasonCode());
        transaction.setStatusMessage(result.statusMessage());
        transaction.setProcessingDurationMs(result.processingDurationMs());
        transaction.setProcessedAt(result.processedAt());

        transaction = transactionRepository.save(transaction);
        log.info("Transaction {} completed with status: {}", transaction.getTransactionReference(), result.status());

        return mapper.toTransactionResponse(transaction);
    }

    /**
     * Get paginated, filtered transactions for a user.
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserTransactions(Long userId, String search,
            String status, LocalDateTime fromDate, LocalDateTime toDate,
            BigDecimal minAmount, BigDecimal maxAmount,
            String sortBy, String sortDir, int page, int size) {

        Specification<Transaction> spec = Specification.where(TransactionSpecifications.belongsToUser(userId));

        if (search != null && !search.isBlank()) {
            spec = spec.and(TransactionSpecifications.searchByQuery(search.trim()));
        }
        if (status != null && !status.isBlank()) {
            try {
                spec = spec.and(TransactionSpecifications.hasStatus(TransactionStatus.valueOf(status.toUpperCase())));
            } catch (IllegalArgumentException e) {
                // Invalid status filter — ignore
            }
        }
        if (fromDate != null) {
            spec = spec.and(TransactionSpecifications.createdAfter(fromDate));
        }
        if (toDate != null) {
            spec = spec.and(TransactionSpecifications.createdBefore(toDate));
        }
        if (minAmount != null) {
            spec = spec.and(TransactionSpecifications.amountGreaterThanOrEqual(minAmount));
        }
        if (maxAmount != null) {
            spec = spec.and(TransactionSpecifications.amountLessThanOrEqual(maxAmount));
        }

        String safeSortField = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(direction, safeSortField));

        return transactionRepository.findAll(spec, pageable).map(mapper::toTransactionResponse);
    }

    /**
     * Get a single transaction by ID, enforcing user ownership.
     */
    @Transactional(readOnly = true)
    public TransactionResponse getUserTransactionById(Long transactionId, Long userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));
        return mapper.toTransactionResponse(transaction);
    }

    /**
     * Get transaction by reference, enforcing user ownership.
     */
    @Transactional(readOnly = true)
    public TransactionResponse getUserTransactionByReference(String reference, Long userId) {
        Transaction transaction = transactionRepository.findByTransactionReference(reference)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", "reference", reference));
        if (!transaction.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Transaction", "reference", reference);
        }
        return mapper.toTransactionResponse(transaction);
    }

    /**
     * Admin: Get all transactions (paginated, filtered).
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllTransactions(String search, String status,
            LocalDateTime fromDate, LocalDateTime toDate,
            BigDecimal minAmount, BigDecimal maxAmount,
            String sortBy, String sortDir, int page, int size) {

        Specification<Transaction> spec = Specification.where(null);

        if (search != null && !search.isBlank()) {
            spec = spec.and(TransactionSpecifications.searchByQuery(search.trim()));
        }
        if (status != null && !status.isBlank()) {
            try {
                spec = spec.and(TransactionSpecifications.hasStatus(TransactionStatus.valueOf(status.toUpperCase())));
            } catch (IllegalArgumentException e) {
                // Invalid status filter — ignore
            }
        }
        if (fromDate != null) { spec = spec.and(TransactionSpecifications.createdAfter(fromDate)); }
        if (toDate != null) { spec = spec.and(TransactionSpecifications.createdBefore(toDate)); }
        if (minAmount != null) { spec = spec.and(TransactionSpecifications.amountGreaterThanOrEqual(minAmount)); }
        if (maxAmount != null) { spec = spec.and(TransactionSpecifications.amountLessThanOrEqual(maxAmount)); }

        String safeSortField = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(direction, safeSortField));

        return transactionRepository.findAll(spec, pageable).map(mapper::toTransactionResponse);
    }

    /**
     * Export user transactions as CSV data.
     */
    @Transactional(readOnly = true)
    public String exportTransactionsCsv(Long userId, String search, String status,
            LocalDateTime fromDate, LocalDateTime toDate) {
        Specification<Transaction> spec = Specification.where(TransactionSpecifications.belongsToUser(userId));

        if (search != null && !search.isBlank()) {
            spec = spec.and(TransactionSpecifications.searchByQuery(search.trim()));
        }
        if (status != null && !status.isBlank()) {
            try {
                spec = spec.and(TransactionSpecifications.hasStatus(TransactionStatus.valueOf(status.toUpperCase())));
            } catch (IllegalArgumentException e) { /* ignore */ }
        }
        if (fromDate != null) { spec = spec.and(TransactionSpecifications.createdAfter(fromDate)); }
        if (toDate != null) { spec = spec.and(TransactionSpecifications.createdBefore(toDate)); }

        List<Transaction> transactions = transactionRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));

        StringBuilder csv = new StringBuilder();
        csv.append("Reference,Merchant,Amount,Currency,Status,Reason,Date\n");
        for (Transaction t : transactions) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s\n",
                escapeCsv(t.getTransactionReference()),
                escapeCsv(t.getMerchantName()),
                t.getAmount().toPlainString(),
                t.getCurrency(),
                t.getStatus().name(),
                escapeCsv(t.getReasonCode() != null ? t.getReasonCode() : ""),
                t.getCreatedAt().toString()
            ));
        }
        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
