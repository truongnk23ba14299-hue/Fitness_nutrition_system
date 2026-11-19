package com.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberLookupResponse {

    // 1. Thông tin cá nhân học viên (Khớp bảng users)
    private Long id;
    private String memberCode;           // Mã thẻ chuẩn "FL-00007"
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String avatarUrl;
    private String gender;
    private LocalDate dateOfBirth;
    private String userStatus;           // "ACTIVE", "LOCKED"

    // 2. Chi tiết gói tập đang kích hoạt (Khớp bảng package_enrollments + packages)
    private Long enrollmentId;
    private Long packageId;
    private String packageTitle;         // Tên gói tập (VD: Gói VIP Thượng Hạng)
    private Integer durationMonths;      // Thời hạn gói (tháng)
    private BigDecimal packagePrice;     // Giá tiền gói
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer remainingDays;       // Số ngày còn hạn (âm nếu đã hết hạn, null nếu chưa có gói)
    private Long trainerId;              // ID Huấn luyện viên kèm riêng (nếu có)
    private String trainerName;          // Tên Huấn luyện viên kèm riêng (nếu có)
    private String benefits;             // Quyền lợi gói tập
    private String scheduleDetails;      // Lịch tập chi tiết

    // 3. Trạng thái thẻ & điều kiện check-in vào phòng tập
    private boolean canCheckIn;          // true nếu thẻ hợp lệ còn hạn, tài khoản không bị khóa
    private String status;               // "ACTIVE", "EXPIRED", "NO_PACKAGE", "LOCKED"
    private LocalDateTime lastCheckInAt; // Thời gian điểm danh gần nhất (nếu có)
    private String lastCheckInMethod;    // QR / MANUAL / SCANNER
}