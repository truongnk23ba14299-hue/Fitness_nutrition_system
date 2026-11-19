package com.fitness.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record RescheduleBookingRequest(@NotNull LocalDateTime bookingDateTime) {}
