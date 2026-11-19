package com.fitness.dto;

import com.fitness.model.Voucher;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateVoucherRequest (
    @NotBlank(message= "Voucher code is required")
    @Size(max = 50, message="Code must not exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Code may only contain letters, digits, '_' and '-'")
    String code,

    @NotBlank(message= "Title is required")
    @Size(max=200, message="Tilte must not exceed ")
    String title,

    @NotNull(message="Discount TYPE is required")
    Voucher.DiscountType discountType,

    @NotNull(message="Discount value is required")
    @DecimalMin(value="0.01", message="Discount value must > 0")
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

){}
