package com.fitness.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateTrainerStatusRequest(
        @NotNull(message = "Trạng thái hoạt động (isActive) không được để trống")
        Boolean isActive
) {}