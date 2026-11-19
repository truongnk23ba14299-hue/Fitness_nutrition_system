package com.fitness.service;

import com.fitness.dto.MemberLookupResponse;
import com.fitness.model.EnrollmentStatus;
import com.fitness.model.PackageEnrollment;
import com.fitness.model.User;
import com.fitness.repository.PackageEnrollmentRepository;
import com.fitness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffMemberService {

    private final UserRepository userRepository;
    private final PackageEnrollmentRepository packageEnrollmentRepository;

    /**
     * GET /api/staff/members?keyword=
     * Phục vụ Lễ tân tra cứu hội viên tại quầy và giải quyết triệt để lỗi 403 Forbidden của ROLE_STAFF.
     * Gộp thông tin User và gói tập hiện tại thành 1 lần gọi duy nhất.
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
        List<MemberLookupResponse> results = new ArrayList<>();

        for (User user : users) {
            // Lấy toàn bộ lịch sử đăng ký gói tập của học viên
            List<PackageEnrollment> enrollments = packageEnrollmentRepository.findByUserId(user.getId());

            // 1. Tìm gói tập ACTIVE còn hạn sử dụng (endDate >= today), ưu tiên gói có hạn xa nhất
            PackageEnrollment currentEnrollment = enrollments.stream()
                    .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                    .filter(e -> e.getEndDate() != null && !e.getEndDate().isBefore(today))
                    .max((e1, e2) -> e1.getEndDate().compareTo(e2.getEndDate()))
                    .orElse(null);

            // 2. Nếu không có gói nào còn hạn, lấy gói gần nhất đã từng mua để hiện thông tin lịch sử
            if (currentEnrollment == null && !enrollments.isEmpty()) {
                currentEnrollment = enrollments.stream()
                        .filter(e -> e.getEndDate() != null)
                        .max((e1, e2) -> e1.getEndDate().compareTo(e2.getEndDate()))
                        .orElse(enrollments.get(0));
            }

            // 3. Phân định trạng thái thẻ cho Lễ tân
            String status;
            Integer remainingDays = null;
            Long enrollmentId = null;
            Long packageId = null;
            String packageTitle = "Chưa đăng ký gói tập";
            BigDecimal packagePrice = null;
            LocalDate startDate = null;
            LocalDate endDate = null;

            if ("LOCKED".equalsIgnoreCase(user.getStatus())) {
                status = "LOCKED";
            } else if (currentEnrollment == null) {
                status = "NO_PACKAGE";
            } else {
                enrollmentId = currentEnrollment.getId();
                if (currentEnrollment.getWorkoutPackage() != null) {
                    packageId = currentEnrollment.getWorkoutPackage().getId();
                    packageTitle = currentEnrollment.getWorkoutPackage().getTitle();
                }
                packagePrice = currentEnrollment.getTotalPrice();
                startDate = currentEnrollment.getStartDate();
                endDate = currentEnrollment.getEndDate();

                if (endDate != null) {
                    remainingDays = (int) ChronoUnit.DAYS.between(today, endDate);
                    if (currentEnrollment.getStatus() == EnrollmentStatus.ACTIVE && remainingDays >= 0) {
                        status = "ACTIVE";
                    } else {
                        status = "EXPIRED";
                    }
                } else {
                    status = (currentEnrollment.getStatus() != null) ? currentEnrollment.getStatus().name() : "ACTIVE";
                }
            }

            String memberCode = String.format("FL-%05d", user.getId());

            results.add(MemberLookupResponse.builder()
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
                    .packagePrice(packagePrice)
                    .startDate(startDate)
                    .endDate(endDate)
                    .remainingDays(remainingDays)
                    .status(status)
                    .build());
        }

        return results;
    }
}