package com.fitness.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ghi chú: Chi tiết gói tập đã được tích hợp vào entity WorkoutPackage (bảng "packages")
 * để tránh lỗi Hibernate DuplicateMappingException khi 2 entity cùng map vào bảng "packages".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageDetail {

    private Long id;
    private String benefits;
    private String schedule;
    private String trainerInfo;
}
