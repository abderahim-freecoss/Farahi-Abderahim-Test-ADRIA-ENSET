package com.farahi.accountservice.mcp;

import com.farahi.accountservice.domain.Account;
import com.farahi.accountservice.service.AccountService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AccountMcpTools {
    private final AccountService accountService;

    public AccountMcpTools(AccountService accountService) {
        this.accountService = accountService;
    }

    @McpTool(description = "List all bank accounts")
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @McpTool(description = "Find a bank account by its UUID")
    public Account getAccountById(@McpToolParam(description = "Account UUID") String id) {
        return accountService.getAccountById(UUID.fromString(id));
    }

    @McpTool(description = "Create a new active bank account")
    public Account createAccount(
            @McpToolParam(description = "Customer reference") String customerReference,
            @McpToolParam(description = "Account currency") String currency,
            @McpToolParam(description = "Initial balance") double balance) {
        Account account = Account.builder()
                .customerReference(customerReference)
                .currency(currency)
                .balance(balance)
                .build();
        return accountService.saveAccount(account);
    }
}
