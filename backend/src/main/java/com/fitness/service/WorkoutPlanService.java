package com.fitness.service;

import com.fitness.dto.*;
import com.fitness.Exception.ResourceNotFoundException;
import com.fitness.model.*;
import com.fitness.repository.EquipmentRepository;
import com.fitness.repository.TrainerRepository;
import com.fitness.repository.UserRepository;
import com.fitness.repository.WorkoutPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

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

        attachDaysAndExercises(plan, request.getDays(), false);
        return toResponse(workoutPlanRepository.save(plan));
    }

    @Transactional
    public WorkoutPlanResponse updatePlan(String trainerEmail, Long planId, WorkoutPlanRequest request) {
        User currentUser = userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase(trainerEmail, trainerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found: " + planId));

        if (!isAdminRole(currentUser.getRole())) {
            Trainer trainer = getOrCreateTrainerProfile(currentUser);
            if (plan.getTrainer() == null || !plan.getTrainer().getId().equals(trainer.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to update this workout plan");
            }
        }

        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must not be before start date");
        }

        if (request.getDays() != null) {
            Set<Integer> seenDays = new HashSet<>();
            for (WorkoutPlanDayRequest dayReq : request.getDays()) {
                if (dayReq.getDayIndex() != null && !seenDays.add(dayReq.getDayIndex())) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Duplicate dayIndex: " + dayReq.getDayIndex()
                    );
                }
            }
        }

        if (request.getClientId() != null) {
            Long currentClientId = plan.getClient() != null ? plan.getClient().getId() : null;
            if (!request.getClientId().equals(currentClientId)) {
                User newClient = userRepository.findById(request.getClientId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Client not found: " + request.getClientId()
                        ));
                plan.setClient(newClient);
            }
        }

        plan.setTitle(request.getTitle().trim());
        plan.setGoal(request.getGoal() != null ? request.getGoal().trim() : null);

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            String normalizedStatus = normalizeStatus(request.getStatus());
            if (normalizedStatus.length() > 15) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must not exceed 15 characters");
            }
            plan.setStatus(normalizedStatus);
        }

        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());

        if (plan.getDays() == null) {
            plan.setDays(new ArrayList<>());
        } else {
            plan.getDays().clear();
        }
        workoutPlanRepository.flush();

        attachDaysAndExercises(plan, request.getDays(), true);

        return toResponse(workoutPlanRepository.save(plan));
    }

    private boolean isAdminRole(String role) {
        if (role == null) {
            return false;
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        return "ADMIN".equals(normalized) || "ROLE_ADMIN".equals(normalized);
    }

    private void attachDaysAndExercises(
            WorkoutPlan plan,
            List<WorkoutPlanDayRequest> dayRequests,
            boolean useHttpStatus
    ) {
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
                                .orElseThrow(() -> useHttpStatus
                                        ? new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found: " + exReq.getEquipmentId())
                                        : new RuntimeException("Equipment not found: " + exReq.getEquipmentId()));
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