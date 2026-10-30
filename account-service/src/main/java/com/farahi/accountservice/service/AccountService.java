package com.farahi.accountservice.service;

import com.farahi.accountservice.domain.Account;
import com.farahi.accountservice.domain.AccountStatus;
import com.farahi.accountservice.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccountById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
    }

    public Account saveAccount(Account account) {
        account.setId(UUID.randomUUID());
        account.setStatus(AccountStatus.ACTIVE);
        return accountRepository.save(account);
    }
}
