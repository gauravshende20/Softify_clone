package com.harmonia.auth.service;

import com.harmonia.auth.crypto.TokenHasher;
import com.harmonia.auth.domain.Account;
import com.harmonia.auth.dto.LoginRequest;
import com.harmonia.auth.dto.RegisterRequest;
import com.harmonia.auth.mapper.AccountMapper;
import com.harmonia.auth.repo.AccountRepository;
import com.harmonia.auth.repo.EmailVerificationTokenRepository;
import com.harmonia.auth.repo.PasswordResetTokenRepository;
import com.harmonia.auth.repo.RefreshTokenRepository;
import com.harmonia.auth.security.JwtTokenService;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.common.api.security.Roles;
import com.harmonia.common.kafka.DomainEventPublisher;
import com.harmonia.common.security.HarmoniaJwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AccountRepository accounts;
    @Mock RefreshTokenRepository refreshTokens;
    @Mock EmailVerificationTokenRepository verificationTokens;
    @Mock PasswordResetTokenRepository resetTokens;
    @Mock DomainEventPublisher events;

    PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    AuthService service;

    @BeforeEach
    void setUp() {
        HarmoniaJwtProperties props = new HarmoniaJwtProperties();
        props.setSecret("test-secret-test-secret-test-secret-32");
        JwtTokenService jwt = new JwtTokenService(props);
        service = new AuthService(accounts, refreshTokens, verificationTokens, resetTokens,
                encoder, jwt, new TokenHasher(), Mappers.getMapper(AccountMapper.class),
                events, props, 5, 15);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(accounts.existsByEmail("ada@harmonia.local")).thenReturn(true);
        assertThrows(HarmoniaException.class, () -> service.register(
                new RegisterRequest("ada@harmonia.local", "StrongPass1x", Roles.LISTENER)));
    }

    @Test
    void loginRejectsBadPasswordAndIncrementsFailures() {
        Account account = Account.create("ada@harmonia.local", encoder.encode("StrongPass1x"), Roles.LISTENER);
        when(accounts.findByEmail("ada@harmonia.local")).thenReturn(Optional.of(account));
        HarmoniaException ex = assertThrows(HarmoniaException.class,
                () -> service.login(new LoginRequest("ada@harmonia.local", "wrong-password")));
        assertEquals(401, ex.getStatus());
        verify(accounts).save(any(Account.class));
        assertEquals(1, account.getFailedAttempts());
    }
}
