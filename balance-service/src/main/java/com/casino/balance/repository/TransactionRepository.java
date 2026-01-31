package com.casino.balance.repository;

import com.casino.balance.model.Transaction;
import com.casino.balance.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByUserIdOrderByTimestampDesc(UUID userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId " +
           "AND (:transactionType IS NULL OR t.transactionType = :transactionType) " +
           "ORDER BY t.timestamp DESC")
    Page<Transaction> findByUserIdAndTransactionType(
            @Param("userId") UUID userId,
            @Param("transactionType") TransactionType transactionType,
            Pageable pageable);

    Optional<Transaction> findByExternalTransactionId(String externalTransactionId);

    boolean existsByExternalTransactionId(String externalTransactionId);
}
