package com.fitness.dto;

import com.fitness.model.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateEnrollmentStatusRequest(
        @NotNull(message = "Trạng thái mới không được để trống (ACTIVE, EXPIRED, CANCELLED)")
        EnrollmentStatus status
) {}