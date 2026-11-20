package com.fitness.repository;

import com.fitness.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 1. Lấy danh sách thông báo của user (mới nhất xếp lên đầu)
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 2. Tìm thông báo theo ID và UserID (bảo mật: không cho user khác đọc/xóa trộm)
    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    // 3. Đánh dấu tất cả thông báo của 1 user là đã đọc
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);

    // 4. Đếm số lượng chưa đọc (để hiển thị badge chuông)
    long countByUserIdAndIsReadFalse(Long userId);
}