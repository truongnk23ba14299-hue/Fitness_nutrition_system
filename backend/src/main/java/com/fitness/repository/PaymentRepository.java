package com.fitness.repository;

import com.fitness.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentCode(String paymentCode);

    Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);

    Optional<Payment> findTopByRefTypeAndRefIdOrderByCreatedAtDesc(String refType, Long refId);

    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Payment> findByStatusAndExpiresAtBefore(String status, LocalDateTime now);
}