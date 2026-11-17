package com.fitness.service;

import com.fitness.dto.BookingResponse;
import com.fitness.dto.TrainerResponse;
import com.fitness.dto.TrainerScheduleResponse;
import com.fitness.model.Booking;
import com.fitness.model.Trainer;
import com.fitness.repository.BookingRepository;
import com.fitness.repository.TrainerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final BookingRepository bookingRepository;

    public TrainerService(TrainerRepository trainerRepository, BookingRepository bookingRepository) {
        this.trainerRepository = trainerRepository;
        this.bookingRepository = bookingRepository;
    }

    // Lấy danh sách PT, chỉ lấy PT đang hoạt động
    @Transactional(readOnly = true)
    public List<TrainerResponse> getAllTrainers(String specialty) {
        List<Trainer> trainers;

        if (specialty != null && !specialty.trim().isEmpty()) {
            trainers = trainerRepository
                    .findBySpecialtyContainingIgnoreCaseAndIsActiveTrue(
                            specialty.trim()
                    );
        } else {
            trainers = trainerRepository.findByIsActiveTrue();
        }

        return trainers.stream()
                .map(TrainerResponse::fromEntity)
                .toList();
    }

    // Lấy toàn bộ PT đang hoạt động
    @Transactional(readOnly = true)
    public List<TrainerResponse> getAllTrainers() {
        return getAllTrainers(null);
    }

    // Lấy chi tiết PT theo ID
    @Transactional(readOnly = true)
    public TrainerResponse getTrainerById(Long id) {
        return trainerRepository.findById(id)
                .map(TrainerResponse::fromEntity)
                .orElse(null);
    }

    // Lấy lịch bận / trống của PT theo ngày
    @Transactional(readOnly = true)
    public TrainerScheduleResponse getTrainerSchedule(Long trainerId, LocalDate date) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy huấn luyện viên có ID: " + trainerId));

        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        List<Booking> bookings = bookingRepository.findTrainerScheduleBookings(
                trainerId, startOfDay, endOfDay, List.of("PENDING", "CONFIRMED")
        );

        List<LocalTime> standardStartTimes = List.of(
                LocalTime.of(7, 0), LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(10, 0),
                LocalTime.of(11, 0), LocalTime.of(12, 0), LocalTime.of(13, 0), LocalTime.of(14, 0),
                LocalTime.of(15, 0), LocalTime.of(16, 0), LocalTime.of(17, 0), LocalTime.of(18, 0),
                LocalTime.of(19, 0), LocalTime.of(20, 0)
        );

        Set<LocalTime> bookedHours = bookings.stream()
                .map(b -> b.getBookingDateTime().toLocalTime().withMinute(0).withSecond(0).withNano(0))
                .collect(Collectors.toSet());

        List<TrainerScheduleResponse.ScheduleSlot> slots = standardStartTimes.stream()
                .map(time -> {
                    boolean isBooked = bookedHours.contains(time);
                    return new TrainerScheduleResponse.ScheduleSlot(
                            time,
                            time.plusHours(1),
                            !isBooked,
                            isBooked ? "BOOKED" : "AVAILABLE"
                    );
                })
                .toList();

        List<BookingResponse> busyBookings = bookings.stream()
                .map(BookingResponse::fromEntity)
                .toList();

        return new TrainerScheduleResponse(
                trainer.getId(),
                trainer.getFullName(),
                targetDate,
                slots,
                busyBookings
        );
    }
}
