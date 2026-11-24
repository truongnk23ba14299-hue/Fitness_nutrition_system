package com.fitness.dto;

import com.fitness.model.EnrollmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EnrollmentResponse(
        Long enrollmentId,
        Long packageId,
        String packageTitle,
        String userFullName,
        String userEmail,
        BigDecimal totalPrice,
        LocalDate startDate,
        LocalDate endDate,
        EnrollmentStatus status,
        String notes,
        LocalDateTime createdAt,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        String voucherCode,
        String paymentMethod,
        PaymentInfoResponse payment
) {}