package com.fitness.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PosSaleRequest(
        // ID gói tập hoặc vé ngày muốn bán (VD: 1 = Daily Pass, 2 = 1 Tháng, 3 = 6 Tháng, 4 = 12 Tháng)
        @NotNull(message = "ID gói tập không được để trống")
        @Positive(message = "ID gói tập phải lớn hơn 0")
        Long packageId,

        // Họ tên khách hàng mua vé/gói
        @NotBlank(message = "Tên khách hàng không được để trống")
        String customerName,

        // Số điện thoại khách hàng
        @NotBlank(message = "Số điện thoại không được để trống")
        String customerPhone,

        // Email khách hàng (tùy chọn)
        String customerEmail,

        // Mã thẻ hội viên nếu khách đã có tài khoản (VD: FL-00012 hoặc 12)
        String memberCode,

        // Hình thức thanh toán: CASH (Tiền mặt), CARD (Quẹt thẻ), VIETQR (Chuyển khoản)
        String paymentMethod,

        // Mã voucher ưu đãi (nếu có)
        String voucherCode,

        // Số tiền giảm giá
        BigDecimal discountAmount,

        // Tổng tiền thực thu sau giảm giá
        BigDecimal totalPrice,

        // Ghi chú của lễ tân
        String notes,

        // Tùy chọn: Tự động check-in ngay sau khi mua (rất tiện cho khách mua vé ngày vào tập luôn)
        Boolean autoCheckIn
) {}