package com.fitness.model;

/**
 * Trạng thái đơn hàng Shop của học viên.
 * Luồng xử lý: PENDING -> CONFIRMED -> SHIPPING -> COMPLETED
 * Đơn có thể bị CANCELLED khi còn ở PENDING hoặc CONFIRMED.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPING,
    COMPLETED,
    CANCELLED
}
