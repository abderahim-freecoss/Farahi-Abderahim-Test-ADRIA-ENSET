package com.farahi.transactionservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {
    @Id
    private UUID id;
    @NotNull
    private UUID accountId;
    @NotNull @Positive
    private BigDecimal amount;
    @NotBlank
    private String currency;
    @NotBlank
    private String merchantCategory;
    @NotBlank
    private String country;
    private Instant createdAt;
}
