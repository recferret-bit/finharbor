package com.casino.balance.controller;

import com.casino.balance.dto.ErrorResponse;
import com.casino.balance.dto.TransactionHistoryResponse;
import com.casino.balance.exception.UserNotFoundException;
import com.casino.balance.model.TransactionType;
import com.casino.balance.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/balance/{userId}/transactions")
    public ResponseEntity<TransactionHistoryResponse> getTransactionHistory(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) TransactionType transactionType) {
        log.info("GET /balance/{}/transactions?page={}&size={}&transactionType={}", userId, page, size, transactionType);
        TransactionHistoryResponse response = transactionService.getTransactionHistory(userId, page, size, transactionType);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex,
                                                             @org.springframework.web.context.request.WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .error("USER_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
