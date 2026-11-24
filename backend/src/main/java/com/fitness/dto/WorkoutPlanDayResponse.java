package com.fitness.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WorkoutPlanDayResponse {
    private Long id;
    private Integer dayIndex;
    private String title;
    private String focus;
    private List<WorkoutPlanExerciseResponse> exercises;
}