package com.fitness.dto;

import com.fitness.model.Booking;
import java.time.LocalDateTime;

public record BookPtResponse(
        Long id,
        Long userId,
        Long trainerId,
        String trainerName,
        LocalDateTime bookingDateTime,
        String note,
        String status
) {
    public static BookPtResponse fromEntity(Booking booking) {
        return new BookPtResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getTrainer().getId(),
                booking.getTrainer().getFullName(),
                booking.getBookingDateTime(),
                booking.getNote(),
                booking.getStatus()
        );
    }
}
