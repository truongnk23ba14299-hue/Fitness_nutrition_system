package com.fitness.dto;

import com.fitness.model.Voucher;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VoucherResponse (
    Long id,
    String code,
    String title,
    Voucher.DiscountType discountType,
    BigDecimal discountValue,
    BigDecimal minOrderValue,
    BigDecimal maxDiscount,
    Integer quantity,
    Integer usedCount,
    Boolean active,
    LocalDateTime startDate,
    LocalDateTime expiryDate

){

    public static VoucherResponse from(Voucher v){
        return new VoucherResponse(
                v.getId(),
                v.getCode(),
                v.getTitle(),
                v.getDiscountType(),
                v.getDiscountValue(),
                v.getMinOrderValue(),
                v.getMaxDiscount(),
                v.getQuantity(),
                v.getUsedCount(),
                v.getIsActive(),
                v.getStartDate(),
                v.getExpiryDate()

        );
    }
}
