package com.fitness.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponse (
    Long orderId,
    String status,
    BigDecimal totalAmount,
    int itemCount,
    LocalDateTime createdAt
){}
