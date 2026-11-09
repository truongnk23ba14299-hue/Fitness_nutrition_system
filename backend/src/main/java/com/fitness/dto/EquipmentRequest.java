package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;

public record EquipmentRequest(
        @NotBlank(message = "Tên thiết bị không được để trống")
        String name,

        String brand,

        String imageUrl,

        @NotBlank(message = "Hướng dẫn tập luyện không được để trống")
        String workoutGuide,

        @NotBlank(message = "Vui lòng nhập nhóm cơ tác động")
        String targetMuscle
) {}