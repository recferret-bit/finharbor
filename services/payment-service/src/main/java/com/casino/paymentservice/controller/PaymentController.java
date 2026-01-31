package com.casino.paymentservice.controller;

import com.casino.paymentservice.model.Payment;
import com.casino.paymentservice.model.PaymentHistoryResponse;
import com.casino.paymentservice.model.PaymentRequest;
import com.casino.paymentservice.model.PaymentResult;
import com.casino.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResult> processPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Received payment request: userId={}, amount={} {}", 
                request.getUserId(), request.getAmount(), request.getCurrency());
        
        PaymentResult result = paymentService.processPayment(request);
        
        HttpStatus status = "COMPLETED".equals(result.getStatus()) 
                ? HttpStatus.OK 
                : HttpStatus.BAD_REQUEST;
        
        return ResponseEntity.status(status).body(result);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPayment(@PathVariable UUID paymentId) {
        log.info("Retrieving payment: paymentId={}", paymentId);
        
        Payment payment = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<PaymentHistoryResponse> getPaymentHistory(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        
        log.info("Retrieving payment history: userId={}, limit={}, offset={}", 
                userId, limit, offset);
        
        if (limit < 1 || limit > 100) {
            return ResponseEntity.badRequest().build();
        }
        if (offset < 0) {
            return ResponseEntity.badRequest().build();
        }
        
        List<Payment> payments = paymentService.getPaymentHistory(userId, limit, offset);
        long total = paymentService.getPaymentCount(userId);
        
        PaymentHistoryResponse response = PaymentHistoryResponse.builder()
                .payments(payments)
                .total((int) total)
                .limit(limit)
                .offset(offset)
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> healthCheck() {
        return ResponseEntity.ok(HealthResponse.builder()
                .status("UP")
                .timestamp(java.time.LocalDateTime.now())
                .version("2.5.1")
                .build());
    }

    @ExceptionHandler(PaymentService.PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(
            PaymentService.PaymentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .error("PAYMENT_NOT_FOUND")
                        .message(ex.getMessage())
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .error("VALIDATION_ERROR")
                        .message(ex.getMessage())
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }

    @lombok.Builder
    @lombok.Data
    static class HealthResponse {
        private String status;
        private java.time.LocalDateTime timestamp;
        private String version;
    }

    @lombok.Builder
    @lombok.Data
    static class ErrorResponse {
        private String error;
        private String message;
        private java.time.LocalDateTime timestamp;
        private String path;
    }
}
