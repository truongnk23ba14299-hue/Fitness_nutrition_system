package com.fitness.dto;

import jakarta.validation.constraints.NotNull;

public record CheckInRequest(
        @NotNull(message = "ID học viên (userId) không được để trống")
        Long userId,

        // Tùy chọn: nếu null, hệ thống tự động tìm gói tập ACTIVE còn hạn của học viên
        Long enrollmentId,

        // Phương thức quét: "QR", "SCANNER", hoặc "MANUAL" (mặc định "QR")
        String method
) {}