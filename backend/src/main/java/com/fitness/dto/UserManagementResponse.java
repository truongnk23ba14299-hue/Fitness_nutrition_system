package com.fitness.dto;

import java.time.LocalDateTime;

public record UserManagementResponse(
        Long id,
        String fullName,
        String email,
        String role,
        String status,
        String phone,
        LocalDateTime createdAt
) {}