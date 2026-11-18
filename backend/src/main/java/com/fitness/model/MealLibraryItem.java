package com.fitness.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "meal_library")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealLibraryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 30)
    private String category; // Bữa sáng, Bữa trưa, Bữa tối, Ăn nhẹ

    @Column(nullable = false)
    private Integer calories;

    @Column(name = "protein_g", precision = 5, scale = 1)
    private BigDecimal proteinG;

    @Column(name = "carbs_g", precision = 5, scale = 1)
    private BigDecimal carbsG;

    @Column(name = "fat_g", precision = 5, scale = 1)
    private BigDecimal fatG;

    @Column(length = 60)
    private String serving; // VD: 1 bát, 200g ức gà

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}