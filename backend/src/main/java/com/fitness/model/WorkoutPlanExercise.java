package com.fitness.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workout_plan_exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutPlanExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    private WorkoutPlanDay workoutPlanDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment; // Liên kết tới máy tập để xem video hướng dẫn

    @Column(name = "exercise_name", nullable = false, length = 200)
    private String exerciseName;

    @Column(nullable = false)
    private Integer sets;

    @Column(nullable = false, length = 20)
    private String reps; // VD: 12-15

    @Column(name = "rest_seconds")
    private Integer restSeconds; // Nghỉ giữa hiệp (giây)

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}