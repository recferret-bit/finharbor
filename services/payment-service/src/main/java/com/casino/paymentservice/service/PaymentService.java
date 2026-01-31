package com.casino.paymentservice.service;

import com.casino.paymentservice.kafka.PaymentEventPublisher;
import com.casino.paymentservice.model.Payment;
import com.casino.paymentservice.model.PaymentRequest;
import com.casino.paymentservice.model.PaymentResult;
import com.casino.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;

    @Transactional
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("Processing payment: userId={}, amount={} {}", 
                request.getUserId(), request.getAmount(), request.getCurrency());

        // Create payment record
        Payment payment = Payment.builder()
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .paymentMethodId(request.getPaymentMethodId())
                .description(request.getDescription())
                .status(Payment.PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);

        // Publish initiated event
        eventPublisher.publishInitiatedEvent(
                payment.getPaymentId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod()
        );

        // Simulate payment processing
        PaymentResult result = processPaymentInternal(payment, request);

        // Update payment status
        if ("COMPLETED".equals(result.getStatus())) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setTransactionId(result.getTransactionId());
            eventPublisher.publishCompletedEvent(
                    payment.getPaymentId(),
                    payment.getUserId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getPaymentMethod(),
                    result.getTransactionId()
            );
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setErrorCode(result.getErrorCode());
            payment.setErrorMessage(result.getErrorMessage());
            eventPublisher.publishFailedEvent(
                    payment.getPaymentId(),
                    payment.getUserId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getPaymentMethod(),
                    result.getErrorCode(),
                    result.getErrorMessage()
            );
        }

        payment = paymentRepository.save(payment);

        return PaymentResult.builder()
                .paymentId(payment.getPaymentId())
                .status(result.getStatus())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .transactionId(payment.getTransactionId())
                .errorCode(payment.getErrorCode())
                .errorMessage(payment.getErrorMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    private PaymentResult processPaymentInternal(Payment payment, PaymentRequest request) {
        // Simulate payment gateway call
        // In production, this would call an actual payment gateway (Stripe, PayPal, etc.)
        
        try {
            // Simulate processing delay
            Thread.sleep(100);

            // Simulate payment validation
            if (payment.getAmount().doubleValue() <= 0) {
                return PaymentResult.builder()
                        .status("FAILED")
                        .errorCode("INVALID_AMOUNT")
                        .errorMessage("Payment amount must be greater than zero")
                        .build();
            }

            // Simulate payment gateway response (90% success rate for demo)
            boolean success = Math.random() > 0.1;

            if (success) {
                String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                return PaymentResult.builder()
                        .status("COMPLETED")
                        .transactionId(transactionId)
                        .build();
            } else {
                return PaymentResult.builder()
                        .status("FAILED")
                        .errorCode("PAYMENT_GATEWAY_ERROR")
                        .errorMessage("Payment gateway declined the transaction")
                        .build();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PaymentResult.builder()
                    .status("FAILED")
                    .errorCode("PROCESSING_ERROR")
                    .errorMessage("Payment processing was interrupted")
                    .build();
        } catch (Exception e) {
            log.error("Error processing payment: paymentId={}", payment.getPaymentId(), e);
            return PaymentResult.builder()
                    .status("FAILED")
                    .errorCode("INTERNAL_ERROR")
                    .errorMessage("An internal error occurred while processing the payment")
                    .build();
        }
    }

    public Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
    }

    public java.util.List<Payment> getPaymentHistory(UUID userId, int limit, int offset) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, 
                org.springframework.data.domain.PageRequest.of(offset / limit, limit));
    }

    public long getPaymentCount(UUID userId) {
        return paymentRepository.countByUserId(userId);
    }

    public static class PaymentNotFoundException extends RuntimeException {
        public PaymentNotFoundException(String message) {
            super(message);
        }
    }
}
