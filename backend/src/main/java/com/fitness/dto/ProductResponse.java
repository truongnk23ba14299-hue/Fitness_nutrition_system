package com.fitness.dto;

import com.fitness.model.Product;
import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String brand,
        BigDecimal price
){
    public static ProductResponse fromEntity(Product p){
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getBrand(),
                p.getPrice()
        );
    }
}









