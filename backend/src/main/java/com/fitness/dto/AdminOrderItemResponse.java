package com.fitness.dto;

import com.fitness.model.OrderItem;
import java.math.BigDecimal;

// Đặt tên field khớp với OrderItemResponse của tuantinhte17-droid
public record AdminOrderItemResponse(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subTotal
){
    public static AdminOrderItemResponse fromEntity(OrderItem item) {
        return new AdminOrderItemResponse(
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubTotal()
        );
    }
}
