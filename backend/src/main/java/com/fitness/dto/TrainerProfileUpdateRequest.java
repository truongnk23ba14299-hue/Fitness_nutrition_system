package com.fitness.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Dữ liệu HLV tự cập nhật: Bio cá nhân và số năm kinh nghiệm.
 * Trường nào không gửi (null) thì giữ nguyên giá trị cũ.
 */
public record TrainerProfileUpdateRequest(
        @Size(max = 2000, message = "Bio cá nhân tối đa 2000 ký tự")
        String bio,

        @Min(value = 0, message = "Kinh nghiệm không được âm")
        @Max(value = 60, message = "Kinh nghiệm tối đa 60 năm")
        Integer experienceYears
) {}
