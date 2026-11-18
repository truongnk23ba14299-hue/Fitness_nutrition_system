package com.fitness.service;

import com.fitness.dto.BookingResponse;
import com.fitness.dto.TrainerRequest;
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

    // =========================================================================
    // CÁC NGHIỆP VỤ QUẢN TRỊ DÀNH CHO ADMIN 
    // =========================================================================

    // 1. Admin: Xem danh sách HLV có tìm kiếm theo tên/chuyên môn và lọc isActive
    @Transactional(readOnly = true)
    public List<TrainerResponse> getAllTrainersForAdmin(String keyword, Boolean isActive) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return trainerRepository.searchTrainersForAdmin(kw, isActive)
                .stream()
                .map(TrainerResponse::fromEntity)
                .toList();
    }

    // 2. Admin: Thêm mới HLV
    @Transactional
    public TrainerResponse createTrainer(TrainerRequest request) {
        Trainer trainer = new Trainer();
        trainer.setFullName(request.fullName().trim());
        trainer.setSpecialty(request.specialty().trim());
        trainer.setExperienceYears(request.experienceYears());
        trainer.setBio(request.bio());
        trainer.setAvatarUrl(request.avatarUrl());
        trainer.setRating(request.rating() != null ? request.rating() : 5.0);
        trainer.setIsActive(request.isActive() != null ? request.isActive() : true);

        Trainer saved = trainerRepository.save(trainer);
        return TrainerResponse.fromEntity(saved);
    }

    // 3. Admin: Cập nhật thông tin chuyên môn, số năm kinh nghiệm, bio
    @Transactional
    public TrainerResponse updateTrainer(Long id, TrainerRequest request) {
        Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "Không tìm thấy Huấn luyện viên có ID: " + id));

        trainer.setFullName(request.fullName().trim());
        trainer.setSpecialty(request.specialty().trim());
        trainer.setExperienceYears(request.experienceYears());
        trainer.setBio(request.bio());
        trainer.setAvatarUrl(request.avatarUrl());
        if (request.rating() != null) {
            trainer.setRating(request.rating());
        }
        if (request.isActive() != null) {
            trainer.setIsActive(request.isActive());
        }

        Trainer updated = trainerRepository.save(trainer);
        return TrainerResponse.fromEntity(updated);
    }

    // 4. Admin: Kích hoạt hoặc Vô hiệu hóa PT (Bật / Tắt hoạt động)
    @Transactional
    public TrainerResponse updateTrainerStatus(Long id, Boolean isActive) {
        Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "Không tìm thấy Huấn luyện viên có ID: " + id));

        trainer.setIsActive(isActive);
        Trainer updated = trainerRepository.save(trainer);
        return TrainerResponse.fromEntity(updated);
    }
}
