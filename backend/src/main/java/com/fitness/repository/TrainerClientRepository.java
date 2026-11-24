package com.fitness.repository;

import com.fitness.model.PackageEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository phục vụ module HLV quản lý học viên kèm riêng.
 * Truy vấn trên bảng package_enrollments theo trainer_id (không đụng tới
 * PackageEnrollmentRepository của module đăng ký gói tập).
 */
@Repository
public interface TrainerClientRepository extends JpaRepository<PackageEnrollment, Long> {

    // 1. Tất cả đăng ký gói tập do HLV này phụ trách (mới nhất xếp lên đầu)
    List<PackageEnrollment> findByTrainer_IdOrderByCreatedAtDesc(Long trainerId);

    // 2. Các đăng ký gói tập của 1 học viên với HLV này
    List<PackageEnrollment> findByTrainer_IdAndUser_IdOrderByCreatedAtDesc(Long trainerId, Long userId);

    // 3. Kiểm tra học viên có thuộc quyền quản lý của HLV hay không (dùng để chặn xem hồ sơ người khác)
    boolean existsByTrainer_IdAndUser_Id(Long trainerId, Long userId);
}
