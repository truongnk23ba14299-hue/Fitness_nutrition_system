package com.fitness.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "nutrition_targets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionTarget {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 10)
    private String gender;

    private Integer age;

    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "activity_level", length = 25)
    private String activityLevel;

    @Column(length = 25)
    private String goal;

    private Integer bmr;
    private Integer tdee;

    @Column(name = "target_calories", nullable = false)
    private Integer targetCalories;

    @Column(name = "protein_g", nullable = false)
    private Integer proteinG;

    @Column(name = "carbs_g", nullable = false)
    private Integer carbsG;

    @Column(name = "fat_g", nullable = false)
    private Integer fatG;

    @Column(name = "water_ml")
    private Integer waterMl;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(length = 10, nullable = false)
    @Builder.Default
    private String source = "SELF"; // SELF hoặc TRAINER

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_by_trainer_id")
    private Trainer setByTrainer;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}