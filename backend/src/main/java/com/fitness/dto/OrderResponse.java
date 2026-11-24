package com.fitness.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        String customerName,
        String receiverName,
        String phone,
        BigDecimal totalAmount,
        String shippingAddress,
        String note,
        String status,
        LocalDateTime createdAt,
        List<OrderItemResponse> items,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        String voucherCode,
        String paymentMethod,
        String paymentStatus,
        PaymentInfoResponse payment
) {
    public record OrderItemResponse(
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subTotal
    ) {}
}



