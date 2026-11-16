package com.fitness.dto;

import com.fitness.model.OrderItem;
import java.math.BigDecimal;

public record AdminOrderItemResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal price,
        Integer quantity,
        BigDecimal lineTotal
){
    public static AdminOrderItemResponse fromEntity(OrderItem item) {
        return new AdminOrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getPrice(),
                item.getQuantity(),
                item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
        );
    }
}
