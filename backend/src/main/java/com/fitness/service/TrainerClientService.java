package com.fitness.service;

import com.fitness.dto.BodyRecordResponse;
import com.fitness.dto.TrainerClientDetailResponse;
import com.fitness.dto.TrainerClientResponse;
import com.fitness.model.BodyRecord;
import com.fitness.model.EnrollmentStatus;
import com.fitness.model.PackageEnrollment;
import com.fitness.model.Trainer;
import com.fitness.model.User;
import com.fitness.repository.BodyRecordRepository;
import com.fitness.repository.TrainerClientRepository;
import com.fitness.repository.TrainerRepository;
import com.fitness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TrainerClientService {

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final TrainerClientRepository trainerClientRepository;
    private final BodyRecordRepository bodyRecordRepository;

    // 1. GET /api/trainer/clients: Danh sách học viên đang theo học gói PT của HLV hiện tại
    @Transactional(readOnly = true)
    public List<TrainerClientResponse> getMyClients(String email) {
        Trainer trainer = currentTrainer(email);

        List<PackageEnrollment> enrollments =
                trainerClientRepository.findByTrainer_IdOrderByCreatedAtDesc(trainer.getId());

        // Mỗi học viên chỉ hiện 1 dòng: lấy đăng ký ACTIVE mới nhất
        Map<Long, PackageEnrollment> latestActiveByUser = new LinkedHashMap<>();
        for (PackageEnrollment enrollment : enrollments) {
            if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
                continue;
            }
            latestActiveByUser.putIfAbsent(enrollment.getUser().getId(), enrollment);
        }

        if (latestActiveByUser.isEmpty()) {
            return List.of();
        }

        // Lấy bản ghi InBody mới nhất của tất cả học viên trong 1 truy vấn
        Map<Long, BodyRecord> latestRecordByUser = new LinkedHashMap<>();
        bodyRecordRepository
                .findByUser_IdInOrderByRecordDateDescIdDesc(latestActiveByUser.keySet())
                .forEach(record -> latestRecordByUser.putIfAbsent(record.getUser().getId(), record));

        return latestActiveByUser.values().stream()
                .map(enrollment -> toSummary(enrollment, latestRecordByUser.get(enrollment.getUser().getId())))
                .toList();
    }

    // 2. GET /api/trainer/clients/{userId}: Chi tiết thể trạng & bệnh sử của học viên kèm riêng
    @Transactional(readOnly = true)
    public TrainerClientDetailResponse getClientDetail(Long userId, String email) {
        Trainer trainer = currentTrainer(email);

        // Chặn tuyệt đối: HLV chỉ được xem hồ sơ học viên do chính mình phụ trách
        if (!trainerClientRepository.existsByTrainer_IdAndUser_Id(trainer.getId(), userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Học viên này không thuộc quyền quản lý của bạn!");
        }

        User client = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy học viên có ID: " + userId));

        List<PackageEnrollment> enrollments = trainerClientRepository
                .findByTrainer_IdAndUser_IdOrderByCreatedAtDesc(trainer.getId(), userId);

        List<BodyRecordResponse> bodyRecords = bodyRecordRepository
                .findByUser_IdOrderByRecordDateDescIdDesc(userId)
                .stream()
                .map(BodyRecordResponse::fromEntity)
                .toList();

        return new TrainerClientDetailResponse(
                client.getId(),
                client.getFullName(),
                client.getEmail(),
                client.getPhone(),
                client.getAvatarUrl(),
                client.getGender(),
                client.getDateOfBirth(),
                client.getHeightCm(),
                client.getWeightKg(),
                client.getMedicalNote(),
                client.getStatus(),
                client.getCreatedAt(),
                enrollments.stream().map(this::toEnrollmentInfo).toList(),
                bodyRecords,
                bodyRecords.isEmpty() ? null : bodyRecords.get(0)
        );
    }

    // Helper: Chuyển 1 đăng ký ACTIVE thành dòng danh sách học viên
    private TrainerClientResponse toSummary(PackageEnrollment enrollment, BodyRecord latestRecord) {
        User client = enrollment.getUser();
        return new TrainerClientResponse(
                client.getId(),
                client.getFullName(),
                client.getEmail(),
                client.getPhone(),
                client.getAvatarUrl(),
                client.getGender(),
                enrollment.getId(),
                enrollment.getWorkoutPackage().getId(),
                enrollment.getWorkoutPackage().getTitle(),
                enrollment.getStartDate(),
                enrollment.getEndDate(),
                enrollment.getStatus() != null ? enrollment.getStatus().name() : null,
                enrollment.getPtSessionsTotal(),
                enrollment.getPtSessionsRemaining(),
                latestRecord != null ? latestRecord.getRecordDate() : null,
                latestRecord != null ? latestRecord.getWeightKg() : null,
                latestRecord != null ? latestRecord.getBodyFatPct() : null
        );
    }

    // Helper: Chuyển 1 đăng ký gói tập thành DTO con
    private TrainerClientDetailResponse.EnrollmentInfo toEnrollmentInfo(PackageEnrollment enrollment) {
        return new TrainerClientDetailResponse.EnrollmentInfo(
                enrollment.getId(),
                enrollment.getWorkoutPackage().getId(),
                enrollment.getWorkoutPackage().getTitle(),
                enrollment.getStartDate(),
                enrollment.getEndDate(),
                enrollment.getStatus() != null ? enrollment.getStatus().name() : null,
                enrollment.getPtSessionsTotal(),
                enrollment.getPtSessionsRemaining(),
                enrollment.getCreatedAt()
        );
    }

    // Helper: Xác định hồ sơ HLV gắn với tài khoản đang đăng nhập
    private Trainer currentTrainer(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bạn chưa đăng nhập");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản người dùng!"));
        return trainerRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Tài khoản chưa được liên kết với hồ sơ huấn luyện viên!"));
    }
}
