package com.casino.balance.service;

import com.casino.balance.dto.BalanceResponse;
import com.casino.balance.dto.DepositRequest;
import com.casino.balance.dto.TransactionResponse;
import com.casino.balance.dto.WithdrawRequest;
import com.casino.balance.exception.InsufficientFundsException;
import com.casino.balance.exception.UserNotFoundException;
import com.casino.balance.kafka.BalanceEventProducer;
import com.casino.balance.model.Balance;
import com.casino.balance.model.Transaction;
import com.casino.balance.model.TransactionStatus;
import com.casino.balance.model.TransactionType;
import com.casino.balance.repository.BalanceRepository;
import com.casino.balance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final TransactionRepository transactionRepository;
    private final BalanceEventProducer eventProducer;

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(UUID userId) {
        log.info("Getting balance for user: {}", userId);
        
        Balance balance = balanceRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        
        return BalanceResponse.builder()
                .userId(balance.getUserId())
                .balance(balance.getBalance())
                .currency(balance.getCurrency())
                .lastUpdated(balance.getLastUpdated())
                .build();
    }

    @Transactional
    public TransactionResponse deposit(UUID userId, DepositRequest request) {
        log.info("Processing deposit for user: {}, amount: {} {}", userId, request.getAmount(), request.getCurrency());
        
        // Check for duplicate transaction
        if (transactionRepository.existsByExternalTransactionId(request.getTransactionId())) {
            throw new IllegalArgumentException("Transaction ID already exists: " + request.getTransactionId());
        }

        // Get or create balance with pessimistic lock
        Balance balance = balanceRepository.findByUserIdWithLock(userId)
                .orElseGet(() -> createInitialBalance(userId, request.getCurrency()));

        // Validate currency
        if (!balance.getCurrency().equals(request.getCurrency())) {
            throw new IllegalArgumentException(
                    String.format("Currency mismatch. Expected: %s, Got: %s", balance.getCurrency(), request.getCurrency()));
        }

        // Update balance
        BigDecimal newBalance = balance.getBalance().add(request.getAmount());
        balance.setBalance(newBalance);
        balance = balanceRepository.save(balance);

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .userId(userId)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .transactionType(TransactionType.DEPOSIT)
                .status(TransactionStatus.COMPLETED)
                .description(request.getDescription())
                .balanceAfter(newBalance)
                .externalTransactionId(request.getTransactionId())
                .build();
        
        transaction = transactionRepository.save(transaction);

        // Publish event to Kafka
        eventProducer.publishBalanceUpdatedEvent(userId, newBalance, request.getCurrency(), transaction.getId());

        log.info("Deposit completed. User: {}, New balance: {}", userId, newBalance);

        return mapToTransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse withdraw(UUID userId, WithdrawRequest request) {
        log.info("Processing withdrawal for user: {}, amount: {} {}", userId, request.getAmount(), request.getCurrency());
        
        // Check for duplicate transaction
        if (transactionRepository.existsByExternalTransactionId(request.getTransactionId())) {
            throw new IllegalArgumentException("Transaction ID already exists: " + request.getTransactionId());
        }

        // Get balance with pessimistic lock
        Balance balance = balanceRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        // Validate currency
        if (!balance.getCurrency().equals(request.getCurrency())) {
            throw new IllegalArgumentException(
                    String.format("Currency mismatch. Expected: %s, Got: %s", balance.getCurrency(), request.getCurrency()));
        }

        // Check sufficient funds
        if (balance.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(
                    String.format("Insufficient funds. Current balance: %s, Requested: %s", 
                            balance.getBalance(), request.getAmount()));
        }

        // Update balance
        BigDecimal newBalance = balance.getBalance().subtract(request.getAmount());
        balance.setBalance(newBalance);
        balance = balanceRepository.save(balance);

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .userId(userId)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.COMPLETED)
                .description(request.getDescription())
                .balanceAfter(newBalance)
                .externalTransactionId(request.getTransactionId())
                .build();
        
        transaction = transactionRepository.save(transaction);

        // Publish event to Kafka
        eventProducer.publishBalanceUpdatedEvent(userId, newBalance, request.getCurrency(), transaction.getId());

        log.info("Withdrawal completed. User: {}, New balance: {}", userId, newBalance);

        return mapToTransactionResponse(transaction);
    }

    private Balance createInitialBalance(UUID userId, String currency) {
        log.info("Creating initial balance for user: {}, currency: {}", userId, currency);
        Balance balance = Balance.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .currency(currency)
                .build();
        return balanceRepository.save(balance);
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .transactionId(transaction.getId())
                .userId(transaction.getUserId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .timestamp(transaction.getTimestamp())
                .newBalance(transaction.getBalanceAfter())
                .build();
    }
}
