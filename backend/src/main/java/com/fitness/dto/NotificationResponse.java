package com.fitness.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String type,        // REMINDER, SYSTEM, ORDER, ENROLLMENT, BOOKING
        String severity,    // INFO, WARNING, URGENT
        String title,
        String message,
        String link,
        Boolean isRead,
        LocalDateTime createdAt
) {}