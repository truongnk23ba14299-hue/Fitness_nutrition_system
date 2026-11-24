package com.fitness.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class WorkoutPlanResponse {
    private Long id;
    private Long clientId;
    private String clientName;
    private Long trainerId;
    private String trainerName;
    private String title;
    private String goal;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private List<WorkoutPlanDayResponse> days;
}