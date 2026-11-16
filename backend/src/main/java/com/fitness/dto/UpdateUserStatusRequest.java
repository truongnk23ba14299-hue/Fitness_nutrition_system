package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateUserStatusRequest(
        @NotBlank(message = "Trạng thái không được để trống")
        @Pattern(regexp = "^(ACTIVE|LOCKED)$", message = "Trạng thái chỉ có thể là ACTIVE hoặc LOCKED")
        String status
) {}