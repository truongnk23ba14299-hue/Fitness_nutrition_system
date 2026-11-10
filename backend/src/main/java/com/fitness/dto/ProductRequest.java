package com.fitness.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must be at most 255 characters")
        String name,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        @NotBlank(message = "Category is required")
        @Size(max = 255, message = "Category must be at most 255 characters")
        String category,

        @Size(max = 255, message = "Brand must be at most 255 characters")
        String brand,

        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "Price must be >= 0")
        @Digits(integer = 10, fraction = 2, message = "Price format is invalid")
        BigDecimal price,

        @NotNull(message = "Stock quantity is required")
        @PositiveOrZero(message = "Stock quantity must be >= 0")
        Integer stockQuantity,

        @Size(max = 500, message = "Image URL must be at most 500 characters")
        String imageUrl,

        @Positive(message = "Weight must be > 0")
        @Digits(integer = 6, fraction = 3, message = "Weight format is invalid")
        BigDecimal weightKg

) {}