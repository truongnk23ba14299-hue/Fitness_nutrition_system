package com.fitness.repository;

import com.fitness.model.EnrollmentStatus;
import com.fitness.model.PackageEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageEnrollmentRepository extends JpaRepository<PackageEnrollment, Long> {

    List<PackageEnrollment> findByUserId(Long userId);

    List<PackageEnrollment> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<PackageEnrollment> findByStatus(EnrollmentStatus status);

    boolean existsByUserIdAndStatus(Long userId, EnrollmentStatus status);

    boolean existsByUserIdAndWorkoutPackageIdAndStatus(
            Long userId,
            Long packageId,
            EnrollmentStatus status
    );

    long countByWorkoutPackageIdAndStatus(Long packageId, EnrollmentStatus status);

    long countByWorkoutPackageId(Long packageId);
}