package com.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO trả về lịch sử hóa đơn thanh toán tập trung (gói tập + shop sản phẩm)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryResponse {
    private String id;              // Mã định danh hiển thị: HD-1 (gói tập) hoặc DH-1 (đơn hàng)
    private String invoiceNo;       // Số hóa đơn: INV-PKG-1 hoặc INV-ORD-1
    private String refType;         // ENROLLMENT (gói tập) hoặc ORDER (đơn hàng)
    private String description;     // Mô tả tóm tắt: Đăng ký Gói VIP... hoặc Đơn hàng TPCN #1 (3 sản phẩm)
    private BigDecimal amount;      // Số tiền thanh toán thực tế sau giảm
    private BigDecimal subtotal;    // Tổng tiền gốc trước giảm
    private BigDecimal discountAmount; // Số tiền được giảm
    private String voucherCode;     // Mã giảm giá đã áp dụng (nếu có)
    private String paymentMethod;   // VIETQR, COD, CASH, TRANSFER
    private String status;          // Trạng thái: PAID, ACTIVE, COMPLETED, CANCELLED
    private LocalDateTime date;     // Ngày thanh toán / tạo hóa đơn
    private List<PaymentItemDto> items; // Chi tiết các dòng hàng

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentItemDto {
        private String name;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal total;
    }
}
