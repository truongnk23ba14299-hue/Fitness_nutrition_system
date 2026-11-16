package com.fitness.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminEnrollmentReportResponse(
        BigDecimal totalRevenue,          // Tổng doanh thu (không tính các gói bị HỦY)
        long totalEnrollments,            // Tổng số lượt mua gói
        long activeCount,                 // Số gói đang ACTIVE
        long expiredCount,                // Số gói đã EXPIRED
        long cancelledCount,              // Số gói đã CANCELLED
        List<EnrollmentResponse> enrollments // Danh sách chi tiết các lượt đăng ký
) {}