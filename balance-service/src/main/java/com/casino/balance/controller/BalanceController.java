package com.casino.balance.controller;

import com.casino.balance.dto.BalanceResponse;
import com.casino.balance.dto.DepositRequest;
import com.casino.balance.dto.ErrorResponse;
import com.casino.balance.dto.TransactionResponse;
import com.casino.balance.dto.WithdrawRequest;
import com.casino.balance.exception.InsufficientFundsException;
import com.casino.balance.exception.UserNotFoundException;
import com.casino.balance.service.BalanceService;
import jakarta.validation.Valid;
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
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/balance/{userId}")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable UUID userId) {
        log.info("GET /balance/{}", userId);
        BalanceResponse response = balanceService.getBalance(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/balance/{userId}/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable UUID userId,
            @Valid @RequestBody DepositRequest request) {
        log.info("POST /balance/{}/deposit", userId);
        TransactionResponse response = balanceService.deposit(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/balance/{userId}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable UUID userId,
            @Valid @RequestBody WithdrawRequest request) {
        log.info("POST /balance/{}/withdraw", userId);
        TransactionResponse response = balanceService.withdraw(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok().body(java.util.Map.of("status", "UP"));
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

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex,
                                                                   @org.springframework.web.context.request.WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .error("INSUFFICIENT_FUNDS")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                              @org.springframework.web.context.request.WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .error("INVALID_REQUEST")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex,
                                                                @org.springframework.web.context.request.WebRequest request) {
        log.error("Unexpected error", ex);
        ErrorResponse error = ErrorResponse.builder()
                .error("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
