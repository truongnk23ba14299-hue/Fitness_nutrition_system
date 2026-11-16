package com.fitness.dto;

import com.fitness.model.Order;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Trạng thái đơn hàng không được để trống")
        Order.OrderStatus status
){
}
