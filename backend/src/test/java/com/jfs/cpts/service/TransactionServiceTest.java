package com.jfs.cpts.service;

import com.jfs.cpts.dto.transaction.TransactionRequest;
import com.jfs.cpts.dto.transaction.TransactionResponse;
import com.jfs.cpts.entity.Role;
import com.jfs.cpts.entity.TransactionStatus;
import com.jfs.cpts.entity.User;
import com.jfs.cpts.exception.ResourceNotFoundException;
import com.jfs.cpts.repository.TransactionRepository;
import com.jfs.cpts.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        userRepository.deleteAll();

        testUser1 = new User();
        testUser1.setName("User One");
        testUser1.setEmail("user1@example.com");
        testUser1.setPasswordHash("hashed_secret");
        testUser1.setRole(Role.USER);
        testUser1.setActive(true);
        testUser1 = userRepository.save(testUser1);

        testUser2 = new User();
        testUser2.setName("User Two");
        testUser2.setEmail("user2@example.com");
        testUser2.setPasswordHash("hashed_secret");
        testUser2.setRole(Role.USER);
        testUser2.setActive(true);
        testUser2 = userRepository.save(testUser2);
    }

    @Test
    @DisplayName("Should successfully simulate transaction and persist in database")
    void shouldSimulateAndPersistTransaction() {
        TransactionRequest req = new TransactionRequest(
                "Swiggy Foods",
                new BigDecimal("650.00"),
                "INR",
                "USER ONE",
                "**** **** **** 1111",
                "Dinner order",
                UUID.randomUUID().toString()
        );

        TransactionResponse res = transactionService.simulateTransaction(req, testUser1);

        assertNotNull(res);
        assertNotNull(res.id());
        assertNotNull(res.transactionReference());
        assertEquals("Swiggy Foods", res.merchantName());
        assertEquals(TransactionStatus.APPROVED.name(), res.status());
        assertEquals("TEST_APPROVED", res.reasonCode());
        assertEquals(1, transactionRepository.count());
    }

    @Test
    @DisplayName("Idempotency: Repeated request with identical key returns cached transaction without creating duplicate")
    void shouldEnforceIdempotency() {
        String idemKey = "idem-" + UUID.randomUUID();
        TransactionRequest req = new TransactionRequest(
                "Netflix India",
                new BigDecimal("499.00"),
                "INR",
                "USER ONE",
                "**** **** **** 1111",
                "Subscription",
                idemKey
        );

        TransactionResponse firstResponse = transactionService.simulateTransaction(req, testUser1);
        long countAfterFirst = transactionRepository.count();

        TransactionResponse secondResponse = transactionService.simulateTransaction(req, testUser1);
        long countAfterSecond = transactionRepository.count();

        assertEquals(countAfterFirst, countAfterSecond, "Duplicate transaction was created despite same idempotency key");
        assertEquals(firstResponse.id(), secondResponse.id());
        assertEquals(firstResponse.transactionReference(), secondResponse.transactionReference());
    }

    @Test
    @DisplayName("Security & Isolation: User cannot inspect another user's transaction by ID")
    void shouldPreventCrossUserAccess() {
        TransactionRequest req = new TransactionRequest(
                "Apple Store",
                new BigDecimal("89900.00"),
                "INR",
                "USER ONE",
                "**** **** **** 1111",
                "Phone",
                UUID.randomUUID().toString()
        );

        TransactionResponse user1Txn = transactionService.simulateTransaction(req, testUser1);

        // User 1 should access fine
        assertDoesNotThrow(() -> transactionService.getUserTransactionById(user1Txn.id(), testUser1.getId()));

        // User 2 trying to access User 1's transaction must throw ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () ->
                transactionService.getUserTransactionById(user1Txn.id(), testUser2.getId())
        );
    }

    @Test
    @DisplayName("Should reject unsupported currency")
    void shouldRejectUnsupportedCurrency() {
        TransactionRequest req = new TransactionRequest(
                "Amazon Global",
                new BigDecimal("100.00"),
                "XYZ", // Invalid currency
                "USER ONE",
                "**** **** **** 1111",
                "Invalid Currency",
                UUID.randomUUID().toString()
        );

        assertThrows(IllegalArgumentException.class, () ->
                transactionService.simulateTransaction(req, testUser1)
        );
    }
}
