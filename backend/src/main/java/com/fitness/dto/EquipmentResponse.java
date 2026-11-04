package com.fitness.dto;

import com.fitness.model.Equipments;

import java.math.BigDecimal;

public record EquipmentResponse(
        Long id,
        String name,
        String description,
        String equipmentBrand,
        BigDecimal price,
        Integer stockQuantity,
        BigDecimal weightKg
){
    public static EquipmentResponse fromEntity(Equipments e){
        return new EquipmentResponse(
                e.getId(),
                e.getName(),
                e.getDescription(),
                e.getBrand(),
                e.getPrice(),
                e.getStockQty(),
                e.getWeightKg()

        );
    }
}









