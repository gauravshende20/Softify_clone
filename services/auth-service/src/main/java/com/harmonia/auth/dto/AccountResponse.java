package com.harmonia.auth.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String email,
        boolean enabled,
        boolean emailVerified,
        Set<String> roles,
        Instant createdAt
) {
}
