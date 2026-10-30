package com.farahi.accountservice.web.graphql;

import com.farahi.accountservice.domain.Account;
import com.farahi.accountservice.service.AccountService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class AccountGraphqlController {
    private final AccountService accountService;

    public AccountGraphqlController(AccountService accountService) {
        this.accountService = accountService;
    }

    @QueryMapping
    public List<Account> accounts() {
        return accountService.getAllAccounts();
    }

    @QueryMapping
    public Account accountById(@Argument String id) {
        return accountService.getAccountById(UUID.fromString(id));
    }

    @MutationMapping
    public Account createAccount(@Argument AccountInput input) {
        Account account = Account.builder()
                .customerReference(input.customerReference())
                .currency(input.currency())
                .balance(input.balance())
                .build();
        return accountService.saveAccount(account);
    }

    public record AccountInput(String customerReference, String currency, double balance) {
    }
}
