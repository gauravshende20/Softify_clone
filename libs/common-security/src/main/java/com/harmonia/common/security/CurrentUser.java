package com.harmonia.common.security;

import java.util.Set;
import java.util.UUID;

public record CurrentUser(UUID id, String email, Set<String> roles) {

    public boolean hasRole(String role) {
        return roles.contains(role) || roles.contains("ROLE_" + role);
    }
}
