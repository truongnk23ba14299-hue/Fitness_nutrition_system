package com.fitness.controller;

import com.fitness.dto.PaymentHistoryResponse;
import com.fitness.model.User;
import com.fitness.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Học viên xem lịch sử hóa đơn thanh toán tập trung (gói tập + shop sản phẩm)
     * GET /api/payments/my
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<PaymentHistoryResponse>> getMyPayments(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paymentService.getMyPaymentHistory(user));
    }
}
