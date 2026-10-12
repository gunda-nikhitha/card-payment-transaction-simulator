package com.jfs.cpts.service;

import com.jfs.cpts.entity.KnowledgeDocument;
import com.jfs.cpts.repository.KnowledgeDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for retrieving relevant educational knowledge documents for RAG.
 * Supports MySQL FULLTEXT search with robust in-memory / keyword fallback for H2 testing.
 */
@Service
public class KnowledgeRetrievalService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeRetrievalService.class);

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;

    @Value("${app.ai.max-context-documents:5}")
    private int maxContextDocuments;

    public KnowledgeRetrievalService(KnowledgeDocumentRepository knowledgeDocumentRepository) {
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
    }

    /**
     * Retrieve relevant documents for a given query string.
     */
    @Transactional(readOnly = true)
    public List<KnowledgeDocument> retrieveRelevantDocuments(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        String cleanQuery = query.trim();
        List<KnowledgeDocument> results = new ArrayList<>();

        // 1. Attempt MySQL FULLTEXT query
        try {
            results = knowledgeDocumentRepository.searchByRelevance(cleanQuery, maxContextDocuments);
        } catch (Exception e) {
            log.debug("Fulltext search unavailable or failed (expected in H2 or short terms): {}", e.getMessage());
        }

        // 2. If fulltext returned results, return them
        if (results != null && !results.isEmpty()) {
            return results;
        }

        // 3. Fallback: Keyword search by splitting query into meaningful tokens
        Set<Long> seenIds = new HashSet<>();
        List<KnowledgeDocument> fallbackResults = new ArrayList<>();

        String[] tokens = cleanQuery.split("[\\s,?.!;:]+");
        for (String token : tokens) {
            String sanitized = token.trim();
            if (sanitized.length() >= 3 && !isStopWord(sanitized)) {
                try {
                    List<KnowledgeDocument> matched = knowledgeDocumentRepository.searchByKeyword(sanitized);
                    for (KnowledgeDocument doc : matched) {
                        if (seenIds.add(doc.getId())) {
                            fallbackResults.add(doc);
                            if (fallbackResults.size() >= maxContextDocuments) {
                                return fallbackResults;
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.warn("Keyword search failed for token {}: {}", sanitized, ex.getMessage());
                }
            }
        }

        // 4. Default: If still empty, return top general documents (basics)
        if (fallbackResults.isEmpty()) {
            List<KnowledgeDocument> basics = knowledgeDocumentRepository.findByCategory("PAYMENT_BASICS");
            if (!basics.isEmpty()) {
                return basics.subList(0, Math.min(basics.size(), maxContextDocuments));
            }
            return knowledgeDocumentRepository.findAll().stream().limit(maxContextDocuments).toList();
        }

        return fallbackResults;
    }

    private boolean isStopWord(String word) {
        Set<String> stopWords = Set.of(
            "the", "and", "is", "are", "what", "how", "why", "who", "which",
            "can", "you", "tell", "about", "for", "with", "this", "that", "from"
        );
        return stopWords.contains(word.toLowerCase());
    }
}
