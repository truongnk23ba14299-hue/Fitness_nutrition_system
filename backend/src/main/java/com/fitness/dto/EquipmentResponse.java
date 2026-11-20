package com.fitness.dto;

public record EquipmentResponse(
        Long id,
        String name,
        String brand,
        String imageUrl,
        String targetMuscle,
        String workoutGuide
) {}
