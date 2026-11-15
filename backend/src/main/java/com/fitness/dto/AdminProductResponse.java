package com.fitness.dto;

import com.fitness.model.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminProductResponse(
        Long id,
        String name,
        String description,
        String brand,
        BigDecimal price,
        Integer stockQuantity,
        String imageUrl,
        BigDecimal weightKg,
        Product.ProductStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
    public static AdminProductResponse fromEntity(Product product) {
        return new AdminProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.getWeightKg(),
                product.getProductStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}


