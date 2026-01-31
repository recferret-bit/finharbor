package com.casino.paymentservice.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "USD|EUR|GBP|CAD|AUD", message = "Invalid currency code")
    private String currency;

    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "CREDIT_CARD|DEBIT_CARD|BANK_TRANSFER|E_WALLET", 
             message = "Invalid payment method")
    private String paymentMethod;

    private String paymentMethodId;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Map<String, Object> metadata;
}
