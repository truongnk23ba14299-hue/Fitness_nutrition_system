package com.fitness.dto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public record OrderItemRequest (
    @NotNull(message= "Product ID is required")
    Long productId,

    @NotNull(message= "Quantity is required")
    @Min(value= 1, message= "Quantity must be at least 1")
    @Max(value = 50, message = "Quantity must not exceed 100")
    Integer quantity

){}
