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
import com.fitness.dto.AdminEnrollmentReportResponse;
import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PackageEnrollmentService {

    private final PackageEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final PackageRepository packageRepository;
    private final TrainerRepository trainerRepository;
    
    // 1. Đăng ký mua gói tập 
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

    // 2. Lấy danh sách gói tập mà người dùng đã đăng ký
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản người dùng"
                ));

        return enrollmentRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(enrollment -> new EnrollmentResponse(
                        enrollment.getId(),
                        enrollment.getWorkoutPackage().getId(),
                        enrollment.getWorkoutPackage().getTitle(),
                        user.getFullName(),
                        user.getEmail(),
                        enrollment.getTotalPrice(),
                        enrollment.getStartDate(),
                        enrollment.getEndDate(),
                        enrollment.getStatus(),
                        enrollment.getNotes(),
                        enrollment.getCreatedAt()
                ))
                .toList();
    }

    // 3. Admin: Báo cáo doanh thu & tìm kiếm lượt mua theo tên/email học viên
    @Transactional(readOnly = true)
    public AdminEnrollmentReportResponse getEnrollmentReport(String keyword) {
        List<PackageEnrollment> list = enrollmentRepository.searchEnrollments(keyword);
        // Tính tổng doanh thu của các gói không bị HỦY (CANCELLED)
        BigDecimal totalRevenue = list.stream()
                .filter(e -> e.getStatus() != EnrollmentStatus.CANCELLED)
                .map(PackageEnrollment::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long activeCount = list.stream().filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE).count();
        long expiredCount = list.stream().filter(e -> e.getStatus() == EnrollmentStatus.EXPIRED).count();
        long cancelledCount = list.stream().filter(e -> e.getStatus() == EnrollmentStatus.CANCELLED).count();
        List<EnrollmentResponse> dtoList = list.stream()
                .map(e -> new EnrollmentResponse(
                        e.getId(),
                        e.getWorkoutPackage().getId(),
                        e.getWorkoutPackage().getTitle(),
                        e.getUser().getFullName(),
                        e.getUser().getEmail(),
                        e.getTotalPrice(),
                        e.getStartDate(),
                        e.getEndDate(),
                        e.getStatus(),
                        e.getNotes(),
                        e.getCreatedAt()
                ))
                .toList();
        return new AdminEnrollmentReportResponse(
                totalRevenue,
                list.size(),
                activeCount,
                expiredCount,
                cancelledCount,
                dtoList
        );
    }
    // 4. Admin: Cập nhật trạng thái gói tập (ACTIVE, EXPIRED, CANCELLED)
    @Transactional
    public EnrollmentResponse updateStatus(Long id, EnrollmentStatus newStatus) {
        PackageEnrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy lượt đăng ký gói tập với ID: " + id
                ));
        enrollment.setStatus(newStatus);
        PackageEnrollment saved = enrollmentRepository.save(enrollment);
        return new EnrollmentResponse(
                saved.getId(),
                saved.getWorkoutPackage().getId(),
                saved.getWorkoutPackage().getTitle(),
                saved.getUser().getFullName(),
                saved.getUser().getEmail(),
                saved.getTotalPrice(),
                saved.getStartDate(),
                saved.getEndDate(),
                saved.getStatus(),
                saved.getNotes(),
                saved.getCreatedAt()
        );
    }
}