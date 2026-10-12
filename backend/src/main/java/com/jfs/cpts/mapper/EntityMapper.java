package com.jfs.cpts.mapper;

import com.jfs.cpts.dto.admin.UserManagementResponse;
import com.jfs.cpts.dto.ai.ConversationResponse;
import com.jfs.cpts.dto.auth.AuthResponse;
import com.jfs.cpts.dto.dashboard.DashboardSummary;
import com.jfs.cpts.dto.transaction.TransactionResponse;
import com.jfs.cpts.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Centralized mapper for entity-to-DTO conversions.
 */
@Component
public class EntityMapper {

    public TransactionResponse toTransactionResponse(Transaction t) {
        return new TransactionResponse(
            t.getId(),
            t.getTransactionReference(),
            t.getMerchantName(),
            t.getAmount(),
            t.getCurrency(),
            t.getMaskedCardSuffix(),
            t.getCardholderName(),
            t.getStatus().name(),
            t.getReasonCode(),
            t.getStatusMessage(),
            t.getDescription(),
            t.getProcessingDurationMs(),
            t.getCreatedAt(),
            t.getProcessedAt()
        );
    }

    public AuthResponse.UserInfo toUserInfo(User user) {
        return new AuthResponse.UserInfo(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name(),
            user.isActive()
        );
    }

    public UserManagementResponse toUserManagementResponse(User user, long transactionCount) {
        return new UserManagementResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name(),
            user.isActive(),
            transactionCount,
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    public DashboardSummary.RecentTransaction toRecentTransaction(Transaction t) {
        return new DashboardSummary.RecentTransaction(
            t.getTransactionReference(),
            t.getMerchantName(),
            t.getAmount(),
            t.getCurrency(),
            t.getStatus().name(),
            t.getCreatedAt().toString()
        );
    }

    public ConversationResponse toConversationResponse(AiConversation conv, boolean includeMessages) {
        List<ConversationResponse.MessageResponse> messages = null;
        if (includeMessages && conv.getMessages() != null) {
            messages = conv.getMessages().stream()
                .map(m -> new ConversationResponse.MessageResponse(
                    m.getId(), m.getRole(), m.getContent(), m.getCreatedAt()
                ))
                .toList();
        }
        return new ConversationResponse(
            conv.getId(), conv.getTitle(), conv.getCreatedAt(), conv.getUpdatedAt(), messages
        );
    }
}
