package com.fitness.dto;

public record RegisterResponse(
        String message,
        String fullName,
        String email
) {}