package com.fitness.dto;

import com.fitness.model.Order;
import com.fitness.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminOrderResponse(
        Long id,
        Long userId,
        String customerName,
        String customerEmail,
        String recipientName,
        String recipientPhone,
        String shippingAddress,
        String note,
        BigDecimal totalPrice,
        OrderStatus status,
        List<AdminOrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
    public static AdminOrderResponse fromEntity(Order order, String customerName, String customerEmail) {
        return new AdminOrderResponse(
                order.getId(),
                order.getUserId(),
                customerName,
                customerEmail,
                order.getRecipientName(),
                order.getRecipientPhone(),
                order.getShippingAddress(),
                order.getNote(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getItems().stream()
                        .map(AdminOrderItemResponse::fromEntity)
                        .toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
