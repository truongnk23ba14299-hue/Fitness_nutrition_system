package com.fitness.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_plan_days", uniqueConstraints = {
        @UniqueConstraint(name = "uk_plan_day", columnNames = {"plan_id", "day_index"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutPlanDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private WorkoutPlan workoutPlan;

    @Column(name = "day_index", nullable = false)
    private Integer dayIndex; // 1 đến 7 (Thứ 2 đến CN)

    @Column(length = 100)
    private String title; // VD: Buổi 1 - Ngực & Tay sau

    @Column(length = 100)
    private String focus; // Nhóm cơ trọng tâm

    @OneToMany(mappedBy = "workoutPlanDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkoutPlanExercise> exercises = new ArrayList<>();
}