package com.fitness.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AddProductRequest(

        @NotBlank(message = "Name is required")
        String name,

        String description,

        @NotBlank(message = "Category is required")
        String category,

        String brand,

        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "Price must >= 0")
        BigDecimal price,

        @NotNull(message = "Stock quantity is required")
        @PositiveOrZero(message = "Stock quantity must >= 0")
        Integer stockQuantity,

        String imageUrl,

        @PositiveOrZero(message = "Weight must >= 0")
        BigDecimal weightKg,

        LocalDateTime created_at,
        LocalDateTime updated_at

) {}
