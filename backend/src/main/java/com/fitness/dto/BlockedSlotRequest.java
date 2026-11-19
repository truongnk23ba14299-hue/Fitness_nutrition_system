package com.fitness.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record BlockedSlotRequest(
        @NotNull LocalDateTime slotStart,
        @Size(max = 200) String reason,
        @NotNull Boolean blocked
) {}
