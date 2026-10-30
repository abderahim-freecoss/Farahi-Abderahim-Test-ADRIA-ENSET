package com.farahi.transactionservice.web.graphql;

import com.farahi.transactionservice.domain.Transaction;
import com.farahi.transactionservice.service.TransactionService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
public class TransactionGraphqlController {
    private final TransactionService transactionService;

    public TransactionGraphqlController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @QueryMapping
    public List<Transaction> transactions() {
        return transactionService.getAllTransactions();
    }

    @QueryMapping
    public Transaction transactionById(@Argument String id) {
        return transactionService.getTransactionById(UUID.fromString(id));
    }

    @QueryMapping
    public List<Transaction> transactionsByAccount(@Argument String accountId) {
        return transactionService.getTransactionsByAccountId(UUID.fromString(accountId));
    }

    @MutationMapping
    public Transaction createTransaction(@Argument TransactionInput input) {
        Transaction transaction = Transaction.builder()
                .accountId(UUID.fromString(input.accountId()))
                .amount(BigDecimal.valueOf(input.amount()))
                .currency(input.currency())
                .merchantCategory(input.merchantCategory())
                .country(input.country())
                .build();
        return transactionService.createTransaction(transaction);
    }

    @MutationMapping
    public boolean transfer(@Argument String sourceId, @Argument String destinationId, @Argument double amount) {
        transactionService.transfer(UUID.fromString(sourceId), UUID.fromString(destinationId), BigDecimal.valueOf(amount));
        return true;
    }

    public record TransactionInput(String accountId, double amount, String currency,
                                   String merchantCategory, String country) {
    }
}
