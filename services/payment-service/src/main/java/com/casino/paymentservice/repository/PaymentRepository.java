package com.casino.paymentservice.repository;

import com.casino.paymentservice.model.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    long countByUserId(UUID userId);
}
