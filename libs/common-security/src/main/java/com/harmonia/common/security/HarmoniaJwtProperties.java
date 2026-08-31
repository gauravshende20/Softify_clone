package com.harmonia.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "harmonia.security.jwt")
public class HarmoniaJwtProperties {

    /**
     * HMAC secret used to sign and verify JWTs. Must be at least 256 bits.
     * Injected from environment variable JWT_SECRET. Never log this value.
     */
    private String secret = "local-dev-only-change-me-use-env-jwt-secret-32b";
    private String issuer = "harmonia-auth";
    private long accessTokenMinutes = 15;
    private long refreshTokenDays = 7;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public long getAccessTokenMinutes() {
        return accessTokenMinutes;
    }

    public void setAccessTokenMinutes(long accessTokenMinutes) {
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public long getRefreshTokenDays() {
        return refreshTokenDays;
    }

    public void setRefreshTokenDays(long refreshTokenDays) {
        this.refreshTokenDays = refreshTokenDays;
    }
}
