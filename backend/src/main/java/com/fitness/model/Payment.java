package com.fitness.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payments_user_time", columnList = "user_id, created_at"),
                @Index(name = "idx_payments_pending", columnList = "status, expires_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_no", nullable = false, unique = true, length = 30)
    private String invoiceNo; // VD: INV-ORD-105, INV-PKG-42

    @Column(name = "payment_code", unique = true, length = 50)
    private String paymentCode; // Nội dung CK duy nhất: FLORD105, FLPKG42, FLPOS18

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ref_type", nullable = false, length = 15)
    private String refType; // ENROLLMENT, ORDER, POS

    @Column(name = "ref_id")
    private Long refId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    private String method; // VIETQR, COD, CASH

    @Column(nullable = false, length = 20)
    @ColumnDefault("'PENDING'")
    @Builder.Default
    private String status = "PENDING"; // PENDING, PAID, EXPIRED, CANCELLED

    @Column(name = "qr_url", columnDefinition = "TEXT")
    private String qrUrl;

    @Column(name = "bank_code", length = 30)
    private String bankCode;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "account_name", length = 150)
    private String accountName;

    @Column(name = "gateway", length = 30)
    @Builder.Default
    private String gateway = "SEPAY";

    @Column(name = "gateway_transaction_id", unique = true, length = 100)
    private String gatewayTransactionId; // ID giao dịch từ SePay (chống xử lý trùng)

    @Column(name = "gateway_reference_code", length = 100)
    private String gatewayReferenceCode; // Mã tham chiếu ngân hàng (FT...)

    @Column(name = "gateway_raw_payload", columnDefinition = "TEXT")
    private String gatewayRawPayload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private User staff; // Lễ tân thu tiền (nếu tại quầy POS)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Chỉ gán giá trị khi thực sự thanh toán thành công (đã bỏ @CreationTimestamp)
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}