package com.jfs.cpts.dto.ai;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Conversation list/detail DTO.
 */
public record ConversationResponse(
    Long id,
    String title,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<MessageResponse> messages
) {
    public record MessageResponse(
        Long id,
        String role,
        String content,
        LocalDateTime createdAt
    ) {}
}
