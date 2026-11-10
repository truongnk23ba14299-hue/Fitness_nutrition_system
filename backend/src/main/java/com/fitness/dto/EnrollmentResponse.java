package com.fitness.dto;

import com.fitness.model.EnrollmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EnrollmentResponse(

        // ID của lượt đăng ký gói tập
        Long enrollmentId,

        // ID và tên gói tập đã đăng ký
        Long packageId,
        String packageTitle,

        // Thông tin người dùng đăng ký
        String userFullName,
        String userEmail,

        // Giá gói tập tại thời điểm đăng ký
        BigDecimal totalPrice,

        // Thời gian sử dụng gói tập
        LocalDate startDate,
        LocalDate endDate,

        // Trạng thái đăng ký gói tập
        EnrollmentStatus status,

        // Ghi chú bổ sung
        String notes,

        // Thời điểm tạo lượt đăng ký
        LocalDateTime createdAt

) {}