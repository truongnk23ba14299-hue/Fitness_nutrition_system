package com.fitness.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.List;

@Builder
public record RevenueReportResponse(
        BigDecimal totalRevenue,    // Tổng doanh thu (Gói tập + Shop)
        BigDecimal packageRevenue,  // Doanh thu từ Gói tập
        BigDecimal productRevenue,  // Doanh thu từ Đơn hàng Shop
        long totalEnrollments,      // Tổng số hợp đồng gói tập
        long totalOrders,           // Tổng số đơn hàng shop
        List<RevenueDataPoint> dataPoints // Chuỗi dữ liệu biểu đồ theo thời gian
) {
    public record RevenueDataPoint(
            String period,              // VD: "T05/2026", "2026-11-20"
            BigDecimal packageAmount,
            BigDecimal productAmount,
            BigDecimal totalAmount
    ) {}
}