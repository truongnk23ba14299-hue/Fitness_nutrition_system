package com.fitness.dto;

import com.fitness.model.EnrollmentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record PosSaleResponse(
        String invoiceCode,        // Mã hóa đơn POS (VD: POS-829104)
        Long enrollmentId,         // ID hợp đồng gói tập vừa tạo
        Long memberId,             // ID hội viên trong hệ thống
        String memberCode,         // Mã thẻ chuẩn "FL-00012"
        String customerName,       // Họ tên khách hàng
        String customerPhone,      // Số điện thoại
        String customerEmail,      // Email khách hàng
        Long packageId,            // ID gói tập
        String packageTitle,       // Tên gói tập / loại vé
        BigDecimal subtotal,       // Giá niêm yết ban đầu
        BigDecimal discountAmount, // Số tiền giảm giá
        String voucherCode,        // Mã voucher
        BigDecimal totalAmount,    // Tổng tiền thực tế đã thu
        String paymentMethod,      // CASH, CARD, VIETQR
        LocalDate startDate,       // Ngày bắt đầu sử dụng
        LocalDate endDate,         // Ngày hết hạn
        EnrollmentStatus status,   // Trạng thái (ACTIVE)
        String notes,              // Ghi chú
        String staffName,          // Tên nhân viên lễ tân lập hóa đơn
        Boolean isCheckedIn,       // Đã tự động check-in vào tập luôn chưa
        LocalDateTime createdAt,    // Thời điểm xuất hóa đơn
        PaymentInfoResponse payment  // Thông tin QR SePay nếu paymentMethod = VIETQR
) {}