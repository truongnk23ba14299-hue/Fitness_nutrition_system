package com.fitness.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code; // NEWMEMBER, VIP2026, FITLIFE50

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "discount_type", nullable = false, length = 20)
    private String discountType; // PERCENT hoặc FIXED

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "max_discount", precision = 12, scale = 2)
    private BigDecimal maxDiscount; // Mức giảm tối đa nếu là %

    @Column(name = "min_order_value", precision = 12, scale = 2)
    private BigDecimal minOrderValue; // Giá trị đơn hàng tối thiểu

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String scope = "ALL"; // ALL, PACKAGE, PRODUCT

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}