package com.fitness.dto;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String fullName,
        String email,
        String role,
        String phone,
        String address,
        Double heightCm,
        Double weightKg,
        String avatarUrl,
        LocalDateTime createdAt,
        boolean hasActivePackage 
) {}