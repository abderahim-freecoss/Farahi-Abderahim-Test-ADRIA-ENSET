package com.farahi.transactionservice.web.rest;

import com.farahi.transactionservice.domain.Transaction;
import com.farahi.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
public class TransactionRestController {
    private final TransactionService transactionService;

    public TransactionRestController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/transactions")
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/transactions/{id}")
    public Transaction getTransactionById(@PathVariable UUID id) {
        return transactionService.getTransactionById(id);
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public List<Transaction> getTransactionsByAccountId(@PathVariable UUID accountId) {
        return transactionService.getTransactionsByAccountId(accountId);
    }

    @PostMapping("/transactions")
    public Transaction createTransaction(@Valid @RequestBody Transaction transaction) {
        return transactionService.createTransaction(transaction);
    }

    @PostMapping("/transfers")
    public void transfer(@RequestBody TransferRequest request) {
        transactionService.transfer(request.sourceId(), request.destinationId(), request.amount());
    }

    public record TransferRequest(UUID sourceId, UUID destinationId, BigDecimal amount) {
    }
}
