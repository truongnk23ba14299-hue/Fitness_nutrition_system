package com.fitness.service;

import com.fitness.model.Payment;
import com.fitness.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentExpirationScheduler {

    private final PaymentRepository paymentRepository;
    private final SePayPaymentService sePayPaymentService;

    /**
     * Quét mỗi 60 giây: tìm các giao dịch QR PENDING đã quá hạn expiresAt để hủy & hoàn tồn kho/voucher
     */
    @Scheduled(fixedDelay = 60_000)
    public void expireOverduePayments() {
        List<Payment> overdueList = paymentRepository.findByStatusAndExpiresAtBefore("PENDING", LocalDateTime.now());
        if (overdueList.isEmpty()) {
            return;
        }

        log.info("[PaymentScheduler] Phát hiện {} giao dịch QR quá hạn, tiến hành xử lý...", overdueList.size());
        for (Payment payment : overdueList) {
            try {
                sePayPaymentService.expireSinglePayment(payment);
            } catch (Exception ex) {
                log.error("[PaymentScheduler] Lỗi khi hết hạn giao dịch {}: {}", payment.getPaymentCode(), ex.getMessage());
            }
        }
    }
}