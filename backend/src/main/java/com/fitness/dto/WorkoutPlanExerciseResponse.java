package com.fitness.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkoutPlanExerciseResponse {
    private Long id;
    private Long equipmentId;
    private String equipmentName;
    private String exerciseName;
    private Integer sets;
    private String reps;
    private Integer restSeconds;
    private String note;
    private Integer sortOrder;
}