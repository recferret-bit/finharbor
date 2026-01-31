package com.casino.balance.dto;

import com.casino.balance.model.TransactionStatus;
import com.casino.balance.model.TransactionType;
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
public class TransactionResponse {
    private UUID transactionId;
    private UUID userId;
    private BigDecimal amount;
    private String currency;
    private TransactionType transactionType;
    private TransactionStatus status;
    private String description;
    private LocalDateTime timestamp;
    private BigDecimal newBalance;
}
