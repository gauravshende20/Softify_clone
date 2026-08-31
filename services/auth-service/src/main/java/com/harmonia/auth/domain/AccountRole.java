package com.harmonia.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "account_roles")
@IdClass(AccountRole.Pk.class)
public class AccountRole {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Id
    @Column(nullable = false, length = 32)
    private String role;

    protected AccountRole() {
    }

    public AccountRole(Account account, String role) {
        this.account = account;
        this.role = role;
    }

    public Account getAccount() {
        return account;
    }

    public String getRole() {
        return role;
    }

    public static class Pk implements Serializable {
        private UUID account;
        private String role;

        public Pk() {
        }

        public Pk(UUID account, String role) {
            this.account = account;
            this.role = role;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pk pk)) return false;
            return Objects.equals(account, pk.account) && Objects.equals(role, pk.role);
        }

        @Override
        public int hashCode() {
            return Objects.hash(account, role);
        }
    }
}
