package com.fitness.dto;

import com.fitness.model.Voucher;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateVoucherRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must be at most 200 characters")
        String title,

        @NotNull(message = "Discount type is required")
        Voucher.DiscountType discountType,

        @NotNull(message = "Discount value is required")
        @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
        BigDecimal discountValue,

        @DecimalMin(value = "0.01", message = "Max discount must be greater than 0")
        BigDecimal maxDiscount,

        @DecimalMin(value = "0.00", message = "Min order value must not be negative")
        BigDecimal minOrderValue,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        @Pattern(regexp = "ALL|PACKAGE|PRODUCT", message = "Scope must be ALL, PACKAGE or PRODUCT")
        String scope,

        Boolean isActive,

        @NotNull(message = "Start date is required")
        LocalDateTime startedDate,

        @NotNull(message = "Expiry date is required")
        LocalDateTime expiryDate
) {}
