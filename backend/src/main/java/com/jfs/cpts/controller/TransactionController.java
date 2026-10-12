package com.jfs.cpts.controller;

import com.jfs.cpts.dto.transaction.TransactionRequest;
import com.jfs.cpts.dto.transaction.TransactionResponse;
import com.jfs.cpts.entity.User;
import com.jfs.cpts.security.RateLimiterService;
import com.jfs.cpts.service.TransactionService;
import com.jfs.cpts.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Simulate payment processing, inspect transaction history, and export records")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;
    private final RateLimiterService rateLimiterService;

    public TransactionController(TransactionService transactionService,
                                 UserService userService,
                                 RateLimiterService rateLimiterService) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/simulate")
    @Operation(summary = "Simulate payment transaction processing")
    public ResponseEntity<TransactionResponse> simulate(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        rateLimiterService.checkTransactionRateLimit(userDetails.getUsername());
        User user = userService.findByEmail(userDetails.getUsername());
        TransactionResponse response = transactionService.simulateTransaction(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get paginated, filtered transaction history for current user")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = userService.findByEmail(userDetails.getUsername());
        Page<TransactionResponse> result = transactionService.getUserTransactions(
                user.getId(), search, status, fromDate, toDate, minAmount, maxAmount, sortBy, sortDir, page, size
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction details by ID")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        TransactionResponse response = transactionService.getUserTransactionById(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ref/{reference}")
    @Operation(summary = "Get transaction details by reference code")
    public ResponseEntity<TransactionResponse> getTransactionByReference(
            @PathVariable String reference,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        TransactionResponse response = transactionService.getUserTransactionByReference(reference, user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Export filtered transaction history as CSV")
    public ResponseEntity<String> exportCsv(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        User user = userService.findByEmail(userDetails.getUsername());
        String csvData = transactionService.exportTransactionsCsv(user.getId(), search, status, fromDate, toDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"transactions.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }
}
