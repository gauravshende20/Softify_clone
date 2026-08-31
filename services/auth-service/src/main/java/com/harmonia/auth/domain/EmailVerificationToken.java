package com.harmonia.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken extends HashedToken {

    @Column(nullable = false)
    private boolean used = false;

    protected EmailVerificationToken() {
    }

    public EmailVerificationToken(Account account, String tokenHash, Instant expiresAt) {
        super(account, tokenHash, expiresAt);
    }

    public boolean isUsed() {
        return used;
    }

    public void markUsed() {
        this.used = true;
    }
}
