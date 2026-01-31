package com.casino.balance.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BalanceEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.balance-updated:balance-updated}")
    private String balanceUpdatedTopic;

    public void publishBalanceUpdatedEvent(UUID userId, BigDecimal balance, String currency, UUID transactionId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("userId", userId.toString());
            event.put("balance", balance.toString());
            event.put("currency", currency);
            event.put("transactionId", transactionId.toString());
            event.put("timestamp", System.currentTimeMillis());
            event.put("eventType", "BALANCE_UPDATED");

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(balanceUpdatedTopic, userId.toString(), eventJson);
            
            log.info("Published balance updated event for user: {} to topic: {}", userId, balanceUpdatedTopic);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize balance updated event for user: {}", userId, e);
            // Don't throw exception - event publishing failure shouldn't break the transaction
        }
    }
}
