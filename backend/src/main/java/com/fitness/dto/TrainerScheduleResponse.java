package com.fitness.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record TrainerScheduleResponse(
        Long trainerId,
        String trainerName,
        LocalDate date,
        List<ScheduleSlot> slots,
        List<BookingResponse> busyBookings
) {
    public record ScheduleSlot(
            LocalTime startTime,
            LocalTime endTime,
            boolean available,
            String status
    ) {}
}
