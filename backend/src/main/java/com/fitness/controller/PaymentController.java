package com.fitness.controller;

import com.fitness.dto.PaymentHistoryResponse;
import com.fitness.dto.PaymentInfoResponse;
import com.fitness.dto.SePayWebhookRequest;
import com.fitness.model.User;
import com.fitness.service.PaymentService;
import com.fitness.service.SePayPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;
    private final SePayPaymentService sePayPaymentService;

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

    /**
     * 1. Webhook nhận biến động số dư từ SePay (mở permitAll trong SecurityConfig, xác thực bằng Apikey)
     * POST /api/payments/webhook/sepay
     */
    @PostMapping("/webhook/sepay")
    public ResponseEntity<Map<String, Boolean>> handleSePayWebhook(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody SePayWebhookRequest payload
    ) {
        sePayPaymentService.handleSePayWebhook(authorization, payload);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 2. Frontend polling trạng thái thanh toán (3 giây/lần)
     * GET /api/payments/{paymentCode}/status
     */
    @GetMapping("/{paymentCode}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentInfoResponse> getPaymentStatus(
            @PathVariable String paymentCode,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(sePayPaymentService.getPaymentStatus(paymentCode, user));
    }

    /**
     * 3. Người dùng / Lễ tân hủy giao dịch QR đang chờ
     * POST /api/payments/{paymentCode}/cancel
     */
    @PostMapping("/{paymentCode}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentInfoResponse> cancelPayment(
            @PathVariable String paymentCode,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(sePayPaymentService.cancelPayment(paymentCode, user));
    }

    /**
     * 4. API dev giả lập đối soát thành công khi test offline (chỉ chạy khi dev-simulate-enabled=true)
     * POST /api/payments/dev/simulate-paid/{paymentCode}
     */
    @PostMapping("/dev/simulate-paid/{paymentCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentInfoResponse> simulatePaid(
            @PathVariable String paymentCode
    ) {
        return ResponseEntity.ok(sePayPaymentService.simulatePaidForDev(paymentCode));
    }
}