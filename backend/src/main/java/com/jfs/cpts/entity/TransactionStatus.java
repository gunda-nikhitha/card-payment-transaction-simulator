package com.jfs.cpts.entity;

/**
 * Transaction status state machine.
 * Valid transitions: RECEIVED → PROCESSING → APPROVED | DECLINED | TIMEOUT
 */
public enum TransactionStatus {
    RECEIVED,
    PROCESSING,
    APPROVED,
    DECLINED,
    TIMEOUT;

    /**
     * Check if this status is a terminal state (no further transitions allowed).
     */
    public boolean isTerminal() {
        return this == APPROVED || this == DECLINED || this == TIMEOUT;
    }

    /**
     * Check if a transition from this status to the target is valid.
     */
    public boolean canTransitionTo(TransactionStatus target) {
        return switch (this) {
            case RECEIVED -> target == PROCESSING;
            case PROCESSING -> target == APPROVED || target == DECLINED || target == TIMEOUT;
            default -> false; // Terminal states cannot transition
        };
    }
}
