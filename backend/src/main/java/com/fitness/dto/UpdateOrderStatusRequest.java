package com.fitness.dto;

import com.fitness.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Trạng thái đơn hàng không được để trống")
        OrderStatus status
){
}
