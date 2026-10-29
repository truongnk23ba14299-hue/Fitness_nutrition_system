package main.java.com.fitness.dto;

import java.math.BigDecimal;

public record EquipmentDTO(
        Long id,
        String name,
        String description,
        String equipmentBrand,
        BigDecimal price,
        Integer stockQuantity,
        BigDecimal weightKg
){}
