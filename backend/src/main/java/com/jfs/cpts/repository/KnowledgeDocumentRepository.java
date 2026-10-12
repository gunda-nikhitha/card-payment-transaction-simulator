package com.jfs.cpts.repository;

import com.jfs.cpts.entity.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    List<KnowledgeDocument> findByCategory(String category);

    /**
     * MySQL FULLTEXT search for RAG retrieval.
     * Returns documents matching the search terms, ordered by relevance.
     */
    @Query(value = "SELECT *, MATCH(title, content) AGAINST (:query IN NATURAL LANGUAGE MODE) AS relevance " +
                   "FROM knowledge_documents " +
                   "WHERE MATCH(title, content) AGAINST (:query IN NATURAL LANGUAGE MODE) " +
                   "ORDER BY relevance DESC LIMIT :limit",
           nativeQuery = true)
    List<KnowledgeDocument> searchByRelevance(@Param("query") String query, @Param("limit") int limit);

    /**
     * Fallback keyword search when fulltext doesn't return results.
     */
    @Query("SELECT k FROM KnowledgeDocument k WHERE " +
           "LOWER(k.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(k.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<KnowledgeDocument> searchByKeyword(@Param("keyword") String keyword);
}
