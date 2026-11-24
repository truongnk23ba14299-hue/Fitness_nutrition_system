package com.fitness.service;

import com.fitness.dto.BodyRecordRequest;
import com.fitness.dto.BodyRecordResponse;
import com.fitness.model.BodyRecord;
import com.fitness.model.User;
import com.fitness.repository.BodyRecordRepository;
import com.fitness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final BodyRecordRepository bodyRecordRepository;
    private final UserRepository userRepository;

    // 1. GET /api/progress/my-records: Lấy lịch sử chỉ số InBody của học viên hiện tại
    @Transactional(readOnly = true)
    public List<BodyRecordResponse> getMyRecords(String email) {
        User user = getUserByEmail(email);
        return bodyRecordRepository.findByUser_IdOrderByRecordDateDescIdDesc(user.getId())
                .stream()
                .map(BodyRecordResponse::fromEntity)
                .toList();
    }

    // 2. POST /api/progress/my-records: Học viên ghi nhận chỉ số cơ thể mới
    @Transactional
    public BodyRecordResponse createMyRecord(String email, BodyRecordRequest request) {
        User user = getUserByEmail(email);

        // Ưu tiên chiều cao nhập trong lần đo, nếu không có thì lấy từ hồ sơ cá nhân
        BigDecimal heightCm = request.heightCm() != null
                ? request.heightCm()
                : (user.getHeightCm() != null ? BigDecimal.valueOf(user.getHeightCm()) : null);

        BodyRecord record = BodyRecord.builder()
                .user(user)
                .recordDate(request.recordDate())
                .weightKg(request.weightKg())
                .heightCm(heightCm)
                .bodyFatPct(request.bodyFatPct())
                .muscleKg(request.muscleKg())
                .bmi(calculateBmi(request.weightKg(), heightCm))
                .note(request.note() != null && !request.note().isBlank() ? request.note().trim() : null)
                .build();

        BodyRecord saved = bodyRecordRepository.save(record);
        return BodyRecordResponse.fromEntity(saved);
    }

    // 3. DELETE /api/progress/my-records/{id}: Xóa 1 bản ghi InBody bị nhập nhầm
    @Transactional
    public void deleteMyRecord(Long id, String email) {
        User user = getUserByEmail(email);
        BodyRecord record = bodyRecordRepository.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy bản ghi chỉ số InBody hoặc bạn không có quyền xóa!"));

        bodyRecordRepository.delete(record);
    }

    // Helper: Tính BMI từ cân nặng (kg) và chiều cao (cm), làm tròn 1 chữ số thập phân
    private BigDecimal calculateBmi(BigDecimal weightKg, BigDecimal heightCm) {
        if (heightCm == null || heightCm.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        BigDecimal heightM = heightCm.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return weightKg.divide(heightM.multiply(heightM), 1, RoundingMode.HALF_UP);
    }

    // Helper: Tìm User theo Email từ JWT
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản người dùng!"));
    }
}
