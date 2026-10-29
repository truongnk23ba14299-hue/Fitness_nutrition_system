package main.java.com.fitness.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EquipmentDetailDTO(
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
}


