
package com.fitness.repository;

import com.fitness.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Kiểm tra PT đã có lịch vào thời gian này chưa
    boolean existsByTrainer_IdAndBookingDateTimeAndStatusIn(
            Long trainerId,
            LocalDateTime bookingDateTime,
            Collection<String> statuses
    );

    // Lấy lịch hẹn của một học viên
    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.trainer " +
           "WHERE b.user.id = :userId " +
           "ORDER BY b.bookingDateTime DESC")
    List<Booking> findByUserIdWithDetails(
            @Param("userId") Long userId
    );

    // Lấy chi tiết một booking
    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.trainer " +
           "WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(
            @Param("id") Long id
    );

    // Lấy lịch đặt của PT trong một khoảng thời gian
    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.trainer " +
           "WHERE b.trainer.id = :trainerId " +
           "AND b.bookingDateTime >= :start " +
           "AND b.bookingDateTime <= :end " +
           "AND b.status IN :statuses " +
           "ORDER BY b.bookingDateTime ASC")
    List<Booking> findTrainerScheduleBookings(
            @Param("trainerId") Long trainerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("statuses") Collection<String> statuses
    );

    // Admin: lấy toàn bộ booking cùng thông tin User và Trainer
    // Không truyền tham số null vào SQL
    @EntityGraph(attributePaths = {"user", "trainer"})
    List<Booking> findAllByOrderByBookingDateTimeDesc();

}
