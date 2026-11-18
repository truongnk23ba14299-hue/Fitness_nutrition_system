package com.fitness.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "trainers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết 1-1 với tài khoản người dùng đăng nhập có role ROLE_TRAINER
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "rating")
    @Builder.Default
    private Double rating = 5.0;

    @Column(name = "specialty", length = 100)
    private String specialty;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Giữ constructor tùy biến cũ để đảm bảo tương thích ngược 100%
    public Trainer(Long id, String fullName, String avatarUrl, String bio, Integer experienceYears, Double rating, String specialty, Boolean isActive) {
        this.id = id;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.experienceYears = experienceYears;
        this.rating = rating != null ? rating : 5.0;
        this.specialty = specialty;
        this.isActive = isActive != null ? isActive : true;
    }
}