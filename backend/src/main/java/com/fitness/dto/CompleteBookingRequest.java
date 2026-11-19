package com.fitness.dto;

import jakarta.validation.constraints.Size;

public record CompleteBookingRequest(@Size(max = 2000) String trainerNote) {}
