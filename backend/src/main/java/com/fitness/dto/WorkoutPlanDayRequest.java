package com.fitness.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WorkoutPlanDayRequest {
    @NotNull(message = "dayIndex không được để trống (1-7)")
    @Min(value = 1, message = "dayIndex phải từ 1 đến 7")
    @Max(value = 7, message = "dayIndex phải từ 1 đến 7")
    private Integer dayIndex;

    private String title;
    private String focus;

    @Valid
    private List<WorkoutPlanExerciseRequest> exercises = new ArrayList<>();
}