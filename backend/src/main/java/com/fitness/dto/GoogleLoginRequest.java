package com.fitness.dto;
import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest (
     @NotBlank(message="Google Id Token is required")
     String idToken
){ }
