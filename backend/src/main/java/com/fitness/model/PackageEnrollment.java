package com.fitness.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "package_enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người dùng đăng ký gói tập (lấy thông tin từ JWT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Gói tập mà người dùng đã lựa chọn
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private WorkoutPackage workoutPackage;

    // Huấn luyện viên được chọn (không bắt buộc)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

    // Giá gói tập tại thời điểm người dùng đăng ký
    @Column(name = "total_price",
            nullable = false,
            precision = 15,
            scale = 2)
    private BigDecimal totalPrice;

    // Ngày bắt đầu sử dụng gói tập
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // Ngày cuối cùng người dùng được phép sử dụng gói tập
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // Trạng thái đăng ký gói tập: ACTIVE, EXPIRED hoặc CANCELLED
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EnrollmentStatus status;

    // Ghi chú bổ sung về lượt đăng ký
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "subtotal", precision = 12, scale = 2)
    private java.math.BigDecimal subtotal;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private java.math.BigDecimal discountAmount;

    @Column(name = "voucher_code", length = 50)
    private String voucherCode;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod; // VIETQR, COD, CASH

    // Quản lý số buổi tập PT đi kèm trong gói tập
    @Column(name = "pt_sessions_total")
    @Builder.Default
    private Integer ptSessionsTotal = 0;

    @Column(name = "pt_sessions_remaining")
    @Builder.Default
    private Integer ptSessionsRemaining = 0;
    // Quản lý bảo lưu gói tập
    @Column(name = "freeze_start")
    private LocalDate freezeStart;

    @Column(name = "freeze_end")
    private LocalDate freezeEnd;

    // Thời điểm tạo bản ghi đăng ký, được Hibernate tự động thiết lập
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}