package com.fitness.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true) // Bỏ qua mọi trường thừa mà Frontend gửi lên
public record EnrollmentRequest(

        // ID gói tập mà người dùng muốn đăng ký
        @NotNull(message = "ID gói tập không được để trống")
        @Positive(message = "ID gói tập phải lớn hơn 0")
        Long packageId,

        // Ngày bắt đầu sử dụng gói tập (mặc định là ngày hiện tại nếu không cung cấp)
        LocalDate startDate,

        // ID huấn luyện viên cá nhân (không bắt buộc)
        @Positive(message = "ID huấn luyện viên phải lớn hơn 0")
        Long trainerId,

        // Ghi chú bổ sung của người dùng, tối đa 500 ký tự
        @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
        String notes,

        String paymentMethod,
        String voucherCode,
        BigDecimal discountAmount,
        BigDecimal totalPrice
) {}