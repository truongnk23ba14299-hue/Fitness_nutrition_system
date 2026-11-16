package com.fitness.dto;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse (
   Long orderId,
   String customerName,
   String receiverName,
   String  phone,
   BigDecimal totalAmount,
   String shippingAddress,
   String note,
   String status,
   LocalDateTime createdAt,
   List<OrderItemResponse> items

) {
    public record OrderItemResponse(
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subTotal
    ) {}
}



