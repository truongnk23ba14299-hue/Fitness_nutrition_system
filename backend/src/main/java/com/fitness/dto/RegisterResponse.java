package com.fitness.dto;
import java.time.LocalDateTime;

public record RegisterResponse(
        Long id,
        String fullName,
        String email,
        String role,
        LocalDateTime createdAt
) {}