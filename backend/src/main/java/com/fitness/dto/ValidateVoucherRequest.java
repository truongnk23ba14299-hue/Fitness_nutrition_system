package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * DTO nhận dữ liệu kiểm tra mã giảm giá từ Frontend
 */
public record ValidateVoucherRequest(
        @NotBlank(message = "Mã giảm giá không được để trống")
        String code,

        @NotNull(message = "Tổng tiền đơn hàng không được để trống")
        @PositiveOrZero(message = "Tổng tiền đơn hàng phải lớn hơn hoặc bằng 0")
        BigDecimal subtotal,

        String scope // PACKAGE, PRODUCT, ALL
) {
}
