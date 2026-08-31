package com.harmonia.auth.security;

import com.harmonia.auth.domain.Account;
import com.harmonia.common.security.HarmoniaJwtProperties;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class JwtTokenService {

    private final HarmoniaJwtProperties properties;

    public JwtTokenService(HarmoniaJwtProperties properties) {
        this.properties = properties;
    }

    public String issueAccessToken(Account account) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.getAccessTokenMinutes() * 60);
        String roles = account.getRoles().stream()
                .map(r -> r.getRole())
                .collect(Collectors.joining(","));
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(account.getId().toString())
                .issuer(properties.getIssuer())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .claim("email", account.getEmail())
                .claim("roles", roles)
                .build();
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(properties.getSecret().getBytes()));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to sign access token", e);
        }
    }

    public long accessTokenExpiresInSeconds() {
        return properties.getAccessTokenMinutes() * 60;
    }
}
