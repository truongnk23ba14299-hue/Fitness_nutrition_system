package com.fitness.repository;

import com.fitness.model.TrainerBlockedSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TrainerBlockedSlotRepository extends JpaRepository<TrainerBlockedSlot, Long> {
    List<TrainerBlockedSlot> findByTrainer_IdOrderBySlotStartAsc(Long trainerId);
    Optional<TrainerBlockedSlot> findByTrainer_IdAndSlotStart(Long trainerId, java.time.LocalDateTime slotStart);
}
