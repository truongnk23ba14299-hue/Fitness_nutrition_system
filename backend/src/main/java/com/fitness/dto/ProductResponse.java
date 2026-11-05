package com.fitness.dto;

import com.fitness.model.Products;
import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String equipmentBrand,
        BigDecimal price,
        Integer stockQuantity,
        BigDecimal weightKg
){
    public static ProductResponse fromEntity(Products p){
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getBrand(),
                p.getPrice(),
                p.getStockQty(),
                p.getWeightKg()
        );
    }
}









