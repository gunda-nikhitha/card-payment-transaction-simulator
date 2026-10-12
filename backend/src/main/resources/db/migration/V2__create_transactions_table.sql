-- =============================================
-- V2__create_transactions_table.sql
-- Payment transaction records
-- =============================================

CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_reference VARCHAR(30) NOT NULL,
    merchant_name VARCHAR(200) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    masked_card_suffix VARCHAR(20),
    cardholder_name VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    reason_code VARCHAR(50),
    status_message VARCHAR(500),
    description VARCHAR(500),
    idempotency_key VARCHAR(64),
    processing_duration_ms BIGINT,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    CONSTRAINT uk_transactions_reference UNIQUE (transaction_reference),
    CONSTRAINT uk_transactions_idempotency UNIQUE (idempotency_key),
    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_transactions_user_id (user_id),
    INDEX idx_transactions_status (status),
    INDEX idx_transactions_created_at (created_at),
    INDEX idx_transactions_merchant (merchant_name),
    INDEX idx_transactions_amount (amount),
    INDEX idx_transactions_reference (transaction_reference)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
