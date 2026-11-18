package com.fitness.dto;

import jakarta.validation.constraints.*;

public record TrainerRequest(
        @NotBlank(message = "Họ và tên HLV không được để trống")
        @Size(max = 100, message = "Họ và tên tối đa 100 ký tự")
        String fullName,

        @NotBlank(message = "Chuyên môn không được để trống (VD: Gym, Yoga, Bodybuilding)")
        @Size(max = 100, message = "Chuyên môn tối đa 100 ký tự")
        String specialty,

        @NotNull(message = "Số năm kinh nghiệm không được để trống")
        @Min(value = 0, message = "Số năm kinh nghiệm phải từ 0 trở lên")
        @Max(value = 50, message = "Số năm kinh nghiệm không hợp lệ")
        Integer experienceYears,

        @Size(max = 2000, message = "Tiểu sử giới thiệu tối đa 2000 ký tự")
        String bio,

        @Size(max = 255, message = "Đường dẫn ảnh đại diện tối đa 255 ký tự")
        String avatarUrl,

        @DecimalMin(value = "0.0", message = "Đánh giá tối thiểu là 0.0")
        @DecimalMax(value = "5.0", message = "Đánh giá tối đa là 5.0")
        Double rating,

        Boolean isActive
) {}