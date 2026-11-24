package com.fitness.dto;

import com.fitness.model.BodyRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BodyRecordResponse(
        Long id,
        LocalDate recordDate,
        BigDecimal weightKg,
        BigDecimal heightCm,
        BigDecimal bodyFatPct,
        BigDecimal muscleKg,
        BigDecimal bmi,
        String note,
        LocalDateTime createdAt
) {
    public static BodyRecordResponse fromEntity(BodyRecord record) {
        if (record == null) {
            return null;
        }
        return new BodyRecordResponse(
                record.getId(),
                record.getRecordDate(),
                record.getWeightKg(),
                record.getHeightCm(),
                record.getBodyFatPct(),
                record.getMuscleKg(),
                record.getBmi(),
                record.getNote(),
                record.getCreatedAt()
        );
    }
}
