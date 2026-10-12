package com.jfs.cpts.service;

import com.jfs.cpts.entity.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SimulationEngineTest {

    private SimulationEngine simulationEngine;

    @BeforeEach
    void setUp() {
        simulationEngine = new SimulationEngine();
        ReflectionTestUtils.setField(simulationEngine, "approveProbability", 0.75);
        ReflectionTestUtils.setField(simulationEngine, "declineProbability", 0.20);
        ReflectionTestUtils.setField(simulationEngine, "processingDelayMs", 0L);
        ReflectionTestUtils.setField(simulationEngine, "maxAmount", new BigDecimal("1000000.00"));
    }

    @Test
    @DisplayName("Should generate valid transaction reference matching TXN-YYYY-XXXXXXXX format")
    void shouldGenerateValidTransactionReference() {
        String ref = simulationEngine.generateTransactionReference();
        assertNotNull(ref);
        assertTrue(ref.matches("^TXN-\\d{4}-[A-Z0-9]{8}$"), "Reference does not match format: " + ref);
    }

    @Test
    @DisplayName("Card ending in 0000 should deterministically DECLINE")
    void shouldDeclineCardEndingIn0000() {
        SimulationEngine.SimulationResult result = simulationEngine.simulate(
                new BigDecimal("500.00"), "Test Merchant", "**** **** **** 0000"
        );
        assertEquals(TransactionStatus.DECLINED, result.status());
        assertEquals("TEST_DECLINE", result.reasonCode());
    }

    @Test
    @DisplayName("Card ending in 9999 should deterministically TIMEOUT")
    void shouldTimeoutCardEndingIn9999() {
        SimulationEngine.SimulationResult result = simulationEngine.simulate(
                new BigDecimal("500.00"), "Test Merchant", "**** **** **** 9999"
        );
        assertEquals(TransactionStatus.TIMEOUT, result.status());
        assertEquals("TEST_TIMEOUT", result.reasonCode());
    }

    @Test
    @DisplayName("Card ending in 1111 should deterministically APPROVE")
    void shouldApproveCardEndingIn1111() {
        SimulationEngine.SimulationResult result = simulationEngine.simulate(
                new BigDecimal("500.00"), "Test Merchant", "**** **** **** 1111"
        );
        assertEquals(TransactionStatus.APPROVED, result.status());
        assertEquals("TEST_APPROVED", result.reasonCode());
    }

    @Test
    @DisplayName("Amount exceeding maximum limit should DECLINE")
    void shouldDeclineAmountExceedingLimit() {
        SimulationEngine.SimulationResult result = simulationEngine.simulate(
                new BigDecimal("1000000.01"), "Test Merchant", "**** **** **** 4321"
        );
        assertEquals(TransactionStatus.DECLINED, result.status());
        assertEquals("AMOUNT_EXCEEDED", result.reasonCode());
    }

    @Test
    @DisplayName("Amount ending in .99 should trigger DECLINE scenario")
    void shouldDeclineAmountEndingIn99() {
        SimulationEngine.SimulationResult result = simulationEngine.simulate(
                new BigDecimal("49.99"), "Test Merchant", "**** **** **** 5678"
        );
        assertEquals(TransactionStatus.DECLINED, result.status());
        assertEquals("AMOUNT_TRIGGER_DECLINE", result.reasonCode());
    }
}
