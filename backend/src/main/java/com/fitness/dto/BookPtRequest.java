package com.fitness.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record BookPtRequest(
        @NotNull(message = "userId không được để trống")
        @Positive(message = "userId phải lớn hơn 0")
        Long userId,

        @NotNull(message = "trainerId không được để trống")
        @Positive(message = "trainerId phải lớn hơn 0")
        Long trainerId,

        @NotNull(message = "bookingDateTime không được để trống")
        @Future(message = "Thời gian đặt lịch phải ở tương lai")
        LocalDateTime bookingDateTime,

        @Size(max = 1000, message = "Ghi chú tối đa 1000 ký tự")
        String note
) {}
