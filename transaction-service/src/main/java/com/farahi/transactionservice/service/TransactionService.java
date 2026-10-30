package com.farahi.transactionservice.service;

import com.farahi.transactionservice.domain.Transaction;
import com.farahi.transactionservice.feign.AccountClient;
import com.farahi.transactionservice.repository.TransactionRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private final BigDecimal maxAmount;

    public TransactionService(TransactionRepository transactionRepository, AccountClient accountClient,
                              CircuitBreakerFactory<?, ?> circuitBreakerFactory,
                              @Value("${bank.transaction.max-amount}") BigDecimal maxAmount) {
        this.transactionRepository = transactionRepository;
        this.accountClient = accountClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.maxAmount = maxAmount;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getTransactionsByAccountId(UUID accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    public Transaction getTransactionById(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));
    }

    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        validateAmount(transaction.getAmount());
        AccountClient.AccountView account = getAccount(transaction.getAccountId());
        requireActive(account);
        if (!account.currency().equalsIgnoreCase(transaction.getCurrency())) {
            throw new IllegalArgumentException("Transaction currency differs from account currency");
        }
        if (BigDecimal.valueOf(account.balance()).compareTo(transaction.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        transaction.setId(UUID.randomUUID());
        transaction.setCreatedAt(Instant.now());
        Transaction saved = transactionRepository.save(transaction);
        callAccount(() -> accountClient.debit(account.id(), new AccountClient.AmountRequest(transaction.getAmount())));
        return saved;
    }

    public void transfer(UUID sourceId, UUID destinationId, BigDecimal amount) {
        validateAmount(amount);
        if (sourceId.equals(destinationId)) {
            throw new IllegalArgumentException("Source and destination must differ");
        }
        AccountClient.AccountView source = getAccount(sourceId);
        AccountClient.AccountView destination = getAccount(destinationId);
        requireActive(source);
        requireActive(destination);
        if (!source.currency().equalsIgnoreCase(destination.currency())) {
            throw new IllegalArgumentException("Account currencies differ");
        }
        if (BigDecimal.valueOf(source.balance()).compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        callAccount(() -> accountClient.debit(sourceId, new AccountClient.AmountRequest(amount)));
        try {
            callAccount(() -> accountClient.credit(destinationId, new AccountClient.AmountRequest(amount)));
        } catch (RuntimeException e) {
            callAccount(() -> accountClient.credit(sourceId, new AccountClient.AmountRequest(amount)));
            throw e;
        }
    }

    private AccountClient.AccountView getAccount(UUID id) {
        return callAccount(() -> accountClient.getAccount(id));
    }

    private AccountClient.AccountView callAccount(java.util.function.Supplier<AccountClient.AccountView> call) {
        return circuitBreakerFactory.create("accountService")
                .run(call, error -> {
                    if (error instanceof FeignException.NotFound) {
                        throw new IllegalArgumentException("Account not found", error);
                    }
                    if (error instanceof FeignException.BadRequest) {
                        throw new IllegalArgumentException("Account operation rejected", error);
                    }
                    throw new IllegalStateException("Account service unavailable", error);
                });
    }

    private void requireActive(AccountClient.AccountView account) {
        if (!"ACTIVE".equals(account.status())) {
            throw new IllegalArgumentException("Account is not active: " + account.id());
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException("Amount must be positive and at most " + maxAmount);
        }
    }
}
