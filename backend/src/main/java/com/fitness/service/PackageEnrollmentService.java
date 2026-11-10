package com.fitness.service;

import com.fitness.dto.EnrollmentRequest;
import com.fitness.dto.EnrollmentResponse;
import com.fitness.model.EnrollmentStatus;
import com.fitness.model.PackageEnrollment;
import com.fitness.model.Trainer;
import com.fitness.model.User;
import com.fitness.model.WorkoutPackage;
import com.fitness.repository.PackageEnrollmentRepository;
import com.fitness.repository.PackageRepository;
import com.fitness.repository.TrainerRepository;
import com.fitness.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PackageEnrollmentService {

    private final PackageEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final PackageRepository packageRepository;
    private final TrainerRepository trainerRepository;

    @Transactional
    public EnrollmentResponse enrollPackage(
            String userEmail,
            EnrollmentRequest request
    ) {

        // 1. Tìm người dùng đăng ký thông qua email lấy từ JWT
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản người dùng"
                ));
        if (enrollmentRepository.existsByUserIdAndStatus(user.getId(), EnrollmentStatus.ACTIVE)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, 
                        "Bạn hiện đang có một gói tập còn hiệu lực!");
        }

        // 2. Tìm gói tập theo ID
        WorkoutPackage pkg = packageRepository.findById(request.packageId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy gói tập với ID: " + request.packageId()
                ));

        // 3. Chỉ cho phép đăng ký gói tập đang hoạt động
        if (!Boolean.TRUE.equals(pkg.getIsActive())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Gói tập này hiện đang tạm ngưng mở bán"
            );
        }

        // 4. Kiểm tra thời hạn của gói tập
        if (pkg.getDurationMonths() == null
                || pkg.getDurationMonths() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Thời hạn gói tập không hợp lệ"
            );
        }

        // 5. Tìm huấn luyện viên nếu người dùng có lựa chọn
        Trainer trainer = null;

        if (request.trainerId() != null) {
            trainer = trainerRepository.findById(request.trainerId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Không tìm thấy huấn luyện viên đã chọn"
                    ));
        }

        // 6. Xác định ngày bắt đầu sử dụng gói tập
        LocalDate start = request.startDate() != null
                ? request.startDate()
                : LocalDate.now();

        // 7. Tính ngày kết thúc dựa trên thời hạn gói tập
        LocalDate end = start.plusMonths(pkg.getDurationMonths());

        // 8. Tạo đối tượng đăng ký gói tập
        PackageEnrollment enrollment = PackageEnrollment.builder()
                .user(user)
                .workoutPackage(pkg)
                .trainer(trainer)
                .totalPrice(pkg.getPrice())
                .startDate(start)
                .endDate(end)
                .status(EnrollmentStatus.ACTIVE)
                .notes(request.notes())
                .build();

        // 9. Lưu lượt đăng ký vào database
        PackageEnrollment saved = enrollmentRepository.save(enrollment);

        // 10. Chuyển dữ liệu thành DTO để trả về frontend
        return new EnrollmentResponse(
                saved.getId(),
                pkg.getId(),
                pkg.getTitle(),
                user.getFullName(),
                user.getEmail(),
                saved.getTotalPrice(),
                saved.getStartDate(),
                saved.getEndDate(),
                saved.getStatus(),
                saved.getNotes(),
                saved.getCreatedAt()
        );
    }
}
