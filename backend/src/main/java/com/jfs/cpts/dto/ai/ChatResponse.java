package com.jfs.cpts.dto.ai;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Chat response DTO.
 */
public record ChatResponse(
    Long conversationId,
    String conversationTitle,
    String reply,
    List<String> sources,
    LocalDateTime timestamp
) {}
