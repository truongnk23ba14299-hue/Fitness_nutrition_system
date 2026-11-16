package com.fitness.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateStockRequest(
        @NotNull(message = "Số lượng nhập thêm không được để trống")
        @Positive(message = "Số lượng nhập thêm phải lớn hơn 0")
        Integer additionalStock
){
}
