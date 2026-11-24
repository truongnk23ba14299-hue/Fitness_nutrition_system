package com.fitness.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class WorkoutPlanRequest {
    @NotNull(message = "clientId không được để trống")
    private Long clientId;

    @NotBlank(message = "Tiêu đề giáo án không được để trống")
    private String title;

    private String goal;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;

    @Valid
    private List<WorkoutPlanDayRequest> days = new ArrayList<>();
}