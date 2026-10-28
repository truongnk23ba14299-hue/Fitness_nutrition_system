package com.fitness.dto;
public record  RegisterResponse(
        Long id,
        String fullName,
        String email,
        String password,
        String role
) {
}