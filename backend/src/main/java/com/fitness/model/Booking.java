package com.fitness.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_user_time", columnList = "user_id, booking_datetime")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Column(name = "booking_datetime", nullable = false)
    private LocalDateTime bookingDateTime;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING, CONFIRMED, COMPLETED, CANCELLED, REJECTED, NO_SHOW

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "trainer_note", columnDefinition = "TEXT")
    private String trainerNote;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method tương thích ngược với BookPtResponse
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
}