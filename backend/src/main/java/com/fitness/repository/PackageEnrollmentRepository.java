package com.fitness.repository;

import com.fitness.model.EnrollmentStatus;
import com.fitness.model.PackageEnrollment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageEnrollmentRepository
        extends JpaRepository<PackageEnrollment, Long> {

    // Lấy danh sách đăng ký gói tập của một người dùng
    List<PackageEnrollment> findByUserId(Long userId);

    // Lấy danh sách đăng ký của người dùng, sắp xếp theo thời gian tạo mới nhất
    List<PackageEnrollment> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Lấy danh sách đăng ký theo trạng thái
    List<PackageEnrollment> findByStatus(EnrollmentStatus status);

    // Kiểm tra người dùng có đăng ký gói tập đang hoạt động hay không
    boolean existsByUserIdAndStatus(
            Long userId,
            EnrollmentStatus status
    );

    // Kiểm tra người dùng đã đăng ký một gói cụ thể với trạng thái tương ứng hay chưa
    boolean existsByUserIdAndWorkoutPackageIdAndStatus(
            Long userId,
            Long packageId,
            EnrollmentStatus status
    );
}