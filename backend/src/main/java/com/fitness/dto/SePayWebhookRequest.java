package com.fitness.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SePayWebhookRequest {
    private Long id;
    private String transactionId;
    private String gateway;
    private String transactionDate;
    private String accountNumber;
    private String subAccount;
    private String code;
    private String content;
    private String transferType;       // "in" hoặc "out"
    private BigDecimal transferAmount; // Chuẩn Webhook SePay
    private BigDecimal amount;         // Dự phòng cho SePay Sandbox
    private String referenceCode;
    private String description;

    public BigDecimal resolvedAmount() {
        return transferAmount != null ? transferAmount : amount;
    }

    public String resolvedTransactionId() {
        if (id != null) return String.valueOf(id);
        return transactionId;
    }
}