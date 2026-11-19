package com.fitness.dto;

import com.fitness.model.TrainerReview;
import java.time.LocalDateTime;

public record ReviewResponse(
        Long id, Long bookingId, Long trainerId, String trainerName,
        Long userId, String userName, Integer rating, String comment,
        LocalDateTime createdAt
) {
    public static ReviewResponse fromEntity(TrainerReview r) {
        return new ReviewResponse(r.getId(), r.getBooking().getId(),
                r.getTrainer().getId(), r.getTrainer().getFullName(),
                r.getUser().getId(), r.getUser().getFullName(),
                r.getRating(), r.getComment(), r.getCreatedAt());
    }
}
