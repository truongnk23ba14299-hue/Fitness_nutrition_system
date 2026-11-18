package com.fitness.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "diary_entries", indexes = {
        @Index(name = "idx_diary_user_date", columnList = "user_id, entry_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "entry_date", nullable = false)
    @Builder.Default
    private LocalDate entryDate = LocalDate.now();

    @Column(name = "entry_type", nullable = false, length = 10)
    private String entryType; // MEAL hoặc WORKOUT

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    @Builder.Default
    private Integer calories = 0;

    @Column(name = "logged_time", length = 10)
    private String loggedTime; // VD: 08:30

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}