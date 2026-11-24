package com.fitness.dto;

import com.fitness.model.Payment;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PaymentInfoResponse(
        Long id,
        String invoiceNo,
        String paymentCode,
        String refType,
        Long refId,
        BigDecimal amount,
        String method,
        String status,
        String qrUrl,
        String bankCode,
        String bankName,
        String accountNumber,
        String accountName,
        LocalDateTime expiresAt,
        LocalDateTime paidAt
) {
    public static PaymentInfoResponse from(Payment p, String bankName) {
        if (p == null) return null;
        return PaymentInfoResponse.builder()
                .id(p.getId())
                .invoiceNo(p.getInvoiceNo())
                .paymentCode(p.getPaymentCode())
                .refType(p.getRefType())
                .refId(p.getRefId())
                .amount(p.getAmount())
                .method(p.getMethod())
                .status(p.getStatus())
                .qrUrl(p.getQrUrl())
                .bankCode(p.getBankCode())
                .bankName(bankName)
                .accountNumber(p.getAccountNumber())
                .accountName(p.getAccountName())
                .expiresAt(p.getExpiresAt())
                .paidAt(p.getPaidAt())
                .build();
    }
}