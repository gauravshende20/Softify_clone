package com.harmonia.auth.bootstrap;

import com.harmonia.auth.domain.Account;
import com.harmonia.auth.repo.AccountRepository;
import com.harmonia.common.api.security.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AccountRepository accounts;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;

    public AdminBootstrap(AccountRepository accounts,
                          PasswordEncoder encoder,
                          @Value("${harmonia.auth.bootstrap-admin-email}") String email,
                          @Value("${harmonia.auth.bootstrap-admin-password}") String password) {
        this.accounts = accounts;
        this.encoder = encoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (accounts.existsByEmail(email.toLowerCase())) {
            return;
        }
        Account admin = Account.create(email, encoder.encode(password), Roles.ADMIN);
        admin.addRole(Roles.MODERATOR);
        admin.setEmailVerified(true);
        accounts.save(admin);
        log.info("Bootstrapped admin account {}", admin.getId());
    }
}
