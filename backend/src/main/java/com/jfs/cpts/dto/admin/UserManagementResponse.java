package com.jfs.cpts.dto.admin;

import java.time.LocalDateTime;

/**
 * User management response DTO for admin views.
 * Never includes password hash or sensitive security data.
 */
public record UserManagementResponse(
    Long id,
    String name,
    String email,
    String role,
    boolean active,
    long transactionCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
