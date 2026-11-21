package com.fitness.dto;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record AdminDashboardSummaryResponse(
        long totalMembers,          // Tổng số học viên (ROLE_CLIENT)
        long activeMembers,         // Số học viên có thẻ tập ACTIVE còn hạn
        long totalTrainers,         // Tổng số Huấn luyện viên
        long totalPackages,         // Tổng số gói tập mở bán
        long totalProducts,         // Tổng số sản phẩm shop
        long totalOrders,           // Tổng số đơn hàng
        long totalBookings,         // Tổng số lịch hẹn PT
        long todayCheckIns,         // Số lượt check-in hôm nay
        BigDecimal totalRevenue,    // Tổng doanh thu tích lũy
        BigDecimal todayRevenue     // Doanh thu phát sinh hôm nay
) {}