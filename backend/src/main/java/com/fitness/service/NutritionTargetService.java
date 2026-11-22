package com.fitness.service;

import com.fitness.dto.NutritionTargetResponse;
import com.fitness.model.NutritionTarget;
import com.fitness.repository.NutritionTargetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NutritionTargetService {

    private final NutritionTargetRepository nutritionTargetRepository;

    public NutritionTargetResponse getNutritionTarget(Long targetUserId, Long currentUserId, String currentRole) {
        validateAccess(targetUserId, currentUserId, currentRole);

        NutritionTarget target = nutritionTargetRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("Chưa có mục tiêu dinh dưỡng cho user id: " + targetUserId));

        return toResponse(target);
    }

    private void validateAccess(Long targetUserId, Long currentUserId, String currentRole) {
        boolean isOwner = currentUserId != null && currentUserId.equals(targetUserId);
        boolean isTrainer = currentRole != null && currentRole.toUpperCase().contains("TRAINER");
        boolean isAdmin = currentRole != null && currentRole.toUpperCase().contains("ADMIN");

        if (!isOwner && !isTrainer && !isAdmin) {
            throw new AccessDeniedException("Không có quyền xem mục tiêu dinh dưỡng của user id: " + targetUserId);
        }
    }

    public NutritionTargetResponse toResponse(NutritionTarget target) {
        Long trainerId = target.getSetByTrainer() != null ? target.getSetByTrainer().getId() : null;

        return NutritionTargetResponse.builder()
                .userId(target.getUserId())
                .gender(target.getGender())
                .age(target.getAge())
                .heightCm(target.getHeightCm())
                .weightKg(target.getWeightKg())
                .activityLevel(target.getActivityLevel())
                .goal(target.getGoal())
                .bmr(target.getBmr())
                .tdee(target.getTdee())
                .targetCalories(target.getTargetCalories())
                .proteinG(target.getProteinG())
                .carbsG(target.getCarbsG())
                .fatG(target.getFatG())
                .waterMl(target.getWaterMl())
                .recommendation(target.getRecommendation())
                .source(target.getSource())
                .setByTrainerId(trainerId)
                .updatedAt(target.getUpdatedAt())
                .build();
    }
}