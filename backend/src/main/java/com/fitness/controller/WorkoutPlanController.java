package com.fitness.controller;

import com.fitness.dto.WorkoutPlanResponse;
import com.fitness.service.WorkoutPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workout-plans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    @GetMapping("/my")
    public ResponseEntity<List<WorkoutPlanResponse>> getMyPlans(Authentication authentication) {
        return ResponseEntity.ok(workoutPlanService.getMyPlans(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutPlanResponse> getPlanById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(workoutPlanService.getPlanById(authentication.getName(), id));
    }
}