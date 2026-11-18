package com.fitness.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "checkins", indexes = {
        @Index(name = "idx_checkins_user_time", columnList = "user_id, checked_in_at"),
        @Index(name = "idx_checkins_time", columnList = "checked_in_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id")
    private PackageEnrollment enrollment;

    @Column(length = 15, nullable = false)
    @Builder.Default
    private String method = "QR"; // QR, USB, MANUAL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private User staff; // Lễ tân thực hiện quét thẻ

    @CreationTimestamp
    @Column(name = "checked_in_at", nullable = false, updatable = false)
    private LocalDateTime checkedInAt;
}