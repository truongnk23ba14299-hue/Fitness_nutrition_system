package com.fitness.repository;

import com.fitness.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByTrainer_IdAndBookingDateTimeAndStatusIn(
            Long trainerId,
            LocalDateTime bookingDateTime,
            Collection<String> statuses
    );
}
