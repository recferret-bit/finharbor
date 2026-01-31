package com.casino.balance.service;

import com.casino.balance.dto.TransactionHistoryResponse;
import com.casino.balance.dto.TransactionResponse;
import com.casino.balance.exception.UserNotFoundException;
import com.casino.balance.model.Transaction;
import com.casino.balance.model.TransactionType;
import com.casino.balance.repository.BalanceRepository;
import com.casino.balance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BalanceRepository balanceRepository;

    @Transactional(readOnly = true)
    public TransactionHistoryResponse getTransactionHistory(UUID userId, int page, int size, TransactionType transactionType) {
        log.info("Getting transaction history for user: {}, page: {}, size: {}, type: {}", userId, page, size, transactionType);
        
        // Verify user exists
        if (!balanceRepository.existsByUserId(userId)) {
            throw new UserNotFoundException("User not found: " + userId);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactionPage;

        if (transactionType != null) {
            transactionPage = transactionRepository.findByUserIdAndTransactionType(userId, transactionType, pageable);
        } else {
            transactionPage = transactionRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
        }

        return TransactionHistoryResponse.builder()
                .transactions(transactionPage.getContent().stream()
                        .map(this::mapToTransactionResponse)
                        .collect(Collectors.toList()))
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .build();
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
