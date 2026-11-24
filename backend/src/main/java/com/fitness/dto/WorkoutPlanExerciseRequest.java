package com.fitness.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkoutPlanExerciseRequest {
    private Long equipmentId;

    @NotBlank(message = "Tên bài tập không được để trống")
    private String exerciseName;

    @NotNull(message = "Số hiệp (sets) không được để trống")
    @Min(value = 1, message = "Số hiệp phải lớn hơn hoặc bằng 1")
    private Integer sets;

    @NotBlank(message = "Số lần lặp (reps) không được để trống")
    private String reps;

    private Integer restSeconds;
    private String note;
    private Integer sortOrder;
}