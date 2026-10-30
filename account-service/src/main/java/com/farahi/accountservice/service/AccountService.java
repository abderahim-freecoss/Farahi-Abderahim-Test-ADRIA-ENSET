package com.farahi.accountservice.service;

import com.farahi.accountservice.domain.Account;
import com.farahi.accountservice.domain.AccountStatus;
import com.farahi.accountservice.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
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
                .orElseThrow(() -> new NoSuchElementException("Account not found: " + id));
    }

    public Account saveAccount(Account account) {
        account.setId(UUID.randomUUID());
        account.setStatus(AccountStatus.ACTIVE);
        return accountRepository.save(account);
    }

    @Transactional
    public Account debit(UUID id, BigDecimal amount) {
        requirePositive(amount);
        Account account = getActiveAccount(id);
        BigDecimal balance = BigDecimal.valueOf(account.getBalance());
        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        account.setBalance(balance.subtract(amount).doubleValue());
        return accountRepository.save(account);
    }

    @Transactional
    public Account credit(UUID id, BigDecimal amount) {
        requirePositive(amount);
        Account account = getActiveAccount(id);
        BigDecimal balance = BigDecimal.valueOf(account.getBalance());
        account.setBalance(balance.add(amount).doubleValue());
        return accountRepository.save(account);
    }

    private Account getActiveAccount(UUID id) {
        Account account = getAccountById(id);
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Account is not active: " + id);
        }
        return account;
    }

    private void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }
}
