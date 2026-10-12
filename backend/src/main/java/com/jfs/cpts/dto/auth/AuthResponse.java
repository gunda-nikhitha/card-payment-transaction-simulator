package com.jfs.cpts.dto.auth;

/**
 * Authentication response with JWT token and user info.
 */
public record AuthResponse(
    String token,
    String type,
    UserInfo user
) {
    public AuthResponse(String token, UserInfo user) {
        this(token, "Bearer", user);
    }

    public record UserInfo(
        Long id,
        String name,
        String email,
        String role,
        boolean active
    ) {}
}
