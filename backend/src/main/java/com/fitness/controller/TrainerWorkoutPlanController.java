package com.fitness.controller;

import com.fitness.dto.WorkoutPlanRequest;
import com.fitness.dto.WorkoutPlanResponse;
import com.fitness.service.WorkoutPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainer/workout-plans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TrainerWorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    @PostMapping
    public ResponseEntity<WorkoutPlanResponse> createPlan(
            Authentication authentication,
            @Valid @RequestBody WorkoutPlanRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutPlanService.createPlan(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutPlanResponse> updatePlan(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody WorkoutPlanRequest request
    ) {
        return ResponseEntity.ok(workoutPlanService.updatePlan(authentication.getName(), id, request));
    }
}