package com.fitness.repository;

import com.fitness.model.EnrollmentStatus;
import com.fitness.model.PackageEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface PackageEnrollmentRepository extends JpaRepository<PackageEnrollment, Long> {

    List<PackageEnrollment> findByUserId(Long userId);

    List<PackageEnrollment> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT e FROM PackageEnrollment e " +
           "JOIN FETCH e.workoutPackage " +
           "WHERE e.user.id = :userId " +
           "ORDER BY e.createdAt DESC")
    List<PackageEnrollment> findMyEnrollmentsWithPackage(@Param("userId") Long userId);

    List<PackageEnrollment> findByStatus(EnrollmentStatus status);

    boolean existsByUserIdAndStatus(Long userId, EnrollmentStatus status);

    boolean existsByUserIdAndWorkoutPackageIdAndStatus(
            Long userId,
            Long packageId,
            EnrollmentStatus status
    );

    long countByWorkoutPackageIdAndStatus(Long packageId, EnrollmentStatus status);

    long countByWorkoutPackageId(Long packageId);
    // tìm kiếm theo tên hoặc email của học viên:
     @Query("SELECT e FROM PackageEnrollment e " +
           "JOIN FETCH e.user u " +
           "JOIN FETCH e.workoutPackage p " +
           "LEFT JOIN FETCH e.trainer t " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY e.createdAt DESC")
    List<PackageEnrollment> searchEnrollments(@Param("keyword") String keyword);
}
