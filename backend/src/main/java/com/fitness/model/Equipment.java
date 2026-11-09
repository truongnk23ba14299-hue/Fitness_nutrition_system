package com.fitness.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;              // Tên máy tập (Máy chạy bộ, Máy ép ngực...)

    @Column(name = "brand", length = 100)
    private String brand;             // Hãng sản xuất (Technogym, Life Fitness, Matrix...)

    @Column(name = "image_url", length = 500)
    private String imageUrl;          // Hình ảnh máy tập

    @Column(name = "workout_guide", columnDefinition = "TEXT", nullable = false)
    private String workoutGuide;      // Hướng dẫn chi tiết cách tập / tư thế chuẩn

    @Column(name = "target_muscle", length = 100)
    private String targetMuscle;      // Nhóm cơ tác động chính (Ngực, Đùi, Lưng, Cardio...)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}