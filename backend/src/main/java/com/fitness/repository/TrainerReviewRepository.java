package com.fitness.repository;

import com.fitness.model.TrainerReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TrainerReviewRepository extends JpaRepository<TrainerReview, Long> {
    List<TrainerReview> findByTrainer_IdOrderByCreatedAtDesc(Long trainerId);
    Optional<TrainerReview> findByBooking_Id(Long bookingId);
    boolean existsByBooking_Id(Long bookingId);
    long countByTrainer_Id(Long trainerId);
    List<TrainerReview> findByTrainer_Id(Long trainerId);
}
