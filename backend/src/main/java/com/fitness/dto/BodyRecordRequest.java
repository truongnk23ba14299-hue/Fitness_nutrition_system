package com.fitness.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dữ liệu học viên ghi nhận chỉ số cơ thể mới (cân, mỡ, cơ...).
 */
public record BodyRecordRequest(
        @NotNull(message = "Ngày đo không được để trống")
        LocalDate recordDate,

        @NotNull(message = "Cân nặng không được để trống")
        @DecimalMin(value = "1.0", message = "Cân nặng phải lớn hơn 1 kg")
        @DecimalMax(value = "500.0", message = "Cân nặng không hợp lệ")
        BigDecimal weightKg,

        @DecimalMin(value = "50.0", message = "Chiều cao phải lớn hơn 50 cm")
        @DecimalMax(value = "300.0", message = "Chiều cao không hợp lệ")
        BigDecimal heightCm,

        @DecimalMin(value = "0.0", message = "Tỷ lệ mỡ không được âm")
        @DecimalMax(value = "100.0", message = "Tỷ lệ mỡ không hợp lệ")
        BigDecimal bodyFatPct,

        @DecimalMin(value = "0.0", message = "Khối lượng cơ không được âm")
        @DecimalMax(value = "300.0", message = "Khối lượng cơ không hợp lệ")
        BigDecimal muscleKg,

        @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
        String note
) {}
