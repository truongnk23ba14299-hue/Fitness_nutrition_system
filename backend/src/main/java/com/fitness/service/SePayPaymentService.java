package com.fitness.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.config.PaymentProperties;
import com.fitness.dto.PaymentInfoResponse;
import com.fitness.dto.SePayWebhookRequest;
import com.fitness.model.*;
import com.fitness.repository.OrderRepository;
import com.fitness.repository.PackageEnrollmentRepository;
import com.fitness.repository.PaymentRepository;
import com.fitness.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class SePayPaymentService {

    private static final Pattern PAYMENT_CODE_PATTERN = Pattern.compile("FL(ORD|PKG|POS)\\d+", Pattern.CASE_INSENSITIVE);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PackageEnrollmentRepository enrollmentRepository;
    private final VoucherRepository voucherRepository;
    private final PaymentProperties paymentProperties;
    private final ObjectMapper objectMapper;

    /**
     * 1. Tạo bản ghi thanh toán VietQR (dùng chung cho ORDER, ENROLLMENT, POS)
     */
    @Transactional
    public PaymentInfoResponse createVietQrPayment(User user, String refType, Long refId, BigDecimal amount, User staff) {
        String normalizedRefType = refType.toUpperCase(Locale.ROOT);
        String prefix = switch (normalizedRefType) {
            case "ORDER" -> "FLORD";
            case "ENROLLMENT" -> "FLPKG";
            case "POS" -> "FLPOS";
            default -> throw new IllegalArgumentException("Loại tham chiếu không hợp lệ: " + refType);
        };

        String invoicePrefix = switch (normalizedRefType) {
            case "ORDER" -> "INV-ORD-";
            case "ENROLLMENT" -> "INV-PKG-";
            case "POS" -> "INV-POS-";
            default -> "INV-";
        };

        String paymentCode = prefix + refId;
        String invoiceNo = invoicePrefix + refId;
        PaymentProperties.Sepay sepay = paymentProperties.getSepay();
        String qrUrl = buildQrUrl(sepay.getAccountNumber(), sepay.getBankCode(), amount, paymentCode);

        Payment payment = Payment.builder()
                .invoiceNo(invoiceNo)
                .paymentCode(paymentCode)
                .user(user)
                .refType(normalizedRefType)
                .refId(refId)
                .amount(amount)
                .method("VIETQR")
                .status("PENDING")
                .qrUrl(qrUrl)
                .bankCode(sepay.getBankCode())
                .accountNumber(sepay.getAccountNumber())
                .accountName(sepay.getAccountName())
                .gateway("SEPAY")
                .staff(staff)
                .expiresAt(LocalDateTime.now().plusMinutes(paymentProperties.getQrExpireMinutes()))
                .build();

        Payment saved = paymentRepository.save(payment);
        return PaymentInfoResponse.from(saved, sepay.getBankName());
    }

    /**
     * 2. Tạo bản ghi thanh toán đã thu tiền ngay (dùng cho POS CASH hoặc COD hoàn tất)
     */
    @Transactional
    public Payment createImmediatePaidPayment(User user, String refType, Long refId, BigDecimal amount, String method, User staff) {
        String normalizedRefType = refType.toUpperCase(Locale.ROOT);
        String codePrefix = switch (normalizedRefType) {
            case "ORDER" -> "FLORD";
            case "ENROLLMENT" -> "FLPKG";
            case "POS" -> "FLPOS";
            default -> "FLPAY";
        };
        String invoicePrefix = switch (normalizedRefType) {
            case "ORDER" -> "INV-ORD-";
            case "ENROLLMENT" -> "INV-PKG-";
            case "POS" -> "INV-POS-";
            default -> "INV-";
        };

        Payment payment = Payment.builder()
                .invoiceNo(invoicePrefix + refId)
                .paymentCode(codePrefix + refId)
                .user(user)
                .refType(normalizedRefType)
                .refId(refId)
                .amount(amount)
                .method(method)
                .status("PAID")
                .gateway(method)
                .staff(staff)
                .paidAt(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);
    }

    /**
     * 3. Frontend polling kiểm tra trạng thái giao dịch (3 giây/lần)
     */
    @Transactional
    public PaymentInfoResponse getPaymentStatus(String paymentCode, User currentUser) {
        Payment payment = paymentRepository.findByPaymentCode(paymentCode.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giao dịch: " + paymentCode));

        verifyOwnerOrStaffAdmin(payment, currentUser);

        // Nếu đã quá hạn mà Scheduler chưa kịp quét thì đánh dấu hết hạn luôn
        if ("PENDING".equals(payment.getStatus())
                && payment.getExpiresAt() != null
                && payment.getExpiresAt().isBefore(LocalDateTime.now())) {
            expireSinglePayment(payment);
        }

        return PaymentInfoResponse.from(payment, paymentProperties.getSepay().getBankName());
    }

    /**
     * 4. Người dùng / Lễ tân chủ động hủy giao dịch QR đang PENDING
     */
    @Transactional
    public PaymentInfoResponse cancelPayment(String paymentCode, User currentUser) {
        Payment payment = paymentRepository.findByPaymentCode(paymentCode.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giao dịch: " + paymentCode));

        verifyOwnerOrStaffAdmin(payment, currentUser);

        if (!"PENDING".equals(payment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Chỉ có thể hủy giao dịch đang chờ thanh toán (trạng thái hiện tại: " + payment.getStatus() + ")");
        }

        payment.setStatus("CANCELLED");
        paymentRepository.save(payment);
        cancelSourceRecordAndRestoreResources(payment);

        return PaymentInfoResponse.from(payment, paymentProperties.getSepay().getBankName());
    }

    /**
     * 5. Nhận Webhook biến động số dư từ SePay
     */
    @Transactional
    public void handleSePayWebhook(String authorizationHeader, SePayWebhookRequest payload) {
        // 5.1. Xác thực API Key từ Header: Authorization: Apikey <KEY>
        validateWebhookApiKey(authorizationHeader);

        if (payload == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Webhook payload rỗng");
        }

        // 5.2. Chỉ xử lý giao dịch tiền VÀO ("in")
        if (!"in".equalsIgnoreCase(payload.getTransferType())) {
            log.info("[SePay Webhook] Bỏ qua giao dịch không phải tiền vào: type={}", payload.getTransferType());
            return;
        }

        // 5.3. Chống xử lý trùng (Idempotency) theo gatewayTransactionId
        String gwTxId = payload.resolvedTransactionId();
        if (gwTxId != null && paymentRepository.findByGatewayTransactionId(gwTxId).isPresent()) {
            log.info("[SePay Webhook] Giao dịch {} đã được xử lý trước đó, bỏ qua.", gwTxId);
            return;
        }

        // 5.4. Trích xuất paymentCode (FLORD / FLPKG / FLPOS)
        String paymentCode = extractPaymentCode(payload.getCode(), payload.getContent());
        if (paymentCode == null) {
            log.warn("[SePay Webhook] Không tìm thấy mã thanh toán FitLife trong content='{}'", payload.getContent());
            return;
        }

        Optional<Payment> paymentOpt = paymentRepository.findByPaymentCode(paymentCode);
        if (paymentOpt.isEmpty()) {
            log.warn("[SePay Webhook] Không tồn tại Payment với mã {}", paymentCode);
            return;
        }

        Payment payment = paymentOpt.get();
        if (!"PENDING".equals(payment.getStatus())) {
            log.info("[SePay Webhook] Payment {} đang ở trạng thái {}, không xử lý lại.", paymentCode, payment.getStatus());
            return;
        }

        // 5.5. Kiểm tra số tiền thực chuyển >= số tiền cần thanh toán
        BigDecimal receivedAmount = payload.resolvedAmount();
        if (receivedAmount == null || receivedAmount.compareTo(payment.getAmount()) < 0) {
            log.warn("[SePay Webhook] Chuyển thiếu tiền cho {}: nhận={}, yêu cầu={}",
                    paymentCode, receivedAmount, payment.getAmount());
            return;
        }

        String rawJson = serializeQuietly(payload);
        markPaymentAsPaidAndActivate(payment, gwTxId, payload.getReferenceCode(), rawJson);
    }

    /**
     * 6. Giả lập đối soát thành công (chỉ dùng khi dev/test local)
     */
    @Transactional
    public PaymentInfoResponse simulatePaidForDev(String paymentCode) {
        if (!paymentProperties.isDevSimulateEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chế độ giả lập thanh toán đang tắt!");
        }

        Payment payment = paymentRepository.findByPaymentCode(paymentCode.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giao dịch: " + paymentCode));

        if (!"PENDING".equals(payment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giao dịch không ở trạng thái PENDING");
        }

        String fakeTxId = "DEV-" + System.currentTimeMillis();
        markPaymentAsPaidAndActivate(payment, fakeTxId, "DEV-REF-" + payment.getId(), "{\"simulated\":true}");
        return PaymentInfoResponse.from(payment, paymentProperties.getSepay().getBankName());
    }

    /**
     * 7. Dùng bởi Scheduler để hết hạn giao dịch quá 10 phút
     */
    @Transactional
    public void expireSinglePayment(Payment payment) {
        if (!"PENDING".equals(payment.getStatus())) return;
        payment.setStatus("EXPIRED");
        paymentRepository.save(payment);
        cancelSourceRecordAndRestoreResources(payment);
        log.info("[PaymentScheduler] Đã tự động hết hạn thanh toán {} ({} #{})",
                payment.getPaymentCode(), payment.getRefType(), payment.getRefId());
    }

    // ==================== HELPER NỘI BỘ ====================

    private void markPaymentAsPaidAndActivate(Payment payment, String gwTxId, String refCode, String rawJson) {
        payment.setStatus("PAID");
        payment.setPaidAt(LocalDateTime.now());
        payment.setGatewayTransactionId(gwTxId);
        payment.setGatewayReferenceCode(refCode);
        payment.setGatewayRawPayload(rawJson);
        paymentRepository.save(payment);

        if ("ORDER".equals(payment.getRefType()) && payment.getRefId() != null) {
            orderRepository.findById(payment.getRefId()).ifPresent(order -> {
                order.setPaymentStatus(Order.PaymentStatus.PAID);
                if (order.getOrderStatus() == Order.OrderStatus.PENDING) {
                    order.confirm();
                }
                orderRepository.save(order);
            });
        } else if (("ENROLLMENT".equals(payment.getRefType()) || "POS".equals(payment.getRefType()))
                && payment.getRefId() != null) {
            enrollmentRepository.findById(payment.getRefId()).ifPresent(enrollment -> {
                if (enrollment.getStatus() == EnrollmentStatus.PENDING_PAYMENT) {
                    enrollment.setStatus(EnrollmentStatus.ACTIVE);
                    enrollmentRepository.save(enrollment);
                }
            });
        }

        log.info("[SePay] Đã xác nhận PAID & kích hoạt {} #{} (code={})",
                payment.getRefType(), payment.getRefId(), payment.getPaymentCode());
    }

    private void cancelSourceRecordAndRestoreResources(Payment payment) {
        if ("ORDER".equals(payment.getRefType()) && payment.getRefId() != null) {
            orderRepository.findById(payment.getRefId()).ifPresent(order -> {
                if (order.getOrderStatus() == Order.OrderStatus.PENDING) {
                    order.cancel(); // Tự động hoàn tồn kho sản phẩm!
                    order.setPaymentStatus(Order.PaymentStatus.UNPAID);
                    orderRepository.save(order);
                    restoreVoucherUsage(order.getVoucherCode());
                }
            });
        } else if (("ENROLLMENT".equals(payment.getRefType()) || "POS".equals(payment.getRefType()))
                && payment.getRefId() != null) {
            enrollmentRepository.findById(payment.getRefId()).ifPresent(enrollment -> {
                if (enrollment.getStatus() == EnrollmentStatus.PENDING_PAYMENT) {
                    enrollment.setStatus(EnrollmentStatus.CANCELLED);
                    enrollmentRepository.save(enrollment);
                    restoreVoucherUsage(enrollment.getVoucherCode());
                }
            });
        }
    }

    private void restoreVoucherUsage(String voucherCode) {
        if (voucherCode == null || voucherCode.isBlank()) return;
        voucherRepository.findByCodeIgnoreCase(voucherCode.trim()).ifPresent(v -> {
            if (v.getUsedCount() != null && v.getUsedCount() > 0) {
                v.setUsedCount(v.getUsedCount() - 1);
                voucherRepository.save(v);
            }
        });
    }

    private void validateWebhookApiKey(String authHeader) {
        String expectedKey = paymentProperties.getSepay().getWebhookApiKey();
        if (expectedKey == null || expectedKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Chưa cấu hình SePay Webhook API Key");
        }
        if (authHeader == null || authHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Thiếu Header Authorization Apikey");
        }

        String trimmed = authHeader.trim();
        String providedKey = trimmed.toLowerCase(Locale.ROOT).startsWith("apikey ")
                ? trimmed.substring(7).trim()
                : trimmed;

        if (!expectedKey.equals(providedKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "SePay Webhook API Key không hợp lệ");
        }
    }

    private String extractPaymentCode(String codeField, String contentField) {
        for (String candidate : new String[]{codeField, contentField}) {
            if (candidate == null || candidate.isBlank()) continue;
            Matcher matcher = PAYMENT_CODE_PATTERN.matcher(candidate.toUpperCase(Locale.ROOT));
            if (matcher.find()) {
                return matcher.group();
            }
        }
        return null;
    }

    private String buildQrUrl(String accountNumber, String bankCode, BigDecimal amount, String paymentCode) {
        long vndAmount = amount != null ? amount.setScale(0, RoundingMode.HALF_UP).longValue() : 0L;
        return "https://qr.sepay.vn/img?acc=" + urlEncode(accountNumber)
                + "&bank=" + urlEncode(bankCode)
                + "&amount=" + vndAmount
                + "&des=" + urlEncode(paymentCode)
                + "&template=compact2";
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private void verifyOwnerOrStaffAdmin(Payment payment, User currentUser) {
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập!");
        }
        boolean isPrivileged = "ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole())
                || "ROLE_STAFF".equalsIgnoreCase(currentUser.getRole());
        boolean isOwner = payment.getUser() != null && payment.getUser().getId().equals(currentUser.getId());

        if (!isOwner && !isPrivileged) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền thao tác trên giao dịch này!");
        }
    }

    private String serializeQuietly(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}