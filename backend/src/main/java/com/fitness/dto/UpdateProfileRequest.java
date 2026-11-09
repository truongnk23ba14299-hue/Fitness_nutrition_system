package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank(message = "Họ và tên không được để trống")
        String fullName,
        String phone,
        String address,
        Double heightCm,
        Double weightKg,
        String avatarUrl
) {}