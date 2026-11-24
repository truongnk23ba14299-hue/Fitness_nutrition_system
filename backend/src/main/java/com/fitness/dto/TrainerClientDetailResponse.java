package com.fitness.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Hồ sơ chi tiết (thể trạng + bệnh sử) của 1 học viên kèm riêng của HLV.
 */
public record TrainerClientDetailResponse(
        Long userId,
        String fullName,
        String email,
        String phone,
        String avatarUrl,
        String gender,
        LocalDate dateOfBirth,
        Double heightCm,
        Double weightKg,
        String medicalNote,
        String accountStatus,
        LocalDateTime memberSince,
        List<EnrollmentInfo> enrollments,
        List<BodyRecordResponse> bodyRecords,
        BodyRecordResponse latestBodyRecord
) {
    public record EnrollmentInfo(
            Long enrollmentId,
            Long packageId,
            String packageName,
            LocalDate startDate,
            LocalDate endDate,
            String status,
            Integer ptSessionsTotal,
            Integer ptSessionsRemaining,
            LocalDateTime enrolledAt
    ) {}
}
