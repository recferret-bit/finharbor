package com.casino.paymentservice.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private UUID paymentId;
    private UUID userId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String status;
    private String transactionId;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;
}
