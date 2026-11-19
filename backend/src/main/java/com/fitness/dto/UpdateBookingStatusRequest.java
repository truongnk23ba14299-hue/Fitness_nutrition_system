package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateBookingStatusRequest(
        @NotBlank @Pattern(regexp = "(?i)CONFIRMED|CANCELLED|REJECTED|PENDING") String status
) {}
