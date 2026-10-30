package com.farahi.transactionservice.mcp;

import com.farahi.transactionservice.domain.Transaction;
import com.farahi.transactionservice.service.TransactionService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class TransactionMcpTools {
    private final TransactionService transactionService;

    public TransactionMcpTools(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @McpTool(description = "List all bank transactions")
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @McpTool(description = "List transactions for an account")
    public List<Transaction> getTransactionsByAccountId(@McpToolParam(description = "Account UUID") String accountId) {
        return transactionService.getTransactionsByAccountId(UUID.fromString(accountId));
    }

    @McpTool(description = "Create a bank transaction")
    public Transaction createTransaction(
            @McpToolParam(description = "Account UUID") String accountId,
            @McpToolParam(description = "Positive amount") String amount,
            @McpToolParam(description = "Currency") String currency,
            @McpToolParam(description = "Merchant category") String merchantCategory,
            @McpToolParam(description = "Country") String country) {
        Transaction transaction = Transaction.builder()
                .accountId(UUID.fromString(accountId))
                .amount(new BigDecimal(amount))
                .currency(currency)
                .merchantCategory(merchantCategory)
                .country(country)
                .build();
        return transactionService.createTransaction(transaction);
    }

    @McpTool(description = "Transfer money between two bank accounts")
    public String transfer(
            @McpToolParam(description = "Source account UUID") String sourceId,
            @McpToolParam(description = "Destination account UUID") String destinationId,
            @McpToolParam(description = "Positive amount") String amount) {
        transactionService.transfer(UUID.fromString(sourceId), UUID.fromString(destinationId), new BigDecimal(amount));
        return "Transfer completed";
    }
}
