package com.farahi.transactionservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "account-service")
public interface AccountClient {
    @GetMapping("/accounts/{id}")
    AccountView getAccount(@PathVariable UUID id);

    @PostMapping("/accounts/{id}/debit")
    AccountView debit(@PathVariable UUID id, @RequestBody AmountRequest request);

    @PostMapping("/accounts/{id}/credit")
    AccountView credit(@PathVariable UUID id, @RequestBody AmountRequest request);

    record AccountView(UUID id, String customerReference, String status, String currency, double balance) {
    }

    record AmountRequest(BigDecimal amount) {
    }
}
