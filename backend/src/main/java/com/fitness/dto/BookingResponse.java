package com.fitness.dto;

import com.fitness.model.Booking;

import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long userId,
        String userName,
        String userEmail,
        Long trainerId,
        String trainerName,
        LocalDateTime bookingDateTime,
        String note,
        String status,
        String trainerNote,
        LocalDateTime completedAt
) {
    public static BookingResponse fromEntity(Booking booking) {
        if (booking == null) {
            return null;
        }

        Long userId = booking.getUser() != null ? booking.getUser().getId() : null;
        String userName = booking.getUser() != null ? booking.getUser().getFullName() : null;
        String userEmail = booking.getUser() != null ? booking.getUser().getEmail() : null;

        Long trainerId = booking.getTrainer() != null ? booking.getTrainer().getId() : null;
        String trainerName = booking.getTrainer() != null ? booking.getTrainer().getFullName() : null;

        return new BookingResponse(
                booking.getId(),
                userId,
                userName,
                userEmail,
                trainerId,
                trainerName,
                booking.getBookingDateTime(),
                booking.getNote(),
                booking.getStatus(),
                booking.getTrainerNote(),
                booking.getCompletedAt()
        );
    }
}
