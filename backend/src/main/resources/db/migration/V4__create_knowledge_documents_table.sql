-- =============================================
-- V4__create_knowledge_documents_table.sql
-- RAG knowledge base for payment learning
-- =============================================

CREATE TABLE knowledge_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    source_type VARCHAR(50) NOT NULL DEFAULT 'INTERNAL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_knowledge_category (category),
    INDEX idx_knowledge_source_type (source_type),
    FULLTEXT INDEX idx_knowledge_fulltext (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
