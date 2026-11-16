package com.fitness.dto;

import com.fitness.model.Product;
import java.math.BigDecimal;

public record ProductDetailResponse(
        Long id,
        String name,
        String description,
        String brand,
        BigDecimal price,
        String imageUrl,
        String category,
        // Minh Quang: không trả số tồn kho chính xác cho public, chỉ trả còn hàng hay hết hàng
        boolean inStock
){
    public static ProductDetailResponse fromEntity(Product product) {
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getPrice(),
                product.getImageUrl(),
                product.getCategory(),
                product.getStockQuantity() != null && product.getStockQuantity() > 0
        );
    }
}


