package com.casino.balance.repository;

import com.casino.balance.model.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Balance b WHERE b.userId = :userId")
    Optional<Balance> findByUserIdWithLock(@Param("userId") UUID userId);

    Optional<Balance> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
