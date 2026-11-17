package com.fitness.dto;

import java.time.LocalDateTime;

public record EquipmentDetailResponse(
        Long id,
        String name,
        String brand,
        String imageUrl,
        String videoUrl,
        String workoutGuide,
        String targetMuscle,
        LocalDateTime createdAt
) {}