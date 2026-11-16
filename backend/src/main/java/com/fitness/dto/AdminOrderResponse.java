package com.fitness.dto;

import com.fitness.model.Order;
import com.fitness.model.OrderItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Response cho Admin — đặt tên field khớp với OrderResponse của tuantinhte17-droid
public record AdminOrderResponse(
        Long orderId,
        Long userId,
        String customerName,
        String customerEmail,
        String receiverName,
        String phone,
        String shippingAddress,
        String note,
        BigDecimal totalAmount,
        Order.OrderStatus status,
        List<AdminOrderItemResponse> items,
        LocalDateTime createdAt
){
    public static AdminOrderResponse fromEntity(Order order) {
        return new AdminOrderResponse(
                order.getId(),
                order.getUser() != null ? order.getUser().getId() : null,
                order.getUser() != null ? order.getUser().getFullName() : null,
                order.getUser() != null ? order.getUser().getEmail() : null,
                order.getReceiverName(),
                order.getPhone(),
                order.getShippingAddress(),
                order.getNote(),
                order.getTotalAmount(),
                order.getOrderStatus(),
                order.getItems().stream()
                        .map(AdminOrderItemResponse::fromEntity)
                        .toList(),
                order.getCreatedAt()
        );
    }
}
