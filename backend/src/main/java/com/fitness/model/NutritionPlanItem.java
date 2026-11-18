package com.fitness.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nutrition_plan_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionPlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private NutritionPlan plan;

    @Column(name = "meal_slot", nullable = false, length = 20)
    private String mealSlot; // Sáng, Trưa, Tối, Phụ

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id")
    private MealLibraryItem meal;

    @Column(name = "custom_name", length = 150)
    private String customName;

    private Integer calories;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}