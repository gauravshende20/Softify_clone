package com.harmonia.auth.dto;

import java.util.UUID;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UUID userId,
        String email
) {
    public static TokenResponse of(String access, String refresh, long expiresIn, UUID userId, String email) {
        return new TokenResponse(access, refresh, "Bearer", expiresIn, userId, email);
    }
}
