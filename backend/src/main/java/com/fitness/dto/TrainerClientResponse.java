package com.fitness.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dòng trong danh sách học viên đang theo học gói PT của HLV.
 */
public record TrainerClientResponse(
        Long userId,
        String fullName,
        String email,
        String phone,
        String avatarUrl,
        String gender,
        Long enrollmentId,
        Long packageId,
        String packageName,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        Integer ptSessionsTotal,
        Integer ptSessionsRemaining,
        LocalDate lastRecordDate,
        BigDecimal latestWeightKg,
        BigDecimal latestBodyFatPct
) {}
