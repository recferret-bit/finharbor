package com.casino.balance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {
    private UUID userId;
    private BigDecimal balance;
    private String currency;
    private LocalDateTime lastUpdated;
}
