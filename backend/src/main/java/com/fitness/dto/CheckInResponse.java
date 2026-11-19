package com.fitness.dto;

import com.fitness.model.CheckIn;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CheckInResponse(
        Long id,
        Long userId,
        String fullName,
        String email,
        String phone,
        String avatarUrl,
        String packageTitle,
        Long enrollmentId,
        LocalDate endDate,
        String method,
        String status, // "VALID"
        String staffName,
        LocalDateTime checkedInAt
) {
    public static CheckInResponse fromEntity(CheckIn c) {
        String pkgTitle = (c.getEnrollment() != null && c.getEnrollment().getWorkoutPackage() != null)
                ? c.getEnrollment().getWorkoutPackage().getTitle()
                : "Vé tập ngày / Không gói";

        Long enrollId = (c.getEnrollment() != null) ? c.getEnrollment().getId() : null;
        LocalDate end = (c.getEnrollment() != null) ? c.getEnrollment().getEndDate() : null;
        String staff = (c.getStaff() != null) ? c.getStaff().getFullName() : "Lễ tân hệ thống";

        return new CheckInResponse(
                c.getId(),
                c.getUser() != null ? c.getUser().getId() : null,
                c.getUser() != null ? c.getUser().getFullName() : "Hội viên",
                c.getUser() != null ? c.getUser().getEmail() : null,
                c.getUser() != null ? c.getUser().getPhone() : null,
                c.getUser() != null ? c.getUser().getAvatarUrl() : null,
                pkgTitle,
                enrollId,
                end,
                c.getMethod() != null ? c.getMethod() : "QR",
                "VALID",
                staff,
                c.getCheckedInAt()
        );
    }
}