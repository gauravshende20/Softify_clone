package com.harmonia.auth.mapper;

import com.harmonia.auth.domain.Account;
import com.harmonia.auth.domain.AccountRole;
import com.harmonia.auth.dto.AccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "roles", expression = "java(roles(account))")
    AccountResponse toResponse(Account account);

    default Set<String> roles(Account account) {
        return account.getRoles().stream().map(AccountRole::getRole).collect(Collectors.toSet());
    }
}
