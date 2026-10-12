package com.jfs.cpts.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "a_very_long_test_secret_key_that_exceeds_sixty_four_bytes_for_hs512_signing_algorithm_verification_1234567890";
    private final long expirationMs = 3600000;
    private final String issuer = "test-issuer";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret, expirationMs, issuer);
    }

    @Test
    @DisplayName("Should generate valid token and extract email and user ID correctly")
    void shouldGenerateAndExtractClaims() {
        Long userId = 42L;
        String email = "john.doe@example.com";
        String role = "USER";

        String token = jwtTokenProvider.generateToken(userId, email, role);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(email, jwtTokenProvider.getEmailFromToken(token));
        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("Should reject invalid or tampered token")
    void shouldRejectInvalidToken() {
        String invalidToken = "invalid.token.structure";
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    @DisplayName("Should reject expired token")
    void shouldRejectExpiredToken() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider(secret, -1000, issuer);
        String token = expiredProvider.generateToken(1L, "expired@example.com", "USER");
        assertFalse(jwtTokenProvider.validateToken(token));
    }
}
