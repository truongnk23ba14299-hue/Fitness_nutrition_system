package com.fitness.service;

import com.fitness.dto.MemberLookupResponse;
import com.fitness.model.CheckIn;
import com.fitness.model.EnrollmentStatus;
import com.fitness.model.PackageEnrollment;
import com.fitness.model.User;
import com.fitness.repository.CheckInRepository;
import com.fitness.repository.PackageEnrollmentRepository;
import com.fitness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffMemberService {

    private final UserRepository userRepository;
    private final PackageEnrollmentRepository packageEnrollmentRepository;
    private final CheckInRepository checkInRepository;

    /**
     * GET /api/staff/members?keyword=
     * Tra cứu danh sách hội viên theo Tên, SĐT, Email hoặc Mã thẻ
     */
    @Transactional(readOnly = true)
    public List<MemberLookupResponse> searchMembers(String keyword) {
        String cleanKeyword = (keyword != null) ? keyword.trim() : "";

        // Tự động nhận diện nếu nhân viên nhập mã dạng "FL-00007", "FL7", "#7" hoặc "7"
        Long searchId = null;
        if (cleanKeyword.matches("^(?i)(FL[-_#]?)?\\d+$") || cleanKeyword.matches("^#\\d+$")) {
            try {
                searchId = Long.parseLong(cleanKeyword.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException ignored) {}
        }

        List<User> users = userRepository.searchMembers(cleanKeyword, searchId);
        LocalDate today = LocalDate.now();

        return users.stream()
                .map(user -> buildMemberLookupResponse(user, today))
                .toList();
    }

    /**
     * GET /api/staff/members/{id}/membership
     * Kiểm tra chi tiết thẻ hội viên của 1 học viên cụ thể theo ID
     */
    @Transactional(readOnly = true)
    public MemberLookupResponse getMemberMembership(Long memberId) {
        User user = userRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy học viên với ID: " + memberId
                ));

        return buildMemberLookupResponse(user, LocalDate.now());
    }

    /**
     * Bóc tách gói tập, tính số ngày còn lại và trạng thái thẻ
     */
    private MemberLookupResponse buildMemberLookupResponse(User user, LocalDate today) {
        List<PackageEnrollment> enrollments = packageEnrollmentRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        // 1. Tìm gói tập ACTIVE còn hạn sử dụng (ưu tiên gói có endDate xa nhất)
        PackageEnrollment currentEnrollment = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .filter(e -> e.getEndDate() != null && !e.getEndDate().isBefore(today))
                .max((e1, e2) -> e1.getEndDate().compareTo(e2.getEndDate()))
                .orElse(null);

        // 2. Nếu không có gói còn hạn, lấy gói gần nhất đã từng mua để hiện thông tin lịch sử
        if (currentEnrollment == null && !enrollments.isEmpty()) {
            currentEnrollment = enrollments.get(0);
        }

        // 3. Phân định trạng thái thẻ & điều kiện check-in vào phòng tập
        boolean isUserLocked = "LOCKED".equalsIgnoreCase(user.getStatus());
        boolean isPackageValid = currentEnrollment != null &&
                currentEnrollment.getStatus() == EnrollmentStatus.ACTIVE &&
                currentEnrollment.getEndDate() != null &&
                !currentEnrollment.getEndDate().isBefore(today);

        boolean canCheckIn = !isUserLocked && isPackageValid;

        String status;
        Integer remainingDays = null;
        Long enrollmentId = null;
        Long packageId = null;
        String packageTitle = "Chưa đăng ký gói tập";
        Integer durationMonths = null;
        BigDecimal packagePrice = null;
        LocalDate startDate = null;
        LocalDate endDate = null;
        Long trainerId = null;
        String trainerName = null;
        String benefits = null;
        String scheduleDetails = null;

        if (isUserLocked) {
            status = "LOCKED";
        } else if (currentEnrollment == null) {
            status = "NO_PACKAGE";
        } else {
            enrollmentId = currentEnrollment.getId();
            packagePrice = currentEnrollment.getTotalPrice();
            startDate = currentEnrollment.getStartDate();
            endDate = currentEnrollment.getEndDate();

            if (currentEnrollment.getWorkoutPackage() != null) {
                var wp = currentEnrollment.getWorkoutPackage();
                packageId = wp.getId();
                packageTitle = wp.getTitle();
                durationMonths = wp.getDurationMonths();
                benefits = wp.getBenefits();
                scheduleDetails = wp.getScheduleDetails();
            }

            if (currentEnrollment.getTrainer() != null) {
                trainerId = currentEnrollment.getTrainer().getId();
                trainerName = currentEnrollment.getTrainer().getFullName();
            }

            if (endDate != null) {
                remainingDays = (int) ChronoUnit.DAYS.between(today, endDate);
                if (isPackageValid) {
                    status = "ACTIVE";
                } else {
                    status = "EXPIRED";
                }
            } else {
                status = (currentEnrollment.getStatus() != null) ? currentEnrollment.getStatus().name() : "ACTIVE";
            }
        }

        // 4. Lấy thời gian điểm danh gần nhất
        Optional<CheckIn> lastCheckIn = checkInRepository.findTopByUserIdOrderByCheckedInAtDesc(user.getId());
        LocalDateTime lastCheckInAt = lastCheckIn.map(CheckIn::getCheckedInAt).orElse(null);
        String lastCheckInMethod = lastCheckIn.map(CheckIn::getMethod).orElse(null);

        String memberCode = String.format("FL-%05d", user.getId());

        return MemberLookupResponse.builder()
                .id(user.getId())
                .memberCode(memberCode)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .avatarUrl(user.getAvatarUrl())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .userStatus(user.getStatus() != null ? user.getStatus() : "ACTIVE")
                .enrollmentId(enrollmentId)
                .packageId(packageId)
                .packageTitle(packageTitle)
                .durationMonths(durationMonths)
                .packagePrice(packagePrice)
                .startDate(startDate)
                .endDate(endDate)
                .remainingDays(remainingDays)
                .trainerId(trainerId)
                .trainerName(trainerName)
                .benefits(benefits)
                .scheduleDetails(scheduleDetails)
                .canCheckIn(canCheckIn)
                .status(status)
                .lastCheckInAt(lastCheckInAt)
                .lastCheckInMethod(lastCheckInMethod)
                .build();
    }
}