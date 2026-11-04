package com.fitness.dto;

import com.fitness.model.Equipments;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EquipmentDetailResponse(
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
    public static EquipmentDetailResponse fromEntity(Equipments equipment) {
        if (equipment == null) {
            throw new IllegalStateException("Dụng cụ ko đc null");
        }

        if (equipment.getId() == null
                || equipment.getName() == null
                || equipment.getName().isBlank()
                || equipment.getCategory() == null
                || equipment.getCategory().isBlank()
                || equipment.getPrice() == null
                || equipment.getPrice().signum() < 0
                || equipment.getStockQty() == null
                || equipment.getStockQty() < 0 ){

            throw new IllegalStateException("Dụng cụ với id ko hợp lệ: " +equipment.getId());
        }

        return new EquipmentDetailResponse(
                equipment.getId(),
                equipment.getName(),
                equipment.getDescription(),
                equipment.getBrand(),
                equipment.getPrice(),
                equipment.getStockQty(),
                equipment.getImageUrl(),
                equipment.getWeightKg(),
                equipment.getCreatedAt(),
                equipment.getUpdatedAt()

        );
    }
}


