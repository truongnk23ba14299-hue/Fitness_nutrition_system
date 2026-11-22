package com.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionTargetResponse {
    private Long userId;
    private String gender;
    private Integer age;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private String activityLevel;
    private String goal;
    private Integer bmr;
    private Integer tdee;
    private Integer targetCalories;
    private Integer proteinG;
    private Integer carbsG;
    private Integer fatG;
    private Integer waterMl;
    private String recommendation;
    private String source;
    private Long setByTrainerId;
    private LocalDateTime updatedAt;
}
