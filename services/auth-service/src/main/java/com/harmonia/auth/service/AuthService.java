package com.harmonia.auth.service;

import com.harmonia.auth.crypto.TokenHasher;
import com.harmonia.auth.domain.Account;
import com.harmonia.auth.domain.EmailVerificationToken;
import com.harmonia.auth.domain.PasswordResetToken;
import com.harmonia.auth.domain.RefreshToken;
import com.harmonia.auth.dto.AccountResponse;
import com.harmonia.auth.dto.ForgotPasswordRequest;
import com.harmonia.auth.dto.LoginRequest;
import com.harmonia.auth.dto.RefreshRequest;
import com.harmonia.auth.dto.RegisterRequest;
import com.harmonia.auth.dto.ResetPasswordRequest;
import com.harmonia.auth.dto.TokenResponse;
import com.harmonia.auth.dto.VerifyEmailRequest;
import com.harmonia.auth.mapper.AccountMapper;
import com.harmonia.auth.repo.AccountRepository;
import com.harmonia.auth.repo.EmailVerificationTokenRepository;
import com.harmonia.auth.repo.PasswordResetTokenRepository;
import com.harmonia.auth.repo.RefreshTokenRepository;
import com.harmonia.auth.security.JwtTokenService;
import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.common.api.security.Roles;
import com.harmonia.common.kafka.DomainEvent;
import com.harmonia.common.kafka.DomainEventPublisher;
import com.harmonia.common.kafka.EventType;
import com.harmonia.common.kafka.Topics;
import com.harmonia.common.security.HarmoniaJwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final Set<String> SELF_REGISTER_ROLES = Set.of(Roles.LISTENER, Roles.ARTIST);

    private final AccountRepository accounts;
    private final RefreshTokenRepository refreshTokens;
    private final EmailVerificationTokenRepository verificationTokens;
    private final PasswordResetTokenRepository resetTokens;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final TokenHasher tokenHasher;
    private final AccountMapper mapper;
    private final DomainEventPublisher events;
    private final HarmoniaJwtProperties jwtProperties;
    private final int maxFailedAttempts;
    private final Duration lockDuration;

    public AuthService(AccountRepository accounts,
                       RefreshTokenRepository refreshTokens,
                       EmailVerificationTokenRepository verificationTokens,
                       PasswordResetTokenRepository resetTokens,
                       PasswordEncoder passwordEncoder,
                       JwtTokenService jwtTokenService,
                       TokenHasher tokenHasher,
                       AccountMapper mapper,
                       DomainEventPublisher events,
                       HarmoniaJwtProperties jwtProperties,
                       @Value("${harmonia.auth.max-failed-attempts:5}") int maxFailedAttempts,
                       @Value("${harmonia.auth.lock-duration-minutes:15}") long lockMinutes) {
        this.accounts = accounts;
        this.refreshTokens = refreshTokens;
        this.verificationTokens = verificationTokens;
        this.resetTokens = resetTokens;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.tokenHasher = tokenHasher;
        this.mapper = mapper;
        this.events = events;
        this.jwtProperties = jwtProperties;
        this.maxFailedAttempts = maxFailedAttempts;
        this.lockDuration = Duration.ofMinutes(lockMinutes);
    }

    @Transactional
    public AccountResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase();
        if (accounts.existsByEmail(email)) {
            throw HarmoniaException.conflict(ErrorCode.USER_ALREADY_EXISTS, "An account with this email already exists");
        }
        if (!SELF_REGISTER_ROLES.contains(request.role())) {
            throw HarmoniaException.forbidden(ErrorCode.FORBIDDEN, "Cannot self-register with this role");
        }
        Account account = Account.create(email, passwordEncoder.encode(request.password()), request.role());
        accounts.save(account);
        String verifyRaw = tokenHasher.randomToken();
        verificationTokens.save(new EmailVerificationToken(account, tokenHasher.hash(verifyRaw), Instant.now().plus(Duration.ofHours(24))));
        events.publish(Topics.USER, DomainEvent.of(
                EventType.USER_REGISTERED, "Account", account.getId().toString(),
                "auth-service", MDC.get("traceId"), account.getId().toString(),
                Map.of("email", account.getEmail(), "role", request.role(), "verificationToken", verifyRaw)
        ));
        log.info("Registered account {}", account.getId());
        return mapper.toResponse(account);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Account account = accounts.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> HarmoniaException.unauthorized(ErrorCode.INVALID_CREDENTIALS, "Invalid credentials"));
        Instant now = Instant.now();
        if (!account.isEnabled()) {
            throw HarmoniaException.forbidden(ErrorCode.ACCOUNT_DISABLED, "Account is disabled");
        }
        if (account.isLocked(now)) {
            throw HarmoniaException.forbidden(ErrorCode.ACCOUNT_LOCKED, "Account is temporarily locked");
        }
        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            account.setFailedAttempts(account.getFailedAttempts() + 1);
            if (account.getFailedAttempts() >= maxFailedAttempts) {
                account.setLockedUntil(now.plus(lockDuration));
                account.setFailedAttempts(0);
                log.warn("Locked account {} after failed logins", account.getId());
            }
            accounts.save(account);
            throw HarmoniaException.unauthorized(ErrorCode.INVALID_CREDENTIALS, "Invalid credentials");
        }
        account.setFailedAttempts(0);
        account.setLockedUntil(null);
        accounts.save(account);
        return issueSession(account);
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        String hash = tokenHasher.hash(request.refreshToken());
        RefreshToken stored = refreshTokens.findByTokenHash(hash)
                .orElseThrow(() -> HarmoniaException.unauthorized(ErrorCode.REFRESH_TOKEN_INVALID, "Invalid refresh token"));
        if (stored.isRevoked() || stored.isExpired(Instant.now())) {
            throw HarmoniaException.unauthorized(ErrorCode.REFRESH_TOKEN_INVALID, "Refresh token expired or revoked");
        }
        stored.revoke();
        return issueSession(stored.getAccount());
    }

    @Transactional
    public void logout(UUID accountId) {
        refreshTokens.revokeAllForAccount(accountId);
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        EmailVerificationToken token = verificationTokens.findByTokenHash(tokenHasher.hash(request.token()))
                .orElseThrow(() -> HarmoniaException.badRequest(ErrorCode.TOKEN_INVALID, "Invalid verification token"));
        if (token.isUsed() || token.isExpired(Instant.now())) {
            throw HarmoniaException.badRequest(ErrorCode.TOKEN_EXPIRED, "Verification token expired");
        }
        token.markUsed();
        Account account = token.getAccount();
        account.setEmailVerified(true);
        events.publish(Topics.USER, DomainEvent.of(
                EventType.USER_VERIFIED, "Account", account.getId().toString(),
                "auth-service", MDC.get("traceId"), account.getId().toString(),
                Map.of("email", account.getEmail())
        ));
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        accounts.findByEmail(request.email().toLowerCase()).ifPresent(account -> {
            String raw = tokenHasher.randomToken();
            resetTokens.save(new PasswordResetToken(account, tokenHasher.hash(raw), Instant.now().plus(Duration.ofHours(1))));
            events.publish(Topics.USER, DomainEvent.of(
                    EventType.PASSWORD_RESET_REQUESTED, "Account", account.getId().toString(),
                    "auth-service", MDC.get("traceId"), account.getId().toString(),
                    Map.of("resetRequested", true)
            ));
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = resetTokens.findByTokenHash(tokenHasher.hash(request.token()))
                .orElseThrow(() -> HarmoniaException.badRequest(ErrorCode.TOKEN_INVALID, "Invalid reset token"));
        if (token.isUsed() || token.isExpired(Instant.now())) {
            throw HarmoniaException.badRequest(ErrorCode.TOKEN_EXPIRED, "Reset token expired");
        }
        token.markUsed();
        Account account = token.getAccount();
        account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        refreshTokens.revokeAllForAccount(account.getId());
    }

    @Transactional(readOnly = true)
    public AccountResponse me(UUID accountId) {
        Account account = accounts.findById(accountId)
                .orElseThrow(() -> HarmoniaException.notFound(ErrorCode.USER_NOT_FOUND, "User not found"));
        return mapper.toResponse(account);
    }

    private TokenResponse issueSession(Account account) {
        String access = jwtTokenService.issueAccessToken(account);
        String refreshRaw = tokenHasher.randomToken();
        Instant refreshExp = Instant.now().plus(Duration.ofDays(jwtProperties.getRefreshTokenDays()));
        refreshTokens.save(new RefreshToken(account, tokenHasher.hash(refreshRaw), refreshExp));
        return TokenResponse.of(access, refreshRaw, jwtTokenService.accessTokenExpiresInSeconds(),
                account.getId(), account.getEmail());
    }
}
