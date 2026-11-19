package com.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberLookupResponse {

    // 1. Thông tin cá nhân hội viên (Khớp bảng users)
    private Long id;
    private String memberCode;        // Mã định dạng chuẩn "FL-00007"
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String avatarUrl;
    private String gender;
    private LocalDate dateOfBirth;
    private String userStatus;        // "ACTIVE" hoặc "LOCKED"

    // 2. Thông tin gói tập đang sử dụng (Khớp bảng package_enrollments + packages)
    private Long enrollmentId;
    private Long packageId;
    private String packageTitle;      // Tên gói tập (Ví dụ: "Gói VIP Thượng Hạng (12 Tháng)")
    private BigDecimal packagePrice;  // Giá gói đã đăng ký
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer remainingDays;    // Số ngày còn lại (>= 0: còn hạn, < 0: hết hạn, null: chưa có gói)

    // 3. Trạng thái tổng hợp cho Lễ tân quầy (Khớp Badge trên UI)
    // ACTIVE: Thẻ hợp lệ | EXPIRED: Thẻ hết hạn | NO_PACKAGE: Chưa mua gói | LOCKED: Tài khoản bị khóa
    private String status;
}