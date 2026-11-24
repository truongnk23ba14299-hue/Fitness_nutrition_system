package com.fitness.dto;

import com.fitness.model.Trainer;

import java.time.LocalDateTime;

/**
 * Thông tin hồ sơ cá nhân (Bio) của HLV đang đăng nhập.
 */
public record TrainerProfileResponse(
        Long trainerId,
        Long userId,
        String fullName,
        String email,
        String avatarUrl,
        String bio,
        Integer experienceYears,
        String specialty,
        Double rating,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TrainerProfileResponse fromEntity(Trainer trainer) {
        if (trainer == null) {
            return null;
        }
        return new TrainerProfileResponse(
                trainer.getId(),
                trainer.getUser() != null ? trainer.getUser().getId() : null,
                trainer.getFullName(),
                trainer.getUser() != null ? trainer.getUser().getEmail() : null,
                trainer.getAvatarUrl(),
                trainer.getBio(),
                trainer.getExperienceYears(),
                trainer.getSpecialty(),
                trainer.getRating(),
                trainer.getIsActive(),
                trainer.getCreatedAt(),
                trainer.getUpdatedAt()
        );
    }
}
