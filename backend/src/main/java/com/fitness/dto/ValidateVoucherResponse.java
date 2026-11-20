package com.fitness.dto;

import java.math.BigDecimal;

/**
 * DTO trả về kết quả kiểm tra & tính tiền giảm giá cho Frontend
 */
public record ValidateVoucherResponse(
        boolean valid,
        String message,
        BigDecimal discountAmount,
        BigDecimal finalAmount,
        VoucherResponse voucher
) {
    public static ValidateVoucherResponse invalid(String message, BigDecimal subtotal) {
        BigDecimal fallbackAmount = subtotal != null ? subtotal : BigDecimal.ZERO;
        return new ValidateVoucherResponse(false, message, BigDecimal.ZERO, fallbackAmount, null);
    }

    public static ValidateVoucherResponse success(String message, BigDecimal discountAmount, BigDecimal finalAmount, VoucherResponse voucher) {
        return new ValidateVoucherResponse(true, message, discountAmount, finalAmount, voucher);
    }
}
