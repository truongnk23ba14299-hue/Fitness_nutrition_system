package com.fitness.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trainer_blocked_slots", uniqueConstraints = {
        @UniqueConstraint(name = "uk_trainer_slot", columnNames = {"trainer_id", "slot_start"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainerBlockedSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Column(name = "slot_start", nullable = false)
    private LocalDateTime slotStart;

    @Column(length = 200)
    private String reason; // Nghỉ phép, bận việc cá nhân...
}