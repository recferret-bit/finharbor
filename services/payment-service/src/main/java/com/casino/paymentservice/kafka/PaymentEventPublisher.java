package com.casino.paymentservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @Value("${payment.kafka.topics.initiated}")
    private String initiatedTopic;

    @Value("${payment.kafka.topics.completed}")
    private String completedTopic;

    @Value("${payment.kafka.topics.failed}")
    private String failedTopic;

    public void publishInitiatedEvent(UUID paymentId, UUID userId, java.math.BigDecimal amount, 
                                     String currency, String paymentMethod) {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(paymentId)
                .userId(userId)
                .amount(amount)
                .currency(currency)
                .paymentMethod(paymentMethod)
                .status("INITIATED")
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(initiatedTopic, paymentId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Published payment initiated event: paymentId={}", paymentId);
                    } else {
                        log.error("Failed to publish payment initiated event: paymentId={}", 
                                paymentId, ex);
                    }
                });
    }

    public void publishCompletedEvent(UUID paymentId, UUID userId, java.math.BigDecimal amount,
                                      String currency, String paymentMethod, String transactionId) {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(paymentId)
                .userId(userId)
                .amount(amount)
                .currency(currency)
                .paymentMethod(paymentMethod)
                .status("COMPLETED")
                .transactionId(transactionId)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(completedTopic, paymentId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Published payment completed event: paymentId={}", paymentId);
                    } else {
                        log.error("Failed to publish payment completed event: paymentId={}", 
                                paymentId, ex);
                    }
                });
    }

    public void publishFailedEvent(UUID paymentId, UUID userId, java.math.BigDecimal amount,
                                   String currency, String paymentMethod, String errorCode,
                                   String errorMessage) {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(paymentId)
                .userId(userId)
                .amount(amount)
                .currency(currency)
                .paymentMethod(paymentMethod)
                .status("FAILED")
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(failedTopic, paymentId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Published payment failed event: paymentId={}, errorCode={}", 
                                paymentId, errorCode);
                    } else {
                        log.error("Failed to publish payment failed event: paymentId={}", 
                                paymentId, ex);
                    }
                });
    }
}
