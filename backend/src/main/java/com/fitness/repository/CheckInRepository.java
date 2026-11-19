package com.fitness.repository;

import com.fitness.model.CheckIn;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    // 1. Kiểm tra chống quét trùng lặp trong vòng 2 giờ
    Optional<CheckIn> findTopByUserIdAndCheckedInAtAfterOrderByCheckedInAtDesc(
            Long userId,
            LocalDateTime afterTime
    );

    // 2. Lấy danh sách lượt check-in theo ngày (dùng JOIN FETCH để tối ưu hiệu năng)
    @Query("SELECT c FROM CheckIn c " +
           "JOIN FETCH c.user u " +
           "LEFT JOIN FETCH c.enrollment e " +
           "LEFT JOIN FETCH e.workoutPackage " +
           "LEFT JOIN FETCH c.staff " +
           "WHERE c.checkedInAt >= :start AND c.checkedInAt <= :end " +
           "ORDER BY c.checkedInAt DESC")
    List<CheckIn> findByCheckedInAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 3. Lấy N lượt check-in gần nhất (mặc định limit 10)
    @Query("SELECT c FROM CheckIn c " +
           "JOIN FETCH c.user u " +
           "LEFT JOIN FETCH c.enrollment e " +
           "LEFT JOIN FETCH e.workoutPackage " +
           "LEFT JOIN FETCH c.staff " +
           "ORDER BY c.checkedInAt DESC")
    List<CheckIn> findRecentCheckIns(Pageable pageable);

    // 4. Lấy lượt check-in gần nhất của 1 học viên cụ thể
    Optional<CheckIn> findTopByUserIdOrderByCheckedInAtDesc(Long userId);
}