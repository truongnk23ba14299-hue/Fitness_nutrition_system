package com.fitness.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private Double price;

    @Column(name = "duration_months")
    private Integer durationMonths;

    @Column(length = 1000)
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "schedule_details", columnDefinition = "TEXT")
    private String scheduleDetails;

    @Column(name = "trainer_info", columnDefinition = "TEXT")
    private String trainerInfo;
}
