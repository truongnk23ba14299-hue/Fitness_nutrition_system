package com.fitness.dto;

import com.fitness.model.Products;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductDetailResponse(
        Long id,
        String name,
        String description,
        String equipmentBrand,
        BigDecimal price,
        Integer stockQuantity,
        String image_url,
        BigDecimal weightKg,
        LocalDateTime created_at,
        LocalDateTime updated_at
){
    public static ProductDetailResponse fromEntity(Products product) {
        if (product == null) {
            throw new IllegalStateException("Sản phẩm ko đc null");
        }
        if (product.getId() == null
            || product.getName() == null
            || product.getName().isBlank()
            || product.getCategory() == null
            || product.getCategory().isBlank()
            || product.getPrice() == null
            || product.getPrice().signum() < 0
            || product.getStockQuantity() == null
            || product.getStockQuantity() < 0 ){

            throw new IllegalStateException("Sản phẩm với id ko hợp lệ: " +product.getId());
        }
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.getWeightKg(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}


