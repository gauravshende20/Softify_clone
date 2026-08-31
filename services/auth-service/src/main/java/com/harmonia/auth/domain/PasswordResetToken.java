package com.harmonia.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken extends HashedToken {

    @Column(nullable = false)
    private boolean used = false;

    protected PasswordResetToken() {
    }

    public PasswordResetToken(Account account, String tokenHash, Instant expiresAt) {
        super(account, tokenHash, expiresAt);
    }

    public boolean isUsed() {
        return used;
    }

    public void markUsed() {
        this.used = true;
    }
}
