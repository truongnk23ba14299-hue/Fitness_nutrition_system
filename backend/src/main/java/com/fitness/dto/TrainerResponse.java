package com.fitness.dto;

import com.fitness.model.Trainer;

public record TrainerResponse(
        Long id,
        String fullName,
        String avatarUrl,
        String bio,
        Integer experienceYears,
        Double rating,
        String specialty,
        Boolean isActive
) {
    public static TrainerResponse fromEntity(Trainer trainer) {
        if (trainer == null) {
            return null;
        }
        return new TrainerResponse(
                trainer.getId(),
                trainer.getFullName(),
                trainer.getAvatarUrl(),
                trainer.getBio(),
                trainer.getExperienceYears(),
                trainer.getRating(),
                trainer.getSpecialty(),
                trainer.getIsActive()
        );
    }
}
