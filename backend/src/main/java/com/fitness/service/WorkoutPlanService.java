package com.fitness.service;

import com.fitness.dto.*;
import com.fitness.Exception.ResourceNotFoundException;
import com.fitness.model.*;
import com.fitness.repository.EquipmentRepository;
import com.fitness.repository.TrainerRepository;
import com.fitness.repository.UserRepository;
import com.fitness.repository.WorkoutPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class WorkoutPlanService {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final UserRepository userRepository;
    private final TrainerRepository trainerRepository;
    private final EquipmentRepository equipmentRepository;

    @Transactional(readOnly = true)
    public List<WorkoutPlanResponse> getMyPlans(String email) {
        User user = getUserByEmail(email);
        return workoutPlanRepository.findByClient_IdOrderByCreatedAtDescIdDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutPlanResponse getPlanById(String email, Long planId) {
        User currentUser = getUserByEmail(email);
        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout plan not found: " + planId));

        boolean isClientOwner = plan.getClient() != null && plan.getClient().getId().equals(currentUser.getId());
        boolean isAssignedTrainer = plan.getTrainer() != null
                && plan.getTrainer().getUser() != null
                && plan.getTrainer().getUser().getId().equals(currentUser.getId());

        if (!isClientOwner && !isAssignedTrainer) {
            throw new RuntimeException("Bạn không có quyền xem giáo án này");
        }

        return toResponse(plan);
    }

    @Transactional
    public WorkoutPlanResponse createPlan(String trainerEmail, WorkoutPlanRequest request) {
        User trainerUser = getUserByEmail(trainerEmail);
        Trainer trainer = getOrCreateTrainerProfile(trainerUser);

        User client = userRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học viên với id = " + request.getClientId()));

        WorkoutPlan plan = WorkoutPlan.builder()
                .client(client)
                .trainer(trainer)
                .title(request.getTitle().trim())
                .goal(request.getGoal())
                .status(normalizeStatus(request.getStatus()))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        attachDaysAndExercises(plan, request.getDays());
        return toResponse(workoutPlanRepository.save(plan));
    }

    @Transactional
    public WorkoutPlanResponse updatePlan(String trainerEmail, Long planId, WorkoutPlanRequest request) {
        User trainerUser = getUserByEmail(trainerEmail);
        Trainer trainer = getOrCreateTrainerProfile(trainerUser);

        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout plan not found: " + planId));

        if (plan.getTrainer() == null || !plan.getTrainer().getId().equals(trainer.getId())) {
            throw new RuntimeException("Bạn không có quyền cập nhật giáo án này");
        }

        if (request.getClientId() != null && !request.getClientId().equals(plan.getClient().getId())) {
            User newClient = userRepository.findById(request.getClientId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy học viên với id = " + request.getClientId()));
            plan.setClient(newClient);
        }

        plan.setTitle(request.getTitle().trim());
        plan.setGoal(request.getGoal());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            plan.setStatus(normalizeStatus(request.getStatus()));
        }
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());

        plan.getDays().clear();

        workoutPlanRepository.flush();

        attachDaysAndExercises(plan, request.getDays());

        return toResponse(workoutPlanRepository.save(plan));
    }

    private void attachDaysAndExercises(WorkoutPlan plan, List<WorkoutPlanDayRequest> dayRequests) {
        if (dayRequests == null) {
            return;
        }
        for (WorkoutPlanDayRequest dayReq : dayRequests) {
            WorkoutPlanDay day = WorkoutPlanDay.builder()
                    .workoutPlan(plan)
                    .dayIndex(dayReq.getDayIndex())
                    .title(dayReq.getTitle())
                    .focus(dayReq.getFocus())
                    .build();

            List<WorkoutPlanExercise> exerciseEntities = new ArrayList<>();
            if (dayReq.getExercises() != null) {
                int defaultOrder = 0;
                for (WorkoutPlanExerciseRequest exReq : dayReq.getExercises()) {
                    Equipment equipment = null;
                    if (exReq.getEquipmentId() != null) {
                        equipment = equipmentRepository.findById(exReq.getEquipmentId())
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị với id = " + exReq.getEquipmentId()));
                    }
                    WorkoutPlanExercise ex = WorkoutPlanExercise.builder()
                            .workoutPlanDay(day)
                            .equipment(equipment)
                            .exerciseName(exReq.getExerciseName().trim())
                            .sets(exReq.getSets())
                            .reps(exReq.getReps().trim())
                            .restSeconds(exReq.getRestSeconds())
                            .note(exReq.getNote())
                            .sortOrder(exReq.getSortOrder() != null ? exReq.getSortOrder() : defaultOrder)
                            .build();
                    exerciseEntities.add(ex);
                    defaultOrder++;
                }
            }
            day.setExercises(exerciseEntities);
            plan.getDays().add(day);
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ACTIVE";
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private Trainer getOrCreateTrainerProfile(User trainerUser) {
        return trainerRepository.findByUser_Id(trainerUser.getId())
                .orElseGet(() -> trainerRepository.save(
                        Trainer.builder()
                                .user(trainerUser)
                                .fullName(trainerUser.getFullName())
                                .experienceYears(0)
                                .specialty("General")
                                .build()
                ));
    }
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }

    private WorkoutPlanResponse toResponse(WorkoutPlan plan) {
        List<WorkoutPlanDayResponse> dayResponses = plan.getDays() == null ? List.of() :
                plan.getDays().stream()
                        .sorted(Comparator.comparing(WorkoutPlanDay::getDayIndex, Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(day -> {
                            List<WorkoutPlanExerciseResponse> exResponses = day.getExercises() == null ? List.of() :
                                    day.getExercises().stream()
                                            .sorted(Comparator.comparing(WorkoutPlanExercise::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                                                    .thenComparing(WorkoutPlanExercise::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                                            .map(ex -> WorkoutPlanExerciseResponse.builder()
                                                    .id(ex.getId())
                                                    .equipmentId(ex.getEquipment() != null ? ex.getEquipment().getId() : null)
                                                    .equipmentName(ex.getEquipment() != null ? ex.getEquipment().getName() : null)
                                                    .exerciseName(ex.getExerciseName())
                                                    .sets(ex.getSets())
                                                    .reps(ex.getReps())
                                                    .restSeconds(ex.getRestSeconds())
                                                    .note(ex.getNote())
                                                    .sortOrder(ex.getSortOrder())
                                                    .build())
                                            .toList();

                            return WorkoutPlanDayResponse.builder()
                                    .id(day.getId())
                                    .dayIndex(day.getDayIndex())
                                    .title(day.getTitle())
                                    .focus(day.getFocus())
                                    .exercises(exResponses)
                                    .build();
                        })
                        .toList();

        return WorkoutPlanResponse.builder()
                .id(plan.getId())
                .clientId(plan.getClient() != null ? plan.getClient().getId() : null)
                .clientName(plan.getClient() != null ? plan.getClient().getFullName() : null)
                .trainerId(plan.getTrainer() != null ? plan.getTrainer().getId() : null)
                .trainerName(plan.getTrainer() != null ? plan.getTrainer().getFullName() : null)
                .title(plan.getTitle())
                .goal(plan.getGoal())
                .status(plan.getStatus())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .createdAt(plan.getCreatedAt())
                .days(dayResponses)
                .build();
    }
}